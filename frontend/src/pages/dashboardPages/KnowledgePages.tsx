import React, { useEffect, useState } from 'react';
import { Modal, message, type UploadProps } from 'antd';
import { InboxOutlined } from '@ant-design/icons';
import { addKnowledgeBase, getKnowledgeBaseList, type KnowledgeBaseListResponse, type NewKnowledgeBaseRequest } from '../../api/KnowledgeBaseApi';
import Dragger from 'antd/es/upload/Dragger';
import { getFileListByKnowledgeBaseId, uploadFilesToKnowledgeBase, type FileListResponse } from '../../api/FileApi';
import KnowledgeBaseInfoModal from '../../components/knowledgeComponents/KnowledgeBaseInfoModal';
import KnowledgeBaseList from '../../components/knowledgeComponents/KnowledgeBaseList';
import NewKBModal from '../../components/knowledgeComponents/NewKBModal';
import FilePreviewList from '../../components/knowledgeComponents/FileList';

const KnowledgePages: React.FC = () => {
    // 知识库列表
    const [kbList, setKbList] = useState<KnowledgeBaseListResponse[]>([]);
    // 文件列表
    const [fileList, setFileList] = useState<FileListResponse[]>([]);
    // 当前选择的知识库Id
    const [selectedKbId, setSelectedKbId] = useState<string>('');
    // 控制知识库详情弹窗显示
    const [kbInfoModalVisible, setKbInfoModalVisible] = useState(false);
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

    // 显示知识库详情弹窗
    const handleShowKbInfo = (id: string) => {
        setSelectedKbId(id);
        setKbInfoModalVisible(true);
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

    return (
        <div style={{ display: 'flex', height: '100%' }}>
            {/* 左侧知识库列表 */}
            <KnowledgeBaseList
                kbList={kbList}
                selectedKbId={selectedKbId}
                setKbModalVisible={setKbModalVisible}
                handleKbSelect={handleKbSelect}
                handleShowKbInfo={handleShowKbInfo}
            ></KnowledgeBaseList>
            {/* 右侧文件格子列表 */}
            <FilePreviewList
                files={fileList}
                selectedKbId={selectedKbId}
                selectedFiles={selectedFiles}
                onFileCheck={handleFileCheck}
                onFilePreview={(file) => setPreviewFile(file)}
                setFileModalVisible={setFileModalVisible}
                handleBatchDelete={handleBatchDelete}
            ></FilePreviewList>
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
            <NewKBModal
                visible={kbModalVisible}
                onOk={handleAddKb}
                onCancel={() => setKbModalVisible(false)}
                newKbName={newKbName}
                setNewKbName={setNewKbName}
                newKbDescription={newKbDescription}
                setNewKbDescription={setNewKbDescription}
            />
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
            <KnowledgeBaseInfoModal
                open={kbInfoModalVisible}
                onClose={() => setKbInfoModalVisible(false)}
                kbInfo={kbList.find(kb => kb.knowledgeBaseId === selectedKbId) || null}
                currentUserRole={kbList.find(kb => kb.knowledgeBaseId === selectedKbId)?.role || null}
            // onConfigEditor={() => { }}
            // onConfigViewer={() => { }}
            // onDeleteKb={() => {}}
            // onChangeOwner={handleChangeOwner}
            />
        </div>
    );
};

export default KnowledgePages;