import React, {memo, type RefObject} from 'react';
import {Avatar, type UploadFile} from 'antd';
import {Empty} from 'antd';
import {Bubble} from '@ant-design/x';
import {Markdown, type MarkdownProps, ThemeProvider} from '@lobehub/ui';
import {useAppSelector} from "../../stores/StoreHook.ts";
import {RobotOutlined, UserOutlined, RobotFilled} from "@ant-design/icons";

export interface Message {
    id: string;
    content: string;
    role: 'user' | 'assistant';
    timestamp: number;
    attachments?: UploadFile[];
}

interface FileItem extends UploadFile {
    url: string;
}

interface ChatMessageListProps {
    messages: Message[];
    messagesEndRef: RefObject<HTMLDivElement>;
}

const options: MarkdownProps | any = {
    allowHtml: true,
    fontSize: 15,
    fullFeaturedCodeBlock: true,
    headerMultiple: 1,
    lineHeight: 1.5,
    marginMultiple: 1.5,
}

// 微软风格配色
const colors = {
    blue: '#0078d4',
    green: '#107c10',
    orange: '#d83b01',
    lightBlue: '#deecf9',
    lightGreen: '#e8f1e8'
};

const ChatMessageList: React.FC<ChatMessageListProps> = ({messages, messagesEndRef}) => {
    const userInfo = useAppSelector(state => state.user.userInfo)
    return (
        <div style={{flex: 1, padding: '20px', overflow: 'auto', backgroundColor: '#f8f8f8'}}>
            {messages && messages.length > 0 ? (
                <>
                    <Bubble.List
                        autoScroll={false}
                        items={messages.map((message) => ({
                            key: message.id,
                            role: message.role,
                            avatar: message.role === 'user' ? (<Avatar
                                size={32}
                                icon={<UserOutlined/>}
                                src={userInfo?.avatar}
                            />) : (<Avatar
                                size={32}
                                icon={<RobotOutlined style={{
                                    fontSize: '24px',
                                    color: 'black',
                                    transform: 'rotate(-10deg)'
                                }}/>}
                            />),
                            content: (
                                <ThemeProvider
                                    theme={{
                                        token: {
                                            fontSize: 14,
                                            fontFamilyCode:
                                                'Hack, ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, "Liberation Mono", monospace, "HarmonyOS Sans SC", "PingFang SC", "Hiragino Sans GB", "Microsoft Yahei UI", "Microsoft Yahei", "Source Han Sans CN", sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", "Apple Color Emoji", "Twemoji Mozilla", "Noto Color Emoji", "Android Emoji"',
                                        },
                                    }}
                                >
                                    <Markdown
                                        {...options}
                                        animated={true}
                                        children={message.content}/>
                                    {/*/!* 附件展示（如果有） *!/*/}
                                    {/*{message.attachments && message.attachments.length > 0 && (*/}
                                    {/*    <div style={{marginTop: '10px'}}>*/}
                                    {/*        {message.attachments.map((file) => (*/}
                                    {/*            <div key={file.uid} style={{margin: '5px 0'}}>*/}
                                    {/*                <FileTextOutlined style={{marginRight: '8px'}} />*/}
                                    {/*                <a*/}
                                    {/*                    href={(file as FileItem).url}*/}
                                    {/*                    target="_blank"*/}
                                    {/*                    rel="noopener noreferrer"*/}
                                    {/*                >*/}
                                    {/*                    {file.name}*/}
                                    {/*                </a>*/}
                                    {/*            </div>*/}
                                    {/*        ))}*/}
                                    {/*    </div>*/}
                                    {/*)}*/}
                                </ThemeProvider>
                            ),
                            loading: !message.content,
                            placement: (message.role === 'user' ? 'end' : 'start') as 'end' | 'start',
                            styles: {
                                content: {
                                    minHeight: '30px'
                                }
                            }
                        }))}
                    />
                    <div ref={messagesEndRef}/>
                </>
            ) : (
                <Empty description="选择一个话题开始对话" style={{top: '40%', position: 'relative'}}/>
            )}
        </div>
    )
};

export default memo(ChatMessageList);