import React from 'react';
import {Button, Flex, Typography, Upload, message} from 'antd';
import {Sender} from '@ant-design/x';
import {CloudUploadOutlined, LinkOutlined, DeleteOutlined} from '@ant-design/icons';
import type {UploadFile} from 'antd';

interface FileItem extends UploadFile {
    url: string;
}

interface ChatSenderProps {
    input: string;
    onInputChange: (val: string) => void;
    onSend: (val: string) => void;
    attachments: FileItem[];
    onFileUpload: (file: UploadFile) => boolean;
    onRemoveAttachment: (file: UploadFile) => void;
    open: boolean;
    setOpen: (o: boolean) => void;
    disabled?: boolean;
}

const ChatSender: React.FC<ChatSenderProps> = ({
                                                   input,
                                                   onInputChange,
                                                   onSend,
                                                   attachments,
                                                   onFileUpload,
                                                   onRemoveAttachment,
                                                   open,
                                                   setOpen,
                                                   disabled
                                               }) => {
    // 附件头部
    const SenderHeader = (
        <Sender.Header title="Upload Sample" open={open} onOpenChange={setOpen}>
            <Flex vertical align="center" gap="small" style={{marginBlock: 16}}>
                <CloudUploadOutlined style={{fontSize: '4em'}} />
                <Typography.Title level={5} style={{margin: 0}}>
                    Drag file here (demo)
                </Typography.Title>
                <Typography.Text type="secondary">
                    支持 pdf, doc, xlsx, ppt, txt, image 类型
                </Typography.Text>
                <Button
                    onClick={() => message.info('Mock select file')}
                >
                    Select File
                </Button>
            </Flex>
        </Sender.Header>
    );

    return (
        <Sender
            prefix={
                <Button
                    type="text"
                    icon={<LinkOutlined />}
                    onClick={() => setOpen(!open)}
                />
            }
            header={SenderHeader}
            value={input}
            onChange={onInputChange}
            onSubmit={onSend}
            disabled={disabled}
            placeholder="输入消息，按Enter发送..."
            style={{width: '100%', borderRadius: '0'}}
        >
            {/*/!* 附件展示与上传 *!/*/}
            {/*<Upload*/}
            {/*    showUploadList={false}*/}
            {/*    customRequest={() => false}*/}
            {/*    beforeUpload={onFileUpload}*/}
            {/*    fileList={attachments}*/}
            {/*/>*/}
            {/*{attachments.length > 0 &&*/}
            {/*    attachments.map(file => (*/}
            {/*        <div key={file.uid} style={{marginTop: 4}}>*/}
            {/*<span>*/}
            {/*  <CloudUploadOutlined />*/}
            {/*    {file.name}*/}
            {/*</span>*/}
            {/*            <Button*/}
            {/*                size="small"*/}
            {/*                icon={<DeleteOutlined />}*/}
            {/*                onClick={() => onRemoveAttachment(file)}*/}
            {/*                style={{marginLeft: 8}}*/}
            {/*                type="text"*/}
            {/*                danger*/}
            {/*            />*/}
            {/*        </div>*/}
            {/*    ))*/}
            {/*}*/}
        </Sender>
    );
};

export default ChatSender;