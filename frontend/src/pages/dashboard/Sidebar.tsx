import React, {useEffect, useState} from 'react';
import type {ActiveMenuTypes} from "./index.tsx";
import {
    Layout,
    Menu,
    Avatar,
    Typography,
    Divider,
    Button,
    Tooltip,
    Badge, message
} from 'antd';
import {
    UserOutlined,
    MessageOutlined,
    BookOutlined,
    DatabaseOutlined,
    TeamOutlined,
    SettingOutlined,
    QuestionCircleOutlined,
    LogoutOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined
} from '@ant-design/icons';
import {useLogto} from "@logto/react";
import {env} from "../../env.ts";
import {useAppDispatch, useAppSelector} from "../../stores/StoreHook.ts";
import {clearUserInfo, setLogoutStatus, setUserInfo} from "../../stores/slices/userSlices.ts";
import {getUserBaseInfo, type UserBaseType} from "../../api/UserApi.ts";

const { Sider } = Layout;
const { Text } = Typography;

interface SideBarProps {
    activeMenu: ActiveMenuTypes,
    setActiveMenu: React.Dispatch<React.SetStateAction<ActiveMenuTypes>>,
    collapsed: boolean,
    setCollapsed: React.Dispatch<React.SetStateAction<boolean>>
}

type UserStatus = 'online' | 'away' | 'offline';

interface User {
    name: string;
    avatar?: string | null;
    status: UserStatus;
}

const Sidebar: React.FC<SideBarProps> = ({ activeMenu, setActiveMenu, collapsed, setCollapsed }) => {
    // 用户数据
    const [user, setUser] = useState<User>({
        name: '用户',
        avatar: null, // 如果有，可以替换为实际URL
        status: 'away', // online, away, offline
    })

    const { signOut } = useLogto();

    const dispatch = useAppDispatch();
    const userSlice = useAppSelector(state => state.user)

    // 状态指示器颜色
    const statusColors: Record<UserStatus, string> = {
        online: '#52c41a',
        away: '#faad14',
        offline: '#bfbfbf',
    };

    useEffect(() => {
        let response: UserBaseType| null = null;
        const tempFunction = async () => {
            response = await getUserBaseInfo();
            if (response != null) {
                dispatch(setUserInfo(response));
            }
        }
        tempFunction()
    }, []);

    useEffect(() => {
        if(userSlice.userInfo) {
            setUser({
                name: userSlice.userInfo.userName,
                avatar: userSlice.userInfo.avatar,
                status: 'online'
            });
        }
    }, [userSlice.userInfo]);

    const menuItems = [
        { key: 'profile', icon: <UserOutlined />, label: '个人信息' },
        { key: 'aiChat', icon: <MessageOutlined />, label: 'AI聊天' },
        { key: 'knowledge', icon: <BookOutlined />, label: '知识库' },
        { key: 'database', icon: <DatabaseOutlined />, label: '数据库' },
        { key: 'organization', icon: <TeamOutlined />, label: '组织管理' },
        {
            key: 'settings',
            icon: <SettingOutlined />,
            label: '设置',
        },
        {
            key: 'help',
            icon: <QuestionCircleOutlined />,
            label: '帮助与支持',
        },
    ];

    // 退出登录
    const logout = () => {
        signOut(env.VITE_APP_URL).then(r =>  {
            dispatch(setLogoutStatus())
            dispatch(clearUserInfo());
        });
    }

    return (
        <Sider
            width={220}
            collapsible
            collapsed={collapsed}
            trigger={null}
            theme="light"
            style={{
                boxShadow: '2px 0 8px 0 rgba(29,35,41,.05)',
                height: '100vh',
                position: 'sticky',
                top: 0,
                left: 0
            }}
        >
            <div style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                padding: collapsed ? '24px 0' : '32px',
                gap: collapsed ? '8px' : '12px'
            }}>
                <Badge
                    dot
                    color={statusColors[user.status]}
                    style={{ width: collapsed ? 10 : 20 ,
                            height: collapsed ? 10 : 20}}
                >
                    <Avatar
                        size={collapsed ? 40 : 80}
                        icon={<UserOutlined />}
                        src={user.avatar}
                    />
                </Badge>
                {!collapsed && (
                    <div style={{ textAlign: 'center', marginTop: 8 }}>
                        <Text strong style={{ fontSize: 16 }}>{user.name}</Text>
                    </div>
                )}
            </div>

            <Divider style={{ margin: '8px 0 16px' }} />

            <Menu
                mode="inline"
                selectedKeys={[activeMenu]}
                style={{ borderRight: 0 }}
                items={menuItems}
                onClick={({ key }) => setActiveMenu(key as ActiveMenuTypes)}
            />

            <div style={{
                position: 'absolute',
                bottom: 0,
                width: '100%',
                padding: '16px',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px'
            }}>
                <Button
                    icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                    onClick={() => setCollapsed(!collapsed)}
                    style={{ marginBottom: 8 }}
                    block
                >
                    {!collapsed && '折叠菜单' }
                </Button>

                <Tooltip title="退出登录" placement="right">
                    <Button
                        type="text"
                        danger
                        icon={<LogoutOutlined />}
                        style={{ margin: '8px 0' }}
                        block
                        onClick={() => logout()}
                    >
                        {!collapsed && '退出登录'}
                    </Button>
                </Tooltip>
            </div>
        </Sider>
    );
};

export default Sidebar;