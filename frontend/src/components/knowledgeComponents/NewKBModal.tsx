import { Input, Modal } from 'antd';

interface newKBModalProps {
    visible: boolean;
    onOk: () => void;
    onCancel: () => void;
    newKbName: string;
    setNewKbName: (name: string) => void;
    newKbDescription: string | undefined;
    setNewKbDescription: (description: string | undefined) => void;
}

const NewKBModal = (props: newKBModalProps) => {
    return (
        <Modal
            open={props.visible}
            title="新建知识库"
            onOk={() => props.onOk()}
            onCancel={() => props.onCancel()}
        >
            <div style={{ marginBottom: 8, fontWeight: 500 }}>知识库名称</div>
            <Input
                placeholder="输入知识库名称"
                value={props.newKbName}
                onChange={e => props.setNewKbName(e.target.value)}
                maxLength={20}
            />
            <div style={{ height: 16 }} /> {/* 空一行 */}
            <div style={{ marginBottom: 8, fontWeight: 500 }}>知识库描述</div>
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