import { Input, Modal, Button } from 'antd';
import { BookOutlined, EditOutlined, FileTextOutlined } from '@ant-design/icons';
import React, { useState } from 'react';

interface NewKBModalProps {
    visible: boolean;
    onOk: () => Promise<void> | void;
    onCancel: () => void;
    newKbName: string;
    setNewKbName: (name: string) => void;
    newKbDescription: string | undefined;
    setNewKbDescription: (description: string | undefined) => void;
}

const NewKBModal = (props: NewKBModalProps) => {
    const [loading, setLoading] = useState(false);
    return (
        <Modal
            open={props.visible}
            title={<span><BookOutlined style={{ marginRight: 8 }} />新建知识库</span>}
            onCancel={() => props.onCancel()}
            footer={[
                <Button key="cancel" onClick={props.onCancel} disabled={loading}>取消</Button>,
                <Button key="ok" type="primary" loading={loading} onClick={async () => {
                    setLoading(true);
                    try {
                        await props.onOk();
                    } finally {
                        setLoading(false);
                    }
                }}>确定</Button>
            ]}
        >
            <div style={{ marginBottom: 8, fontWeight: 500, display: 'flex', alignItems: 'center' }}>
                <EditOutlined style={{ color: '#52c41a', marginRight: 6 }} />
                <span>知识库名称</span>
                <span style={{ color: 'red', marginLeft: 4, fontSize: 14 }}>*</span>
            </div>
            <Input
                placeholder="输入知识库名称"
                value={props.newKbName}
                onChange={e => props.setNewKbName(e.target.value)}
                maxLength={20}
            />
            <div style={{ height: 16 }} /> {/* 空一行 */}
            <div style={{ marginBottom: 8, fontWeight: 500, display: 'flex', alignItems: 'center' }}>
                <FileTextOutlined style={{ color: '#888', marginRight: 6 }} />
                <span>知识库描述</span>
            </div>
            <Input.TextArea
                placeholder="输入知识库描述（可选）"
                value={props.newKbDescription || ''}
                onChange={e => props.setNewKbDescription(e.target.value)}
                rows={3}
                maxLength={100}
                style={{ resize: 'none' }}
            />
        </Modal>
    );
}

export default NewKBModal;