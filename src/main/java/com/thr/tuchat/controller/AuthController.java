package com.thr.tuchat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thr.tuchat.config.LogtoConfig;
import com.thr.tuchat.exception.ServiceDeniedException;
import com.thr.tuchat.pojo.ResponseResult;
import com.thr.tuchat.pojo.User;
import com.thr.tuchat.service.UserService;
import io.jsonwebtoken.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin
@RestController
public class AuthController {

    private final LogtoConfig logtoConfig;

    @Resource
    private UserService userService;

    // 构造方法注入
    public AuthController(LogtoConfig logtoConfig) {
        this.logtoConfig = logtoConfig;
    }

    // 会话登录接口
    @GetMapping(value = "/login")
    public Object doLogin(String token) {
        try {
            // 第一步：从 JWKS 获取公钥
            PublicKey publicKey = getEcPublicKey();

            // 第二步：校验并解析令牌
            Claims claims = Jwts.parser().verifyWith(publicKey).requireIssuer(logtoConfig.getIssuer()) // 验证发行者
                    .requireAudience(logtoConfig.getAppId()).build().parseSignedClaims(token).getPayload();
            String userId = claims.get("sub", String.class); // 用户 ID
            Map<String, String> user = (Map<String, String>) claims.get("user", Map.class);
            String avatar = user.get("avatar");
            String email = user.get("primaryEmail");
            String username = user.get("username");
            log.info("收取到用户信息:#{}, #{}, #{}, #{}", userId, username, email, avatar);
            StpUtil.login(userId); // 这里的 userId 是 Logto 返回的 `sub`
            User u = new User()
                    .setUserId(userId)
                    .setUserName(username)
                    .setEmail(email)
                    .setAvatar(avatar);
            userService.saveOrUpdateUser(u);
            return ResponseResult.success(StpUtil.getTokenValue());
        } catch (SecurityException e) {
            throw new ServiceDeniedException("令牌错误");
        } catch (ExpiredJwtException e) {
            throw new ServiceDeniedException("令牌过期");
        } catch (JwtException e) {
            throw new ServiceDeniedException("JWT 令牌解析失败");
        } catch (Exception e) {
            throw new ServiceDeniedException(e.getMessage());
        }
    }

    // 从 JWKS 获取椭圆曲线公钥
    private PublicKey getEcPublicKey() throws Exception {
        // 使用 RestTemplate 请求 JWKS JSON 数据
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(logtoConfig.getJwksUri(), String.class);
        String jwks = response.getBody();

        // 使用 Jackson 解析 JWKS JSON 数据
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> jwksData = mapper.readValue(jwks, Map.class);

        // 提取 "keys" 数组中的第一个元素
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwksData.get("keys");
        Map<String, Object> firstKey = keys.get(0); // 获取第一个 key

        // 检查是否为 EC 类型，并且算法是 ES384
        String keyType = (String) firstKey.get("kty"); // 密钥类型
        String algorithm = (String) firstKey.get("alg"); // 签名算法
        if (!"EC".equals(keyType) || !"ES384".equals(algorithm)) {
            throw new IllegalArgumentException("Unsupported key type or algorithm");
        }

        // 提取曲线参数、x 坐标、y 坐标
        String crv = (String) firstKey.get("crv"); // 曲线名称
        String x = (String) firstKey.get("x"); // 横坐标
        String y = (String) firstKey.get("y"); // 纵坐标

        // Base64 解码 x 和 y 坐标
        byte[] xBytes = Base64.getUrlDecoder().decode(x);
        byte[] yBytes = Base64.getUrlDecoder().decode(y);

        // 根据曲线名称获取 EC 参数
        ECParameterSpec ecSpec = getCurveParameters(crv); // 改为直接调用更通用的方法

        // 创建椭圆曲线公钥
        ECPoint ecPoint = new ECPoint(new BigInteger(1, xBytes), new BigInteger(1, yBytes));
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(ecPoint, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePublic(pubSpec);
    }

    // 根据曲线名称获取 EC Parameters
    private ECParameterSpec getCurveParameters(String crv) throws Exception {
        if ("P-384".equals(crv)) {
            // 使用标准曲线：P-384 对应 secp384r1
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec("secp384r1"));
            return parameters.getParameterSpec(ECParameterSpec.class);
        } else {
            throw new ServiceDeniedException("Unsupported curve: " + crv);
        }
    }
}

