import {Input, Modal} from "antd";
import React from "react";

interface TopicEditModalProps {
    open: boolean,
    confirmLoading: boolean,
    value: string,
    onValueChange: any
    handleOk: any
    handleCancel: any
}

const TopicEditModal: React.FC<TopicEditModalProps> = (
    {open, confirmLoading, value, onValueChange, handleOk, handleCancel}
) => {

    return (
        <>
            <Modal
                title="修改标题名称"
                open={open}
                onOk={handleOk}
                confirmLoading={confirmLoading}
                onCancel={handleCancel}
            >
                <Input placeholder="请输入新的标题" value={value}
                       onChange={(e) => onValueChange(e.target.value)}
                       style={{margin: "10px 0px"}}
                />
            </Modal>
        </>)
}

export default TopicEditModal;