import React from 'react';
import type {GetRef} from 'antd';
import {Button} from 'antd';
import {Attachments, Sender} from '@ant-design/x';
import {CloudUploadOutlined, LinkOutlined} from '@ant-design/icons';
import type {Attachment} from "@ant-design/x/es/attachments";

interface ChatSenderProps {
    input: string;
    onInputChange: (val: string) => void;
    onSend: (val: string) => void;
    attachments: Attachment[];
    setAttachments: (o: Attachment[]) => void
    loading: boolean
    open: boolean;
    setOpen: (o: boolean) => void;
    disabled?: boolean;
}

const ChatSender: React.FC<ChatSenderProps> = ({
                                                   input,
                                                   onInputChange,
                                                   onSend,
                                                   attachments,
                                                   setAttachments,
                                                   open,
                                                   loading,
                                                   setOpen,
                                                   disabled
                                               }) => {

    const senderRef = React.useRef<GetRef<typeof Sender>>(null);

    const SenderHeader = (
        <Sender.Header
            title="文件上传"
            open={open}
            onOpenChange={setOpen}
            styles={{
                content: {
                    padding: "5px",
                },
            }}
        >
            <Attachments
                // Mock not real upload file
                beforeUpload={() => false}
                items={attachments}
                onChange={({ fileList }) => setAttachments(fileList)}
                placeholder={(type) =>
                    type === 'drop'
                        ? {
                            title: '将文件拖到这里',
                        }
                        : {
                            icon: <CloudUploadOutlined />,
                            title: '上传文件',
                            description: '点击或拖拽文件到此处完成上传',
                        }
                }
                getDropContainer={() => senderRef.current?.nativeElement}
            />
        </Sender.Header>
    );

    return (
        <Sender
            prefix={
                <Button
                    type="text"
                    icon={<LinkOutlined/>}
                    onClick={() => setOpen(!open)}
                />
            }
            header={SenderHeader}
            value={input}
            onChange={onInputChange}
            onSubmit={onSend}
            disabled={disabled}
            loading={loading}
            actions={(_, info) => {
                const {SendButton, LoadingButton} = info.components;
                if (loading === true) {
                    return (
                        <LoadingButton/>
                    );
                }
                return <SendButton style={{borderRadius: 12}}/>
            }}
            placeholder="输入消息，按Enter发送..."
            style={{width: '100%', borderRadius: '0'}}
        />
    );
};

export default ChatSender;