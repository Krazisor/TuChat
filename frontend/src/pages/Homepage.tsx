import React from 'react';
import { Layout, Typography, Button, Row, Col, Card, Space, Divider } from 'antd';
import {
    RobotOutlined,
    DatabaseOutlined,
    SafetyCertificateOutlined,
    FileTextOutlined,
    ApiOutlined,
    CloudOutlined
} from '@ant-design/icons';
import { useNavigate } from "react-router";
import {useAppSelector} from "../stores/StoreHook.ts";
import {useLogto} from "@logto/react";
import {env} from "../env.ts";

const { Header, Content, Footer } = Layout;
const { Title, Paragraph } = Typography;

// 微软风格配色
const colors = {
    blue: '#0078d4',
    green: '#107c10',
    orange: '#d83b01',
    lightBlue: '#deecf9',
    lightGreen: '#e8f1e8'
};

const HomePage: React.FC = () => {
    const nav = useNavigate();
    const user = useAppSelector(state => state.user);
    const { signIn, signOut, isAuthenticated } = useLogto();

    return (
        <Layout style={{ minHeight: '100vh', background: '#fff'}}>
            {/* 头部导航 */}
            <Header style={{
                padding: '0 50px',
                height: '64px',
                lineHeight: '64px',
                background: '#fff',
                boxShadow: '0 1px 4px rgba(0,0,0,0.1)',
                position: 'sticky',
                top: 0,
                zIndex: 100,
                width: '100%'
            }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', margin: '0 auto' }}>
                    <div style={{ display: 'flex', alignItems: 'center' }}>
                        <RobotOutlined style={{
                            fontSize: '26px',
                            marginRight: '16px',
                            color: colors.blue,
                            transform: 'rotate(-10deg)'
                        }}/>
                        <Title level={4} style={{ margin: 0, color: colors.blue }}>途聊 TuChat</Title>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center' }}>
                        <Space size="middle">
                            <Button type="text" onClick={() => nav("/dashboard")}>仪表盘</Button>
                            {isAuthenticated ?
                                <Button type="primary" style={{ backgroundColor: colors.blue }} onClick={() => signOut(env.VITE_APP_URL)}>注销</Button> :
                                <Button type="primary" style={{ backgroundColor: colors.blue }} onClick={() => signIn(env.VITE_APP_URL + '/callback')}>登录</Button> }
                        </Space>
                    </div>
                </div>
            </Header>

            <Content style={{ width: '100%' }}>
                {/* 英雄区域 */}
                <div style={{
                    background: `linear-gradient(135deg, ${colors.lightBlue}, #fff)`,
                    padding: '80px 50px',
                    width: '100%'
                }}>
                    <Row gutter={[48, 48]} align="middle">
                        <Col xs={24} md={12}>
                            <Title style={{ fontSize: 42, marginBottom: 24, color: '#111' }}>
                                智能知识库对话系统
                            </Title>
                            <Paragraph style={{ fontSize: 18, margin: '0 0 40px', color: '#555' }}>
                                连接企业数据与文档，构建安全可靠的知识库对话平台
                            </Paragraph>

                            <Space size="large">
                                <Button
                                    type="primary"
                                    size="large"
                                    style={{ height: 48, width: 160, fontSize: 16, backgroundColor: colors.blue }}
                                >
                                    开始使用
                                </Button>
                                <Button
                                    size="large"
                                    style={{ height: 48, width: 160, fontSize: 16 }}
                                >
                                    查看文档
                                </Button>
                            </Space>
                        </Col>

                        <Col xs={24} md={12}>
                            {/* 聊天界面模拟 */}
                            <div style={{
                                background: '#fff',
                                borderRadius: 8,
                                boxShadow: '0 12px 48px rgba(0,0,0,0.12)',
                                position: 'relative',
                                overflow: 'hidden',
                                padding: 20
                            }}>
                                <div style={{ display: 'flex', marginBottom: 20 }}>
                                    <div style={{
                                        backgroundColor: '#f0f0f0',
                                        borderRadius: '18px',
                                        padding: '12px 20px',
                                        maxWidth: '70%'
                                    }}>
                                        请帮我分析最近一个季度的销售数据，重点关注增长最快的产品线。
                                    </div>
                                </div>

                                <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 20 }}>
                                    <div style={{
                                        backgroundColor: colors.lightBlue,
                                        color: '#333',
                                        borderRadius: '18px',
                                        padding: '12px 20px',
                                        maxWidth: '70%'
                                    }}>
                                        <div>根据数据库分析，Q2季度增长最快的是智能家居产品线，同比增长46.8%。详细报告如下：</div>
                                        <div style={{
                                            backgroundColor: '#fff',
                                            marginTop: 12,
                                            padding: 10,
                                            borderRadius: 4,
                                            border: '1px solid #ddd'
                                        }}>
                                            <div style={{ fontWeight: 'bold' }}>产品线季度增长报告</div>
                                            <div>- 智能家居：+46.8%</div>
                                            <div>- 办公设备：+24.2%</div>
                                            <div>- 网络设备：+18.5%</div>
                                        </div>
                                    </div>
                                </div>

                                {/* 装饰标签 */}
                                <div style={{
                                    position: 'absolute',
                                    top: 10,
                                    right: 10,
                                    backgroundColor: colors.green,
                                    color: 'white',
                                    padding: '4px 10px',
                                    borderRadius: 4,
                                    fontSize: 12
                                }}>
                                    数据库分析
                                </div>
                            </div>
                        </Col>
                    </Row>
                </div>

                {/* 亮点特性区 */}
                <div style={{ padding: '80px 50px', width: '100%' }}>
                    <Title level={2} style={{ textAlign: 'center', marginBottom: 60 }}>为什么选择途聊</Title>

                    <Row gutter={[40, 40]}>
                        <Col xs={24} sm={12} md={8}>
                            <Card variant={"outlined"} style={{ height: '100%' }}>
                                <div style={{ color: colors.blue, fontSize: 36, marginBottom: 16 }}>
                                    <DatabaseOutlined />
                                </div>
                                <Title level={4}>数据库连接能力</Title>
                                <Paragraph style={{ color: '#555', fontSize: 15 }}>
                                    安全连接多种数据源，执行SQL查询，通过自然语言对话形式展现业务数据洞察
                                </Paragraph>
                            </Card>
                        </Col>

                        <Col xs={24} sm={12} md={8}>
                            <Card variant={"outlined"} style={{ height: '100%' }}>
                                <div style={{ color: colors.green, fontSize: 36, marginBottom: 16 }}>
                                    <FileTextOutlined />
                                </div>
                                <Title level={4}>智能文档管理</Title>
                                <Paragraph style={{ color: '#555', fontSize: 15 }}>
                                    将各类文档转化为结构化知识，支持多级目录、权限管理和全文检索
                                </Paragraph>
                            </Card>
                        </Col>

                        <Col xs={24} sm={12} md={8}>
                            <Card variant={"outlined"} style={{ height: '100%' }}>
                                <div style={{ color: colors.orange, fontSize: 36, marginBottom: 16 }}>
                                    <SafetyCertificateOutlined />
                                </div>
                                <Title level={4}>企业级安全</Title>
                                <Paragraph style={{ color: '#555', fontSize: 15 }}>
                                    严格的权限控制，操作审计和数据加密，保障企业数据安全合规
                                </Paragraph>
                            </Card>
                        </Col>
                    </Row>
                </div>

                {/* 解决方案区域 */}
                <div style={{
                    background: `linear-gradient(135deg, ${colors.lightGreen}, #fff)`,
                    padding: '80px 50px',
                    width: '100%'
                }}>
                    <Title level={2} style={{ textAlign: 'center', marginBottom: 50 }}>行业解决方案</Title>

                    <Row gutter={[24, 24]}>
                        <Col xs={24} md={8}>
                            <Card
                                hoverable
                                style={{
                                    height: 320,
                                    borderRadius: 8,
                                    overflow: 'hidden',
                                    border: 'none',
                                    boxShadow: '0 8px 24px rgba(0,0,0,0.1)'
                                }}
                                cover={
                                    <div style={{
                                        height: 140,
                                        background: colors.blue,
                                        display: 'flex',
                                        justifyContent: 'center',
                                        alignItems: 'center',
                                        color: '#fff',
                                        fontSize: 48
                                    }}>
                                        <CloudOutlined />
                                    </div>
                                }
                            >
                                <Title level={4}>金融行业</Title>
                                <Paragraph>
                                    安全合规的金融知识库，市场分析报告检索，客户画像分析
                                </Paragraph>
                            </Card>
                        </Col>

                        <Col xs={24} md={8}>
                            <Card
                                hoverable
                                style={{
                                    height: 320,
                                    borderRadius: 8,
                                    overflow: 'hidden',
                                    border: 'none',
                                    boxShadow: '0 8px 24px rgba(0,0,0,0.1)'
                                }}
                                cover={
                                    <div style={{
                                        height: 140,
                                        background: colors.green,
                                        display: 'flex',
                                        justifyContent: 'center',
                                        alignItems: 'center',
                                        color: '#fff',
                                        fontSize: 48
                                    }}>
                                        <DatabaseOutlined />
                                    </div>
                                }
                            >
                                <Title level={4}>医疗健康</Title>
                                <Paragraph>
                                    医学知识库构建，病历分析，临床决策支持，医疗文献检索系统
                                </Paragraph>
                            </Card>
                        </Col>

                        <Col xs={24} md={8}>
                            <Card
                                hoverable
                                style={{
                                    height: 320,
                                    borderRadius: 8,
                                    overflow: 'hidden',
                                    border: 'none',
                                    boxShadow: '0 8px 24px rgba(0,0,0,0.1)'
                                }}
                                cover={
                                    <div style={{
                                        height: 140,
                                        background: colors.orange,
                                        display: 'flex',
                                        justifyContent: 'center',
                                        alignItems: 'center',
                                        color: '#fff',
                                        fontSize: 48
                                    }}>
                                        <ApiOutlined />
                                    </div>
                                }
                            >
                                <Title level={4}>制造业</Title>
                                <Paragraph>
                                    产品知识库，故障排查助手，技术文档管理，生产数据分析
                                </Paragraph>
                            </Card>
                        </Col>
                    </Row>
                </div>
            </Content>

            {/* 简化的页脚 */}
            <Footer style={{ background: '#333', padding: '40px 50px', width: '100%' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap' }}>
                    <div>
                        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 15 }}>
                            <RobotOutlined style={{ color: '#fff', fontSize: 24, marginRight: 10 }} />
                            <Title level={4} style={{ margin: 0, color: '#fff' }}>途聊 TuChat</Title>
                        </div>
                        <Paragraph style={{ color: 'rgba(255,255,255,0.6)', maxWidth: 400 }}>
                            智能知识库对话系统，连接企业数据与知识，提升团队协作效率
                        </Paragraph>
                    </div>

                    <div style={{ display: 'flex', gap: 60 }}>
                        <div>
                            <Title level={5} style={{ color: '#fff' }}>产品</Title>
                            <ul style={{ listStyle: 'none', padding: 0 }}>
                                <li style={{ margin: '8px 0' }}><a style={{ color: 'rgba(255,255,255,0.6)' }} href="#">基础版</a></li>
                                <li style={{ margin: '8px 0' }}><a style={{ color: 'rgba(255,255,255,0.6)' }} href="#">企业版</a></li>
                            </ul>
                        </div>
                        <div>
                            <Title level={5} style={{ color: '#fff' }}>联系我们</Title>
                            <ul style={{ listStyle: 'none', padding: 0 }}>
                                <li style={{ margin: '8px 0' }}><a style={{ color: 'rgba(255,255,255,0.6)' }} href="#">sales@tuchat.com</a></li>
                                <li style={{ margin: '8px 0' }}><a style={{ color: 'rgba(255,255,255,0.6)' }} href="#">sales@tuchat.com</a></li>
                                <li style={{ margin: '8px 0' }}><a style={{ color: 'rgba(255,255,255,0.6)' }} href="#">400-888-9999</a></li>
                            </ul>
                        </div>
                    </div>
                </div>

                <Divider style={{ borderColor: 'rgba(255,255,255,0.1)', margin: '20px 0' }} />

                <div style={{ color: 'rgba(255,255,255,0.4)', textAlign: 'center' }}>
                    © {new Date().getFullYear()} 221310329屠皓然 版权所有
                </div>
            </Footer>
        </Layout>
    );
};

export default HomePage;