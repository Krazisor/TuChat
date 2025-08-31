import { Button, Card, Empty, List } from "antd";
import { PlusOutlined, EyeOutlined, MoreOutlined, UserOutlined, EditOutlined } from '@ant-design/icons';
import type { KnowledgeBaseListResponse } from "../../api/KnowledgeBaseApi";

interface KnowledgeBaseListProps {
    kbList: KnowledgeBaseListResponse[];
    selectedKbId: string;
    setKbModalVisible: (visible: boolean) => void;
    handleKbSelect: (kbId: string) => void;
    handleShowKbInfo: (kbId: string) => void;
}

const KnowledgeBaseList: React.FC<KnowledgeBaseListProps> = ({ kbList, selectedKbId, setKbModalVisible, handleKbSelect, handleShowKbInfo }) => {
    return (
        <>
            {/* 左侧知识库列表 */}
            <div style={{ width: 300, borderRight: '1px solid #eee', padding: 16, background: '#fafbfc' }}>
                <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: 500, fontSize: 16 }}>知识库</span>
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => setKbModalVisible(true)}>
                        新建
                    </Button>
                </div>
                <List
                    dataSource={kbList}
                    renderItem={kb => (
                        <List.Item key={kb.knowledgeBaseId} style={{ padding: '8px 0' }}>
                            <Card
                                hoverable
                                onClick={() => handleKbSelect(kb.knowledgeBaseId)}
                                style={{
                                    borderRadius: 12,
                                    boxShadow: kb.knowledgeBaseId === selectedKbId ? '0 0 0 2px #1677ff' : '0 1px 4px #eee',
                                    background: kb.knowledgeBaseId === selectedKbId ? '#e6f7ff' : '#fff',
                                    cursor: 'pointer',
                                    transition: 'box-shadow 0.2s',
                                    position: 'relative',
                                    width: '100%',
                                    margin: '0 3px',
                                }}
                                styles={{
                                    body: {
                                        padding: '16px',
                                    }
                                }}
                            >
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                    <span style={{ fontWeight: 500, fontSize: 16 }}>{kb.name}</span>
                                    <Button
                                        type="text"
                                        icon={<MoreOutlined style={{ fontSize: 18 }} />}
                                        style={{ padding: 0 }}
                                        onClick={e => {
                                            handleShowKbInfo(kb.knowledgeBaseId);
                                        }}
                                    />
                                </div>
                                <div style={{ display: 'flex', alignItems: 'center', marginTop: 8 }}>
                                    {kb.role === 'OWNER' && (
                                        <UserOutlined style={{ color: '#1677ff', marginRight: 8 }} title="拥有者" />
                                    )}
                                    {kb.role === 'EDITOR' && (
                                        <EditOutlined style={{ color: '#52c41a', marginRight: 8 }} title="编辑者" />
                                    )}
                                    {kb.role === 'VIEWER' && (
                                        <EyeOutlined style={{ color: '#faad14', marginRight: 8 }} title="只读者" />
                                    )}
                                    <span
                                        style={{
                                            fontSize: 13,
                                            color: '#888',
                                            WebkitLineClamp: 2,
                                            WebkitBoxOrient: 'vertical',
                                            overflow: 'hidden',
                                            textOverflow: 'ellipsis',
                                            cursor: kb.description ? 'pointer' : 'default',
                                            lineHeight: '20px',
                                            height: (kb.description && kb.description.length <= 15) || !kb.description ? '20px' : '40px',
                                            display: kb.description && kb.description.length <= 15 ? 'flex' : '-webkit-box',
                                        }}
                                        title={kb.description || ''}
                                    >
                                        {kb.description || '无描述'}
                                    </span>
                                </div>
                            </Card>
                        </List.Item>
                    )}
                    locale={{ emptyText: <Empty description="暂无知识库" style={{ marginTop: 50 }} /> }}
                    style={{
                        maxHeight: 'calc(100vh - 200px)',
                        overflowY: 'auto'
                    }}
                />
            </div>
        </>
    );
};

export default KnowledgeBaseList;
