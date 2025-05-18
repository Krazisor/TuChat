import React, {useEffect, useState} from 'react';
import {
    Card,
    Avatar,
    Typography,
    Form,
    Input,
    Button,
    Upload,
    message,
    Divider,
    Row,
    Col,
    Space
} from 'antd';
import {
    UserOutlined,
    MailOutlined,
    LockOutlined,
    CalendarOutlined,
    IdcardOutlined,
    UploadOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import {useAppSelector} from "../../stores/StoreHook.ts";
import {getUserBaseInfo} from "../../api/userApi.ts";

const { Title, Text } = Typography;

const UserProfile = () => {
    // 模拟用户数据
    const [userData, setUserData] = useState({
        userId: '123456',
        userName: 'user',
        avatar: '',
        password: '••••••••••',
        createTime: '2023-01-15T08:30:00',
        email: 'user@example.com'
    });

    const [form] = Form.useForm();
    const [editing, setEditing] = useState(false);
    const [loading, setLoading] = useState(false);
    const [changePassword, setChangePassword] = useState(false);
    const userSlice = useAppSelector(state => state.user)

    // 处理头像上传
    const handleAvatarChange = (info: { file: { status: string; response: { url: any; }; originFileObj: Blob | MediaSource; }; }) => {
        if (info.file.status === 'uploading') {
            return;
        }

        if (info.file.status === 'done') {
            // 在实际应用中，这里应该获取服务器返回的URL
            setUserData({
                ...userData,
                avatar: info.file.response.url || URL.createObjectURL(info.file.originFileObj)
            });
            message.success('头像上传成功');
        } else if (info.file.status === 'error') {
            message.error('头像上传失败');
        }
    };

    useEffect(() => {
        if (userSlice.userInfo !== null) {
            console.log(userSlice.userInfo.createTime)
            setUserData({
                ...userSlice.userInfo,
                password: '••••••••••',
            })
        }
    }, []);

    // 启用编辑模式
    const handleEdit = () => {
        setEditing(true);
        setChangePassword(false);
        form.setFieldsValue({
            userName: userData.userName,
            email: userData.email,
        });
    };

    // 取消编辑
    const handleCancel = () => {
        setEditing(false);
        setChangePassword(false);
        form.resetFields();
    };

    // 处理密码修改选择
    const handlePasswordChange = (checked: boolean | ((prevState: boolean) => boolean)) => {
        setChangePassword(checked);
        if (!checked) {
            form.setFieldsValue({
                oldPassword: '',
                newPassword: '',
                confirmPassword: '',
            });
        }
    };

    // 验证确认密码
    const validateConfirmPassword = (_: any, value: any) => {
        const newPassword = form.getFieldValue('newPassword');
        if (value && value !== newPassword) {
            return Promise.reject(new Error('两次输入的密码不一致!'));
        }
        return Promise.resolve();
    };

    // 保存用户信息
    const handleSave = async () => {
        try {
            setLoading(true);
            const values = await form.validateFields();

            // 模拟后端验证旧密码
            if (changePassword) {
                const oldPassword = values.oldPassword;
                // 这里应该是实际的API调用来验证旧密码
                // 模拟密码验证失败的情况
                const mockCorrectOldPassword = "123456"; // 假设这是正确的旧密码
                if (oldPassword !== mockCorrectOldPassword) {
                    message.error('旧密码不正确');
                    setLoading(false);
                    return;
                }
            }

            // 这里应该是API调用来更新用户信息
            setTimeout(() => {
                setUserData({
                    ...userData,
                    userName: values.userName,
                    // 只有在选择修改密码时才更新密码
                    password: changePassword ? '••••••••••' : userData.password,
                    email: values.email
                });

                setEditing(false);
                setChangePassword(false);
                setLoading(false);
                message.success('个人信息已更新');
            }, 1000);

        } catch (error) {
            console.error('Validation failed:', error);
            setLoading(false);
        }
    };

    // 日期格式化
    const formatDate = (dateString: string) => {
        return dayjs(dateString).format('YYYY年MM月DD日 HH:mm:ss');
    };

    return (
        <div style={{ maxWidth: '100%', margin: '0 auto', padding: '20px' }}>
            <Card
                variant={'outlined'}
                style={{ boxShadow: '0 1px 2px 0 rgba(0,0,0,0.03), 0 1px 6px -1px rgba(0,0,0,0.02), 0 2px 4px 0 rgba(0,0,0,0.02)' }}
            >
                <Title level={2} style={{ marginBottom: 24 }}>个人信息</Title>

                <Row gutter={[16, 16]} align="middle" style={{ marginBottom: 24 }}>
                    <Col xs={24} sm={8} md={6} style={{ textAlign: 'center' }}>
                        {editing ? (
                            <Upload
                                name="avatar"
                                listType="picture-card"
                                className="avatar-uploader"
                                showUploadList={false}
                                action="https://www.mocky.io/v2/5cc8019d300000980a055e76" // 替换为您的上传API
                                onChange={() => handleAvatarChange}
                            >
                                {userData.avatar ? (
                                    <Avatar
                                        size={100}
                                        src={userData.avatar}
                                    />
                                ) : (
                                    <div>
                                        <UploadOutlined />
                                        <div style={{ marginTop: 8 }}>上传头像</div>
                                    </div>
                                )}
                            </Upload>
                        ) : (
                            <Avatar
                                size={100}
                                src={userData.avatar}
                                icon={<UserOutlined />}
                            />
                        )}
                    </Col>

                    <Col xs={24} sm={16} md={18}>
                        <Form
                            form={form}
                            layout="vertical"
                            initialValues={{
                                userName: userData.userName,
                                email: userData.email
                            }}
                        >
                            {/* 用户ID - 不可编辑 */}
                            <Form.Item
                                label={<Space><IdcardOutlined /> 用户ID</Space>}
                            >
                                <Input
                                    value={userData.userId}
                                    disabled
                                    style={{ backgroundColor: '#f5f5f5' }}
                                />
                            </Form.Item>

                            {/* 用户名 */}
                            <Form.Item
                                name="userName"
                                label={<Space><UserOutlined /> 用户名</Space>}
                                rules={[
                                    { required: true, message: '请输入用户名' },
                                    { min: 3, message: '用户名至少包含3个字符' }
                                ]}
                            >
                                {editing ? (
                                    <Input placeholder="请输入用户名" />
                                ) : (
                                    <Input value={userData.userName} disabled={!editing} />
                                )}
                            </Form.Item>

                            {/* 密码区域 */}
                            {!editing ? (
                                <Form.Item
                                    label={<Space><LockOutlined /> 密码</Space>}
                                >
                                    <Input
                                        value={userData.password}
                                        disabled
                                    />
                                </Form.Item>
                            ) : (
                                <>
                                    <Form.Item label={<Space><LockOutlined /> 密码</Space>}>
                                        <Button
                                            type={changePassword ? "primary" : "default"}
                                            onClick={() => handlePasswordChange(!changePassword)}
                                        >
                                            {changePassword ? '取消修改密码' : '修改密码'}
                                        </Button>
                                    </Form.Item>

                                    {changePassword && (
                                        <>
                                            <Form.Item
                                                name="oldPassword"
                                                label="旧密码"
                                                rules={[
                                                    { required: true, message: '请输入旧密码' },
                                                ]}
                                            >
                                                <Input.Password placeholder="请输入旧密码" />
                                            </Form.Item>

                                            <Form.Item
                                                name="newPassword"
                                                label="新密码"
                                                rules={[
                                                    { required: true, message: '请输入新密码' },
                                                    { min: 6, message: '密码至少包含6个字符' }
                                                ]}
                                            >
                                                <Input.Password placeholder="请输入新密码" />
                                            </Form.Item>

                                            <Form.Item
                                                name="confirmPassword"
                                                label="确认新密码"
                                                dependencies={['newPassword']}
                                                rules={[
                                                    { required: true, message: '请确认新密码' },
                                                    { validator: validateConfirmPassword }
                                                ]}
                                            >
                                                <Input.Password placeholder="请再次输入新密码" />
                                            </Form.Item>
                                        </>
                                    )}
                                </>
                            )}

                            {/* 注册时间 - 不可编辑 */}
                            <Form.Item
                                label={<Space><CalendarOutlined /> 注册时间</Space>}
                            >
                                <Input
                                    value={formatDate(userData.createTime)}
                                    disabled
                                    style={{ backgroundColor: '#f5f5f5' }}
                                />
                            </Form.Item>

                            {/* 邮箱 */}
                            <Form.Item
                                name="email"
                                label={<Space><MailOutlined /> 邮箱</Space>}
                                rules={[
                                    { required: true, message: '请输入邮箱地址' },
                                    { type: 'email', message: '请输入有效的邮箱地址' }
                                ]}
                            >
                                {editing ? (
                                    <Input placeholder="请输入邮箱地址" />
                                ) : (
                                    <Input value={userData.email} disabled={!editing} />
                                )}
                            </Form.Item>

                            <Divider />

                            <Form.Item>
                                {editing ? (
                                    <Space>
                                        <Button
                                            type="primary"
                                            onClick={handleSave}
                                            loading={loading}
                                        >
                                            保存
                                        </Button>
                                        <Button onClick={handleCancel}>取消</Button>
                                    </Space>
                                ) : (
                                    <Button
                                        type="primary"
                                        onClick={handleEdit}
                                    >
                                        编辑信息
                                    </Button>
                                )}
                            </Form.Item>
                        </Form>
                    </Col>
                </Row>
            </Card>
        </div>
    );
};

export default UserProfile;