import React, { useEffect, useState } from 'react';
import { Button, Modal, Input, List, Checkbox, message, Card, Row, Col, Empty, type UploadProps } from 'antd';
import { PlusOutlined, DeleteOutlined, EyeOutlined, MoreOutlined, UserOutlined, EditOutlined, InboxOutlined } from '@ant-design/icons';
import { addKnowledgeBase, getKnowledgeBaseList, type KnowledgeBaseListResponse, type NewKnowledgeBaseRequest } from '../../api/KnowledgeBaseApi';
import Dragger from 'antd/es/upload/Dragger';
import { getFileListByKnowledgeBaseId, uploadFilesToKnowledgeBase, type FileListResponse } from '../../api/FileApi';
import KnowledgeBaseInfoModal from '../../components/knowledgeComponents/KnowledgeBaseInfoModal';

// const initialFiles: FileListResponse[] = [
//     { id: 'f1', name: 'API说明.pdf', kbId: '1' },
//     { id: 'f2', name: '用户手册.docx', kbId: '1' },
//     { id: 'f3', name: '架构图.png', kbId: '2' },
// ];

const KnowledgePages: React.FC = () => {
    // 知识库列表
    const [kbList, setKbList] = useState<KnowledgeBaseListResponse[]>([]);
    // 文件列表
    const [fileList, setFileList] = useState<FileListResponse[]>([]);
    // 当前选择的知识库Id
    const [selectedKbId, setSelectedKbId] = useState<string>('');
    // 当前选择的文件Id
    const [selectedFiles, setSelectedFiles] = useState<string[]>([]);
    // 新建知识库弹窗控制开关
    const [kbModalVisible, setKbModalVisible] = useState(false);
    const [fileModalVisible, setFileModalVisible] = useState(false);
    const [previewFile, setPreviewFile] = useState<FileListResponse | null>(null);
    const [newKbName, setNewKbName] = useState('');
    const [newKbDescription, setNewKbDescription] = useState<string | null>(null);

    // 获取知识库列表
    const fetchKbList = async () => {
        const data = await getKnowledgeBaseList();
        if (data) {
            setKbList(data);
            if (data.length > 0) {
                setSelectedKbId(data[0].knowledgeBaseId);
            }
        } else {
            message.error('获取知识库列表失败');
        }
    }

    // 获取文件列表
    const fetchFileList = async (knowledgeBaseId: string) => {
        const data = await getFileListByKnowledgeBaseId(knowledgeBaseId);
        if (data) {
            setFileList(data);
        } else {
            message.error('获取文件列表失败');
        }
    }

    // 监听知识库Id变化获取文件列表
    useEffect(() => {
        if (selectedKbId) {
            fetchFileList(selectedKbId);
        }
    }, [selectedKbId]);

    // 在页面加载之初自动获取知识库列表
    useEffect(() => {
        fetchKbList();
    }, []);

    const handleKbSelect = (id: string) => {
        setSelectedKbId(id);
        setSelectedFiles([]);
    };

    const handleFileCheck = (id: string, checked: boolean) => {
        setSelectedFiles(checked ? [...selectedFiles, id] : selectedFiles.filter(fid => fid !== id));
    };

    const handleBatchDelete = () => {
        if (selectedFiles.length === 0) {
            message.warning('请选择要删除的文件');
            return;
        }
        setFileList(fileList.filter(f => !selectedFiles.includes(f.fileId)));
        setSelectedFiles([]);
        message.success('删除成功');
    };

    const handleAddKb = async () => {
        if (!newKbName.trim()) return;
        const newKb: NewKnowledgeBaseRequest = {
            name: newKbName,
            description: newKbDescription || '',
        };
        const data = await addKnowledgeBase(newKb)
        if (data !== null) {
            setNewKbName('');
            setKbModalVisible(false);
            fetchKbList();
            message.success('知识库创建成功');
        } else {
            message.error('知识库重名啦，换一个吧')
        }
    };

    // 新建文件弹窗确认按钮操作逻辑
    const handleAddFile = () => {
        // if (!newFileName.trim()) return;
        // setFileList([...fileList, { id: Date.now().toString(), name: newFileName, kbId: selectedKbId }]);
        // setNewFileName('');
        // setFileModalVisible(false);
        // message.success('文件创建成功');
    };

    const props: UploadProps = {
        name: 'file',
        multiple: true,
        customRequest: async (options) => {
            // 只允许md文件上传
            console.log(options)
            const files = Array.isArray(options.file) ? options.file : [options.file];
            const mdFiles = files.filter(f => f.name.endsWith('.md'));
            if (mdFiles.length === 0) {
                message.error('仅支持上传md文件');
                return;
            }
            if (!selectedKbId) {
                message.error('请先选择知识库');
                return;
            }
            try {
                const res = await uploadFilesToKnowledgeBase(mdFiles, selectedKbId);
                options.onSuccess && options.onSuccess(res, options.file);
            } catch (err) {
                options.onError && options.onError(err);
            }
        },
        onChange(info) {
            // 这里可以根据需要处理 fileList
            if (info.file.status === 'done') {
                message.success(`${info.file.name} 上传成功。`);
            } else if (info.file.status === 'error') {
                message.error(`${info.file.name} 文件上传失败。`);
            }
        },
        onDrop(e) {
            console.log('Dropped files', e.dataTransfer.files);
        },
    };

    const filesOfSelectedKb = fileList.filter(f => f.knowledgeBaseId === selectedKbId);

    return (
        <div style={{ display: 'flex', height: '100%' }}>
            {/* 左侧知识库列表 */}
            <div style={{ width: 300, borderRight: '1px solid #eee', padding: 24, background: '#fafbfc' }}>
                <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: 500, fontSize: 18 }}>知识库</span>
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => setKbModalVisible(true)}>
                        新建
                    </Button>
                </div>
                <Row gutter={[16, 16]}>
                    {kbList.length > 0 ?
                        (kbList.map(kb => (
                            <Col key={kb.knowledgeBaseId} span={24}>
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
                                    }}
                                >
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <span style={{ fontWeight: 500, fontSize: 16 }}>{kb.name}</span>
                                        <Button
                                            type="text"
                                            icon={<MoreOutlined style={{ fontSize: 18 }} />}
                                            style={{ padding: 0 }}
                                            onClick={e => {
                                                e.stopPropagation();
                                                // 这里可以添加弹出菜单等逻辑
                                            }}
                                        />
                                    </div>
                                    <div style={{ display: 'flex', alignItems: 'center', marginTop: 8 }}>
                                        {/* 身份图标 */}
                                        {kb.role === 'owner' && (
                                            <UserOutlined style={{ color: '#1677ff', marginRight: 8 }} title="拥有者" />
                                        )}
                                        {kb.role === 'editor' && (
                                            <EditOutlined style={{ color: '#52c41a', marginRight: 8 }} title="编辑者" />
                                        )}
                                        {kb.role === 'viewer' && (
                                            <EyeOutlined style={{ color: '#faad14', marginRight: 8 }} title="只读者" />
                                        )}
                                        {/* 描述省略显示，鼠标悬停显示全部 */}
                                        <span
                                            style={{
                                                fontSize: 13,
                                                color: '#888',
                                                WebkitLineClamp: 2, // 限制显示两行
                                                WebkitBoxOrient: 'vertical',
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis',
                                                maxWidth: 180,
                                                cursor: kb.description ? 'pointer' : 'default',
                                                lineHeight: '20px',
                                                height: (kb.description && kb.description.length <= 13) || !kb.description ? '20px' : '40px',
                                                alignItems: kb.description && kb.description.length <= 13 ? 'center' : 'normal',
                                                display: kb.description && kb.description.length <= 13 ? 'flex' : '-webkit-box',
                                                justifyContent: kb.description && kb.description.length <= 13 ? 'center' : 'normal',
                                            }}
                                            title={kb.description || ''}
                                        >
                                            {kb.description || '无描述'}
                                        </span>
                                    </div>
                                </Card>
                            </Col>
                        ))) :
                        (<Col span={24}
                            style={{
                                display: 'flex',
                                justifyContent: 'center',
                                alignItems: 'center',
                                height: '220px', // 设置高度保证垂直居中
                                minHeight: 220,
                            }} >
                            <Empty description="暂无知识库" />
                        </Col>)}
                </Row>
            </div>
            {/* 右侧文件格子列表 */}
            <div style={{ flex: 1, padding: 24, background: '#f9f9f9' }}>
                <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
                    <div>
                        <Button
                            type="primary"
                            icon={<PlusOutlined />}
                            onClick={() => setFileModalVisible(true)}
                            style={{ marginRight: 8 }}
                            disabled={!selectedKbId}
                        >
                            新建文件
                        </Button>
                        <Button
                            danger
                            icon={<DeleteOutlined />}
                            onClick={handleBatchDelete}
                            disabled={selectedFiles.length === 0 || !selectedKbId}
                        >
                            批量删除
                        </Button>
                    </div>
                    <span style={{ fontWeight: 500 }}>
                        {kbList.find(kb => kb.knowledgeBaseId === selectedKbId)?.name || ''}
                    </span>
                </div>
                <Row gutter={[16, 16]}>
                    {filesOfSelectedKb.map(file => (
                        <Col key={file.fileId} xs={24} sm={12} md={8} lg={6}>
                            <Card
                                hoverable
                                actions={[
                                    <EyeOutlined key="preview" onClick={() => setPreviewFile(file)} />,
                                    <Checkbox
                                        checked={selectedFiles.includes(file.fileId)}
                                        onChange={e => handleFileCheck(file.fileId, e.target.checked)}
                                    />,
                                ]}
                                style={{ borderRadius: 8 }}
                            >
                                <Card.Meta title={file.fileName} />
                            </Card>
                        </Col>
                    ))}
                </Row>
                {/* 预览文件弹窗 */}
                <Modal
                    open={!!previewFile}
                    title="文件预览"
                    footer={null}
                    onCancel={() => setPreviewFile(null)}
                >
                    <div>
                        <strong>文件名：</strong> {previewFile?.fileName}
                        <div style={{ marginTop: 16, color: '#888' }}>（此处可集成文件预览组件）</div>
                    </div>
                </Modal>
                {/* 新建知识库弹窗 */}
                <Modal
                    open={kbModalVisible}
                    title="新建知识库"
                    onOk={() => handleAddKb()}
                    onCancel={() => setKbModalVisible(false)}
                >
                    <div style={{ marginBottom: 8, fontWeight: 500 }}>知识库名称</div>
                    <Input
                        placeholder="输入知识库名称"
                        value={newKbName}
                        onChange={e => setNewKbName(e.target.value)}
                        maxLength={20}
                    />
                    <div style={{ height: 16 }} /> {/* 空一行 */}
                    <div style={{ marginBottom: 8, fontWeight: 500 }}>知识库描述</div>
                    <Input.TextArea
                        placeholder="输入知识库描述（可选）"
                        value={newKbDescription || ''}
                        onChange={e => setNewKbDescription(e.target.value)}
                        rows={3}
                        maxLength={100}
                        style={{ resize: 'none' }}
                    />
                </Modal>
                {/* 新建文件弹窗 */}
                <Modal
                    open={fileModalVisible}
                    title="新建文件"
                    onOk={handleAddFile}
                    onCancel={() => setFileModalVisible(false)}
                >
                    <Dragger {...props}>
                        <p className="ant-upload-drag-icon">
                            <InboxOutlined />
                        </p>
                        <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
                        <p className="ant-upload-hint">
                            仅支持上传md文件
                        </p>
                    </Dragger>
                </Modal>
            </div>
            <KnowledgeBaseInfoModal
                open={!!selectedKbId}
                onClose={() => setSelectedKbId(null)}
                kbInfo={kbList.find(kb => kb.knowledgeBaseId === selectedKbId) || null}
                currentUserRole={kbList.find(kb => kb.knowledgeBaseId === selectedKbId)?.role || null}
                onConfigEditor={() => { }}
                onConfigViewer={() => { }}
            // onDeleteKb={() => {}}
            // onChangeOwner={handleChangeOwner}
            />
        </div>
    );
};

export default KnowledgePages;