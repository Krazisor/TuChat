import React, { useEffect, useState } from 'react';
import { Typography, Space } from 'antd';
import { motion } from 'framer-motion';

const { Title } = Typography;

const Developing: React.FC = () => {
    const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });
    const [count, setCount] = useState(0);

    // 追踪鼠标位置以创建交互效果
    useEffect(() => {
        const handleMouseMove = (e: MouseEvent) => {
            setMousePosition({ x: e.clientX, y: e.clientY });
        };

        window.addEventListener('mousemove', handleMouseMove);

        // 动画计数器
        const interval = setInterval(() => {
            setCount(prev => (prev + 1) % 100);
        }, 50);

        return () => {
            window.removeEventListener('mousemove', handleMouseMove);
            clearInterval(interval);
        };
    }, []);

    return (
        <div
            style={{
                width: '100%',
                height: '100%',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                overflow: 'hidden',
                position: 'relative',
                background: 'linear-gradient(135deg, #0f17a4 0%, #1a1a2e 100%)',
            }}
        >
            {/* 精致背景 */}
            <div style={{
                position: 'absolute',
                top: 0,
                left: 0,
                width: '100%',
                height: '100%',
                backgroundImage:
                    'radial-gradient(circle at 25% 25%, rgba(53, 92, 125, 0.05) 2%, transparent 6%), ' +
                    'radial-gradient(circle at 75% 75%, rgba(138, 112, 181, 0.05) 1%, transparent 5%), ' +
                    'radial-gradient(circle at 65% 35%, rgba(255, 215, 179, 0.05) 1%, transparent 4%)',
                backgroundSize: '120px 120px',
                opacity: 0.7,
            }} />

            {/* 光效跟随鼠标 */}
            <motion.div
                style={{
                    position: 'absolute',
                    background: 'radial-gradient(circle, rgba(255,255,255,0.03) 0%, transparent 70%)',
                    width: '60vw',
                    height: '60vw',
                    borderRadius: '50%',
                }}
                animate={{
                    x: mousePosition.x - 30 + 'vw',
                    y: mousePosition.y - 30 + 'vh',
                }}
                transition={{ type: 'spring', mass: 2, stiffness: 50 }}
            />

            {/* 奢华边框装饰 */}
            <div style={{
                position: 'absolute',
                top: 40,
                left: 40,
                right: 40,
                bottom: 40,
                border: '1px solid rgba(255, 255, 255, 0.03)',
                boxShadow: '0 0 40px rgba(0, 0, 0, 0.15) inset',
                pointerEvents: 'none',
            }}/>

            {/* 装饰线条 - 左上 */}
            <motion.div
                style={{
                    position: 'absolute',
                    top: 20,
                    left: 20,
                    width: 60,
                    height: 60,
                    borderLeft: '1px solid rgba(255, 215, 179, 0.3)',
                    borderTop: '1px solid rgba(255, 215, 179, 0.3)',
                }}
                animate={{ opacity: [0.3, 0.5, 0.3] }}
                transition={{ repeat: Infinity, duration: 3 }}
            />

            {/* 装饰线条 - 右下 */}
            <motion.div
                style={{
                    position: 'absolute',
                    bottom: 20,
                    right: 20,
                    width: 60,
                    height: 60,
                    borderRight: '1px solid rgba(255, 215, 179, 0.3)',
                    borderBottom: '1px solid rgba(255, 215, 179, 0.3)',
                }}
                animate={{ opacity: [0.3, 0.5, 0.3] }}
                transition={{ repeat: Infinity, duration: 3, delay: 1.5 }}
            />

            {/* 中央内容 */}
            <motion.div
                style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 10,
                    padding: '40px',
                    background: 'rgba(26, 32, 44, 0.4)',
                    backdropFilter: 'blur(20px)',
                    borderRadius: '2px',
                    boxShadow: '0 20px 80px rgba(0, 0, 0, 0.3)',
                    border: '1px solid rgba(255, 255, 255, 0.05)',
                    maxWidth: '600px',
                    position: 'relative',
                }}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 1 }}
            >
                {/* 精致标志 */}
                <div style={{
                    width: '80px',
                    height: '2px',
                    background: 'linear-gradient(90deg, transparent, rgba(255, 215, 179, 0.8), transparent)',
                    marginBottom: '30px',
                }}/>

                <Title
                    level={1}
                    style={{
                        fontSize: '2.8rem',
                        fontWeight: 200,
                        color: 'white',
                        letterSpacing: '0.2em',
                        textTransform: 'uppercase',
                        margin: 0,
                        textAlign: 'center',
                    }}
                >
                    <Space>
                        精 心 打 造
                    </Space>
                </Title>

                <motion.div
                    style={{
                        fontSize: '1rem',
                        color: 'rgba(255, 255, 255, 0.7)',
                        letterSpacing: '0.1em',
                        marginTop: '20px',
                        fontWeight: 300,
                        textAlign: 'center',
                    }}
                >
                    <Space size={18}>
                        {[...Array(3)].map((_, i) => (
                            <motion.span
                                key={i}
                                animate={{
                                    opacity: [0.4, 0.8, 0.4],
                                    scale: [0.98, 1, 0.98]
                                }}
                                transition={{
                                    repeat: Infinity,
                                    duration: 2,
                                    delay: i * 0.7
                                }}
                                style={{
                                    display: 'inline-block',
                                    width: '8px',
                                    height: '8px',
                                    borderRadius: '50%',
                                    background: 'rgba(255, 215, 179, 0.7)'
                                }}
                            />
                        ))}
                    </Space>
                </motion.div>

                <div style={{
                    fontSize: '0.9rem',
                    color: 'rgba(255, 255, 255, 0.6)',
                    marginTop: '30px',
                    letterSpacing: '0.15em',
                    textAlign: 'center',
                    maxWidth: '450px',
                    lineHeight: 1.8,
                    fontWeight: 300,
                }}>
                    系统升级过程中，我们正在为您创造卓越的体验。
                    <br/>您的等待将换来更加精彩的功能与设计。
                </div>

                {/* 底部进度条 */}
                <div style={{
                    marginTop: '40px',
                    width: '180px',
                    height: '1px',
                    background: 'rgba(255, 255, 255, 0.1)',
                    position: 'relative',
                    overflow: 'hidden',
                }}>
                    <motion.div
                        style={{
                            height: '100%',
                            background: 'linear-gradient(90deg, rgba(255, 215, 179, 0.5), rgba(255, 255, 255, 0.8))',
                            width: '30%',
                        }}
                        animate={{
                            x: [-50, 180]
                        }}
                        transition={{
                            repeat: Infinity,
                            duration: 2,
                            ease: "easeInOut"
                        }}
                    />
                </div>

                {/* 高端署名 */}
                <div style={{
                    position: 'absolute',
                    bottom: '-50px',
                    fontSize: '0.7rem',
                    color: 'rgba(255, 255, 255, 0.3)',
                    letterSpacing: '0.3em',
                    textTransform: 'uppercase',
                    fontWeight: 300,
                }}>
                    EXCELLENCE IN PROGRESS
                </div>
            </motion.div>

            {/* 装饰性粒子 */}
            {[...Array(12)].map((_, i) => (
                <motion.div
                    key={i}
                    style={{
                        position: 'absolute',
                        width: Math.random() * 3 + 1 + 'px',
                        height: Math.random() * 3 + 1 + 'px',
                        borderRadius: '50%',
                        backgroundColor: 'rgba(255, 255, 255, 0.2)',
                        left: Math.random() * 100 + '%',
                        top: Math.random() * 100 + '%',
                    }}
                    animate={{
                        opacity: [0, 0.6, 0],
                        y: [-20, 20],
                    }}
                    transition={{
                        duration: Math.random() * 10 + 10,
                        repeat: Infinity,
                        delay: Math.random() * 5,
                    }}
                />
            ))}

            {/* 时间指示 */}
            <div style={{
                position: 'absolute',
                bottom: '20px',
                right: '20px',
                color: 'rgba(255, 255, 255, 0.15)',
                fontSize: '0.8rem',
                fontFamily: 'monospace',
                letterSpacing: '0.1em',
            }}>
                {new Date().toLocaleTimeString('en-US', {
                    hour12: false,
                    hour: '2-digit',
                    minute: '2-digit'
                })}
            </div>
        </div>
    );
};

export default Developing;