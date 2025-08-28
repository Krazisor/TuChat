import { Button, Card, Checkbox, Col, Empty, Row } from "antd"
import {
    DeleteOutlined,
    EyeOutlined,
    PlusOutlined
} from '@ant-design/icons';
import type { FileListResponse } from "../../api/FileApi";
import type { KnowledgeBaseListResponse } from "../../api/KnowledgeBaseApi";

interface fileListProps {
    files: FileListResponse[];
    kbList: KnowledgeBaseListResponse[];
    selectedKbId: string;
    selectedFiles: string[];
    onFileCheck: (id: string, checked: boolean) => void;
    onFilePreview: (file: FileListResponse) => void;
    setFileModalVisible: (visible: boolean) => void;
    handleBatchDelete: () => void;
}

const FilePreviewList = ({ files, selectedKbId, selectedFiles, onFileCheck, onFilePreview, setFileModalVisible, handleBatchDelete }) => {
    return (
        <div style={{ flex: 1, padding: 16, background: '#f9f9f9' }}>
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
            </div>
            <Row gutter={[16, 16]}>
                {files.length === 0 ? (
                    <Col span={24} style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 220 }}>
                        <Empty description="暂无文件" />
                    </Col>
                ) : (
                    files.map(file => (
                        <Col key={file.fileId} xs={24} sm={12} md={8} lg={6}>
                            <Card
                                hoverable
                                actions={[
                                    <EyeOutlined key="preview" onClick={() => onFilePreview(file)} />,
                                    <Checkbox
                                        checked={selectedFiles.includes(file.fileId)}
                                        onChange={e => onFileCheck(file.fileId, e.target.checked)}
                                    />,
                                ]}
                                style={{ borderRadius: 8 }}
                            >
                                <Card.Meta title={file.fileName} />
                            </Card>
                        </Col>
                    ))
                )}
            </Row>
        </div>
    )
}

export default FilePreviewList;