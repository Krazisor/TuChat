import React, {useEffect, useRef, useState} from 'react';
import type {UploadFile} from 'antd';
import {Button, Card, Divider, Empty, Flex, Input, message, Splitter, theme, Typography, Upload} from 'antd';
import {Bubble, Conversations, type ConversationsProps, Sender} from '@ant-design/x';
import {
    CloudUploadOutlined,
    DeleteOutlined,
    EditOutlined,
    FileTextOutlined,
    LinkOutlined,
    PlusOutlined,
    StarOutlined,
    StopOutlined
} from '@ant-design/icons';
import {addNewConversation, getConversationList} from "../../api/ConversationApi.ts";
import type {Conversation} from '@ant-design/x/es/conversations/interface';
import {getMessageListByConversationId} from "../../api/MessageApi.ts";
import {type AIRequestDTO, fetchAIResponseStream} from "../../api/FetchStream.ts";
import dayjs from "dayjs";
import {Markdown} from '@lobehub/ui';

const {Title, Text} = Typography;
const {Dragger} = Upload;

interface Message {
    id: string;
    content: string;
    role: 'user' | 'assistant';
    timestamp: number;
    attachments?: UploadFile[];
}

interface FileItem extends UploadFile {
    url: string;
}

// 主应用组件
const AIChatPages: React.FC = () => {
    // 当前对话的消息
    const [messages, setMessages] = useState<Message[]>([]);
    // 当前显示的会话ID
    const [activeTopic, setActiveTopic] = useState<string | undefined>(undefined);
    // 当前输入的内容
    const [input, setInput] = useState<string>('');
    // 当前是否在创建新会话状态
    const [isCreatingTopic, setIsCreatingTopic] = useState<boolean>(false);
    // 新会话的title
    const [newTopicTitle, setNewTopicTitle] = useState<string>('');
    // 当前输入的附件
    const [attachments, setAttachments] = useState<FileItem[]>([]);
    // 附件窗口是否展示
    const [open, setOpen] = useState(false);
    // 当前所有的会话
    const [conversation, setConversation] = useState<Conversation[]>([])
    const {token} = theme.useToken();
    const messagesEndRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const getConversations = async () => {
            const response = await getConversationList();
            if (response != null) {
                setConversation(
                    response.map(item => ({
                        key: item.conversationId,
                        label: item.title,
                        icon: <StarOutlined style={{color: '#bbbbbb'}}/>,
                        timestamp: Number(item.createTime)
                    }))
                )
            }
        }
        getConversations();
    }, []);

    // 滚动到对话末尾
    const scrollToBottom = (): void => {
        messagesEndRef.current?.scrollIntoView({behavior: 'smooth'});
    };
    useEffect(() => {
        scrollToBottom();
    }, [messages, activeTopic]);


    // 实现会话列表多功能菜单
    const menuConfig: ConversationsProps['menu'] = (conversation) => ({
        items: [
            {
                label: '编辑',
                key: 'edit',
                icon: <EditOutlined/>,
            },
            {
                label: '收藏',
                key: 'mark',
                icon: <StopOutlined/>,
                disabled: true,
            },
            {
                label: '删除',
                key: 'delete',
                icon: <DeleteOutlined/>,
                danger: true,
            },
        ],
        onClick: (menuInfo) => {
            menuInfo.domEvent.stopPropagation();
            message.info(`Click ${conversation.key} - ${menuInfo.key}`);
            if (menuInfo.key === 'edit') {

            }
        },
    });

    // 处理点击话题后获取对话逻辑
    const handleActiveChange = async (key: string) => {
        setActiveTopic(key);
        const response = await getMessageListByConversationId(key);
        const temp: Message[] = response.map(item => ({
            id: String(item.messageId),
            content: item.content,
            role: (item.role === 'user' || item.role === 'assistant')
                ? item.role as 'user' | 'assistant'
                : 'user',
            timestamp: dayjs(item.createTime).valueOf()
            // attachments: undefined
        }));
        setMessages(temp);
    }

    // 处理发送消息
    const handleSend = (value: string): void => {
        if (!value.trim() && attachments.length === 0) return;
        if (!activeTopic) return;

        const newUserMsg: Message = {
            id: `${activeTopic}-${Date.now()}`,
            content: value.trim(),
            role: 'user',
            timestamp: Date.now(),
            attachments: attachments.length > 0 ? [...attachments] : undefined
        };

        setMessages([...messages, newUserMsg]);

        const dto: AIRequestDTO = {
            conversationId: activeTopic,
            question: value,
            model: 'gpt-4.1'
        }

        // 清空输入和附件
        setInput('');
        setAttachments([]);

        setMessages(prevState => [...prevState, {
            id: `${activeTopic}+${Date.now()}`,
            content: '',
            role: 'assistant',
            timestamp: Date.now()
        }]);

        fetchAIResponseStream(
            dto,
            msg => {
                setMessages(prev => {
                    if (!prev.length) return prev; // 没有消息就不做操作
                    // 复制数组
                    const updated = [...prev];
                    // 只更新最后一个元素的 content 字段
                    updated[updated.length - 1] = {
                        ...updated[updated.length - 1],
                        content: updated[updated.length - 1].content + msg,
                    };
                    return updated;
                });
            },
            err => {
                console.log(err);
                message.error(err);
            }
        )
    };

    // 创建新话题
    const handleCreateTopic = async (): Promise<void> => {
        if (!isCreatingTopic) {
            setIsCreatingTopic(true);
            return;
        }
        if (!newTopicTitle.trim()) {
            setIsCreatingTopic(false);
            return;
        }
        let conversationId = await addNewConversation(newTopicTitle);
        let conversationList = await getConversationList();
        setConversation(
            conversationList!.map(item => ({
                key: item.conversationId,
                label: item.title,
                icon: <StarOutlined style={{color: '#bbbbbb'}}/>,
                timestamp: Number(item.createTime)
            }))
        )
        setActiveTopic(conversationId as string);
        await handleActiveChange(conversationId as string)
        setIsCreatingTopic(false);
        setNewTopicTitle('');
    };

    // 处理文件上传
    const handleFileUpload = (file: UploadFile): boolean => {
        const isAccepted = true; // 这里可以添加文件类型检查逻辑

        if (!isAccepted) {
            message.error('文件类型不支持');
            return false;
        }

        // 创建文件对象
        const newFile: FileItem = {
            ...file,
            uid: file.uid,
            name: file.name,
            status: 'done',
            url: URL.createObjectURL(file as any as Blob),
            type: file.type
        };

        setAttachments(prev => [...prev, newFile]);
        return false; // 阻止自动上传
    };

    // 移除附件
    const handleRemoveAttachment = (file: UploadFile): void => {
        setAttachments(prev => prev.filter(item => item.uid !== file.uid));
    };

    // 格式化时间
    const formatTime = (timestamp: number): string => {
        const date = new Date(timestamp);
        return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    };

    const SenderHeader = (
        <Sender.Header title="Upload Sample" open={open} onOpenChange={setOpen}>
            <Flex vertical align="center" gap="small" style={{marginBlock: token.paddingLG}}>
                <CloudUploadOutlined style={{fontSize: '4em'}}/>
                <Typography.Title level={5} style={{margin: 0}}>
                    Drag file here (just demo)
                </Typography.Title>
                <Typography.Text type="secondary">
                    Support pdf, doc, xlsx, ppt, txt, image file types
                </Typography.Text>
                <Button
                    onClick={() => {
                        message.info('Mock select file');
                    }}
                >
                    Select File
                </Button>
            </Flex>
        </Sender.Header>
    )

    return (
        <div style={{height: '100%', overflow: 'hidden'}}>
            <Splitter
                style={{height: '100%'}}
            >
                {/* 左侧对话区域 */}
                <Splitter.Panel defaultSize="85%" min="50%" max="85%">
                    <div style={{height: '100%', display: 'flex', flexDirection: 'column'}}>
                        <div style={{textAlign: 'center', background: '#f8f8f8'}}>
                            <Title
                                level={4}
                                style={{
                                    height: '30px',          // 高度24px
                                    lineHeight: '30px',      // 让文字垂直居中
                                    margin: 0,               // 去除默认外边距（可选）
                                    padding: '0 8px',        // 内边距让文字不顶边（可选）
                                    display: 'inline-block', // 根据内容自适应宽度（可选）
                                }}
                            >
                                {conversation.find(t => t.key === activeTopic)?.label}
                            </Title>
                        </div>
                        <Divider style={{margin: '0', background: '#eeeeee', height: '2px'}}/>
                        <div
                            style={{
                                flex: 1,
                                padding: '20px',
                                overflow: 'auto',
                                backgroundColor: '#f8f8f8'
                            }}
                        >
                            {activeTopic ? (
                                <div>
                                    {messages?.map((message) => (
                                        <Bubble
                                            key={message.id}
                                            content={(
                                                <Markdown
                                                    animated={true}
                                                    fontSize={14}
                                                    fullFeaturedCodeBlock={true}
                                                    children={message.content}
                                                    allowHtml={true}
                                                    style={{fontFamily: "sans-serif"}}
                                                    componentProps={{

                                                        highlight: {

                                                            style: {
                                                                fontFamily: 'serif',

                                                            }
                                                        },

                                                    }}
                                                />)}
                                            style={{marginBottom: '16px'}}
                                            placement={message.role === 'user' ? 'end' : 'start'}
                                            loading={!message.content}
                                        >
                                            {message.attachments && message.attachments.length > 0 && (
                                                <div style={{marginTop: '10px'}}>
                                                    {message.attachments.map((file) => (
                                                        <div key={file.uid} style={{margin: '5px 0'}}>
                                                            <FileTextOutlined style={{marginRight: '8px'}}/>
                                                            <a href={(file as FileItem).url} target="_blank"
                                                               rel="noopener noreferrer">{file.name}</a>
                                                        </div>
                                                    ))}
                                                </div>
                                            )}
                                        </Bubble>
                                    ))}
                                    <div ref={messagesEndRef}/>
                                </div>
                            ) : (
                                <Empty description="选择一个话题开始对话" style={{top: '40%', position: 'relative'}}/>
                            )}
                        </div>

                        {/* 发送消息区域 */}
                        <Sender
                            prefix={
                                <Button
                                    type="text"
                                    icon={<LinkOutlined/>}
                                    onClick={() => {
                                        setOpen(!open);
                                    }}
                                />
                            }
                            header={SenderHeader}
                            value={input}
                            onChange={setInput}
                            onSubmit={handleSend}
                            disabled={!activeTopic}
                            placeholder="输入消息，按Enter发送..."
                            style={{width: '100%', borderRadius: '0'}}
                        >
                        </Sender>
                    </div>
                </Splitter.Panel>

                {/* 右侧话题管理区域 */}
                <Splitter.Panel collapsible>
                    <Card
                        title="话题管理"
                        style={{height: '100%', overflow: 'auto', borderRadius: '0'}}
                        styles={{body: {padding: '15px'}}}
                        extra={
                            <Button
                                type="primary"
                                icon={<PlusOutlined/>}
                                onClick={handleCreateTopic}
                            >
                                {isCreatingTopic ? '保存' : '新建'}
                            </Button>
                        }
                    >
                        {isCreatingTopic && (
                            <div style={{marginBottom: '16px'}}>
                                <Input
                                    placeholder="输入话题标题"
                                    value={newTopicTitle}
                                    onChange={(e) => setNewTopicTitle(e.target.value)}
                                    onPressEnter={handleCreateTopic}
                                    autoFocus
                                />
                            </div>
                        )}

                        <Conversations
                            menu={menuConfig}
                            items={conversation}
                            activeKey={activeTopic}
                            onActiveChange={async (v) => {
                                setActiveTopic(v);
                                await handleActiveChange(v);
                            }}
                            style={{padding: '0'}}
                        />
                    </Card>
                </Splitter.Panel>
            </Splitter>
        </div>
    );
};

export default AIChatPages;