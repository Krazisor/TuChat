import React, { useState } from 'react';
import { Layout, Typography } from 'antd';
const { Header, Content } = Layout;
const { Title } = Typography;
import SideBar from "./Sidebar.tsx"
import Developing from "../dashboardPages/Developing.tsx";
import UserProfile from "../dashboardPages/UserProfile.tsx";

export type ActiveMenuTypes = 'profile' | 'aiChat' | 'knowledge' | 'database' | 'organization' | 'settings' | 'help';

const Dashboard = () => {
    const [activeMenu, setActiveMenu] = useState<ActiveMenuTypes>('aiChat');
    const [collapsed, setCollapsed] = useState(false);

    const getPageTitle = () => {
        const titles: Record<ActiveMenuTypes, string> = {
            profile: '个人信息',
            aiChat: 'AI聊天',
            knowledge: '知识库',
            database: '数据库',
            organization: '组织管理',
            settings: '设置',
            help: '帮助与支持',
        };
        return titles[activeMenu] || '仪表盘';
    };

    const renderContent = () => {
        switch (activeMenu) {
            case 'profile':
                return <UserProfile />;
            // case 'aiChat':
            //     return <AIChat />;
            // case 'knowledge':
            //     return <Knowledge />;
            // case 'database':
            //     return <Database />;
            // case 'organization':
            //     return <Organization />;
            default:
                return <Developing />;
        }
    };

    return (
        <Layout style={{ height: '100vh', width: '100vw', display: 'flex' }}>
            <SideBar
                activeMenu={activeMenu}
                setActiveMenu={setActiveMenu}
                collapsed={collapsed}
                setCollapsed={setCollapsed}
            />
            <Layout>
                <Header style={{
                    background: '#fff',
                    padding: '0 24px',
                    boxShadow: '0 1px 4px rgba(0, 21, 41, 0.08)',
                    display: 'flex',
                    alignItems: 'center'
                }}>
                    <Title level={4} style={{ margin: 0 }}>
                        {getPageTitle()}
                    </Title>
                </Header>
                <Content style={{
                    margin: '24px 16px',
                    padding: 24,
                    background: '#fff',
                    borderRadius: '4px',
                    minHeight: 280
                }}>
                    {renderContent()}
                </Content>
            </Layout>
        </Layout>
    );
};

export default Dashboard;