import React, { useEffect, useState } from 'react';
import { Modal, Button, Input, Select, message, Popconfirm, Divider, Tooltip } from 'antd';
import { UserOutlined, EditOutlined, EyeOutlined, DeleteOutlined, LockOutlined, CheckCircleOutlined, CloseCircleOutlined, CalendarOutlined } from '@ant-design/icons';
import { getKnowledgeBaseMembers, type KnowledgeBaseListResponse, type KnowledgeBaseMember, type KnowledgeBaseReOwnerRequest, type KnowledgeUpdateRequest, type RoleEnum } from '../../api/KnowledgeBaseApi';

interface KnowledgeBaseInfoModalProps {
    visible: boolean;
    setVisible: (visible: boolean) => void;
    kbInfo: KnowledgeBaseListResponse | null;
    setKbInfo: (kbInfo: KnowledgeBaseListResponse) => void;
    onDeleteKb?: (kbId: string) => Promise<boolean | null>;
    onChangeOwner?: (request: KnowledgeBaseReOwnerRequest) => Promise<boolean | null>;
    onChangeInfo?: (request: KnowledgeUpdateRequest) => Promise<boolean | null>;
    fetchKbList?: () => Promise<void>;
}

const roleOptions = [
    { label: <><EditOutlined style={{ color: '#52c41a' }} /> 编辑者</>, value: 'EDITOR' },
    { label: <><EyeOutlined style={{ color: '#faad14' }} /> 只读者</>, value: 'VIEWER' },
    { label: <><CloseCircleOutlined style={{ color: '#888' }} /> 无权限</>, value: 'NONE' },
];

const KnowledgeBaseInfoModal: React.FC<KnowledgeBaseInfoModalProps> = ({
    visible,
    setVisible,
    kbInfo,
    setKbInfo,
    onDeleteKb,
    onChangeOwner,
    onChangeInfo,
    fetchKbList
}) => {
    const [deleteConfirm, setDeleteConfirm] = useState('');
    const [ownerChangeRole, setOwnerChangeRole] = useState<RoleEnum>('EDITOR');
    const [isChangingOwner, setIsChangingOwner] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [isUpdating, setIsUpdating] = useState(false);
    const [isChangingOwnerLoading, setIsChangingOwnerLoading] = useState(false);
    const [members, setMembers] = useState<KnowledgeBaseMember>({ EDITOR: [], VIEWER: [] });

    // 获取知识库成员
    useEffect(() => {
        const fetchMembers = async () => {
            if (kbInfo && visible && kbInfo.role === 'OWNER') {
                const ans = await getKnowledgeBaseMembers(kbInfo.knowledgeBaseId);
                setMembers(ans || { EDITOR: [], VIEWER: [] });
            }
        };
        fetchMembers();
    }, [visible, kbInfo]);

    // 时间格式美化
    const formatTime = (iso: string) => {
        if (!iso) return '';
        const d = new Date(iso);
        return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
    };

    // 删除知识库
    const handleDelete = async () => {
        if (deleteConfirm !== kbInfo.name) {
            message.error('请输入正确的知识库名称以确认删除');
            return;
        }
        setIsDeleting(true);
        try {
            if (onDeleteKb) {
                await onDeleteKb(kbInfo.knowledgeBaseId);
                setDeleteConfirm('');
                setVisible(false);
                fetchKbList && fetchKbList();
            }
        } finally {
            setIsDeleting(false);
        }
    };

    const handleChangeOwner = async () => {
        setIsChangingOwnerLoading(true);
        try {
            if (onChangeOwner) {
                await onChangeOwner({
                    knowledgeBaseId: kbInfo.knowledgeBaseId,
                    newOwnerId: '',
                    newRole: ownerChangeRole
                });
                setIsChangingOwner(false);
                setVisible(false);
            }
        } finally {
            setIsChangingOwnerLoading(false);
        }
    };

    if (!kbInfo) return null;

    return (
        <Modal
            open={visible}
            title={<span><LockOutlined style={{ marginRight: 8 }} />知识库详情</span>}
            footer={null}
            onCancel={() => setVisible(false)}
            width={640}
        >
            {/* 名称和创建时间同一行 */}
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: 20, gap: 16 }}>
                <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 500, fontSize: 15, marginBottom: 8 }}>
                        <UserOutlined style={{ color: '#1677ff', marginRight: 6 }} />知识库名称
                    </div>
                    <Input value={kbInfo.name} style={{ fontWeight: 500 }} onChange={e => setKbInfo({ ...kbInfo, name: e.target.value })} />
                </div>
                <div style={{ width: 220 }}>
                    <div style={{ fontWeight: 500, fontSize: 15, marginBottom: 8 }}>
                        <CalendarOutlined style={{ color: '#faad14', marginRight: 6 }} />创建时间
                    </div>
                    <Input value={formatTime(kbInfo.createTime)} disabled style={{ background: '#f5f5f5' }} />
                </div>
            </div>
            {/* 描述单独一行 */}
            <div style={{ marginBottom: 20 }}>
                <div style={{ fontWeight: 500, fontSize: 15, marginBottom: 8 }}>
                    <EditOutlined style={{ color: '#52c41a', marginRight: 6 }} />知识库描述
                </div>
                <Input.TextArea value={kbInfo.description || ''} autoSize onChange={e => setKbInfo({ ...kbInfo, description: e.target.value })} />
            </div>
            <Divider style={{ margin: '16px 0' }} />
            {/* 身份单独一行 */}
            <div style={{ marginBottom: 20 }}>
                <div style={{ fontWeight: 500, fontSize: 15, marginBottom: 8 }}>
                    <LockOutlined style={{ color: '#1677ff', marginRight: 6 }} />我的身份
                </div>
                <Input
                    value={kbInfo.role === 'OWNER' ? '拥有者' : kbInfo.role === 'EDITOR' ? '编辑者' : '只读者'}
                    disabled
                    style={{ background: '#f5f5f5', fontWeight: 500, color: '#1677ff' }}
                />
            </div>
            {kbInfo.role === 'OWNER' && (
                <>
                    <Divider style={{ margin: '16px 0' }} />
                    {/* 角色配置：编辑者名单和预览者名单各一行 */}
                    <div style={{ marginBottom: 20 }}>
                        <div style={{ fontWeight: 500, fontSize: 15, marginBottom: 8 }}>
                            <EditOutlined style={{ color: '#52c41a', marginRight: 6 }} />编辑者名单
                        </div>
                        <Select
                            mode="tags"
                            style={{ width: '100%' }}
                            placeholder="搜索并添加编辑者"
                            value={members.EDITOR}
                            disabled
                            options={[]}
                        />
                    </div>
                    <div style={{ marginBottom: 20 }}>
                        <div style={{ fontWeight: 500, fontSize: 15, marginBottom: 8 }}>
                            <EyeOutlined style={{ color: '#faad14', marginRight: 6 }} />只读者名单
                        </div>
                        <Select
                            mode="tags"
                            style={{ width: '100%' }}
                            placeholder="搜索并添加预览者"
                            value={members.VIEWER}
                            disabled
                            options={[]}
                        />
                    </div>
                    {/* 知识库移交和删除按钮，放在底部操作区的上方 */}
                    <div style={{ display: 'flex', gap: 12, marginBottom: 12 }}>
                        <Button danger icon={<LockOutlined />} onClick={() => setIsChangingOwner(true)} style={{ flex: 1 }}>知识库移交</Button>
                        <Popconfirm
                            title={
                                <div>
                                    <div>输入知识库名称以确认删除：</div>
                                    <Input
                                        value={deleteConfirm}
                                        onChange={e => setDeleteConfirm(e.target.value)}
                                        placeholder="请输入知识库名称"
                                    />
                                </div>
                            }
                            onConfirm={handleDelete}
                            okText={<span>{isDeleting ? <span style={{ marginRight: 8 }}><span className="ant-btn-loading-icon" /></span> : null}确认删除</span>}
                            cancelText="取消"
                            disabled={isDeleting}
                        >
                            <Button danger icon={<DeleteOutlined />} style={{ flex: 1 }} loading={isDeleting}>删除知识库</Button>
                        </Popconfirm>
                    </div>
                    {/* 底部操作按钮区域 */}
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 32 }}>
                        <Button onClick={() => setVisible(false)}>取消</Button>
                        <Button type="primary" loading={isUpdating} onClick={async () => {
                            setIsUpdating(true);
                            try {
                                if (onChangeInfo) {
                                    const request: KnowledgeUpdateRequest = {
                                        knowledgeBaseId: kbInfo.knowledgeBaseId,
                                        name: kbInfo.name,
                                        description: kbInfo.description,
                                        editorList: members.EDITOR,
                                        viewerList: members.VIEWER
                                    };
                                    await onChangeInfo(request);
                                    message.success('设置已确认');
                                    setVisible(false);
                                }
                            } finally {
                                setIsUpdating(false);
                            }
                        }}>确定</Button>
                    </div>
                </>
            )}
            {/* 知识库移交弹窗 */}
            <Modal
                open={isChangingOwner}
                title={<span><LockOutlined style={{ marginRight: 8 }} />知识库移交</span>}
                onCancel={() => setIsChangingOwner(false)}
                onOk={handleChangeOwner}
                confirmLoading={isChangingOwnerLoading}
            >
                <div style={{ marginTop: 16 }}>新拥有者</div>
                <Input placeholder="请输入新拥有者用户名或ID" />
                <div style={{ marginTop: 16 }}>选择移交后你的身份：</div>
                <Select
                    style={{ width: '100%' }}
                    value={ownerChangeRole}
                    onChange={v => setOwnerChangeRole(v as RoleEnum)}
                    options={roleOptions}
                />
                <div style={{ marginTop: 16, color: 'red' }}>
                    移交后你将失去拥有者权限，降级为所选身份。
                </div>
            </Modal>
        </Modal>
    );
};

export default KnowledgeBaseInfoModal;