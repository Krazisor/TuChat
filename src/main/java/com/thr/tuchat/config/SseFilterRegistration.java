package com.thr.tuchat.config;


import cn.dev33.satoken.filter.SaTokenContextFilterForJakartaServlet;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.EnumSet;

@Configuration
public class SseFilterRegistration {

    @Bean
    public FilterRegistrationBean<SaTokenContextFilterForJakartaServlet> saTokenContextFilterForJakartaServletFilterRegistrationBean() {
        FilterRegistrationBean<SaTokenContextFilterForJakartaServlet> bean = new FilterRegistrationBean<>(new SaTokenContextFilterForJakartaServlet());
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        bean.setAsyncSupported(true);
        bean.setDispatcherTypes(EnumSet.of(DispatcherType.ASYNC, DispatcherType.REQUEST));
        return bean;
    }
}
