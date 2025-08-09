import React, {useEffect, useRef, useState} from 'react';
import {Divider, message, Splitter, Typography} from 'antd';
import {
    DeleteOutlined,
    EditOutlined,
    StarOutlined,
    StopOutlined
} from '@ant-design/icons';

import {addNewConversation, deleteConversation, getConversationList} from "../../api/ConversationApi";
import {getMessageListByConversationId} from "../../api/MessageApi";
import {fetchAIResponseStream} from "../../api/FetchStream";
import dayjs from "dayjs";

import ChatMessageList, {type Message} from '../../components/chatComponents/ChatMessageList';
import ChatSender from '../../components/chatComponents/ChatSender';
import TopicSidebar from '../../components/chatComponents/TopicSidebar';
import type {Attachment} from "@ant-design/x/es/attachments";

const {Title} = Typography;

const AIChatPages: React.FC = () => {
    // --- State 定义 ---
    const [messages, setMessages] = useState<Message[]>([]);
    const [activeTopic, setActiveTopic] = useState<string>('');
    const [input, setInput] = useState<string>('');
    const [isCreatingTopic, setIsCreatingTopic] = useState<boolean>(false);
    const [newTopicTitle, setNewTopicTitle] = useState<string>('');
    const [attachments, setAttachments] = useState<Attachment[]>([]);
    const [open, setOpen] = useState(false);
    const [conversation, setConversation] = useState<any[]>([]);
    const [loading, setLoading] = useState<boolean>(false)
    const messagesEndRef = useRef<HTMLDivElement>(null);

    // --- 拉取会话列表 ---
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

    // 滚动到最新
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({behavior: 'smooth'});
    }, [messages, activeTopic]);

    // --- 会话列表菜单 ---
    const menuConfig = (conv: { key: any; }) => ({
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
        onClick: async (menuInfo: { domEvent: { stopPropagation: () => void; }; key: any; }) => {
            menuInfo.domEvent.stopPropagation();
            if (menuInfo.key === 'edit') {

            }
            if (menuInfo.key === 'delete') {
                if (await deleteConversation(conv.key)) {
                    message.success("删除成功")
                } else {
                    message.error("删除失败")
                }
            }
            let conversationList = await getConversationList();
            setConversation(
                conversationList!.map(item => ({
                    key: item.conversationId,
                    label: item.title,
                    icon: <StarOutlined style={{color: '#bbbbbb'}}/>,
                    timestamp: Number(item.createTime)
                }))
            );
        },
    });

    // --- 选中话题，拉取消息 ---
    const handleActiveChange = async (key: string) => {
        setActiveTopic(key);
        const response = await getMessageListByConversationId(key);
        const temp: Message[] = response.map(item => ({
            id: String(item.messageId),
            content: item.content,
            role: (item.role === 'user' || item.role === 'assistant')
                ? item.role as 'user' | 'assistant'
                : 'user',
            timestamp: dayjs(item.createTime).valueOf(),
            attachments: undefined,
        }));
        setMessages(temp);
    };

    // --- 发送消息 ---
    const handleSend = (value: string): void => {
        if (!value.trim() && attachments.length === 0) return;
        if (!activeTopic) return;
        setLoading(true);

        const newUserMsg: Message = {
            id: `${activeTopic}-${Date.now()}`,
            content: value.trim(),
            role: 'user',
            timestamp: Date.now(),
            attachments: attachments.length > 0 ? [...attachments] : undefined
        };

        setMessages([...messages, newUserMsg]);

        const dto = {
            conversationId: activeTopic,
            question: value,
            model: 'gpt-4.1'
        };
        console.log(attachments)

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
                    const updated = [...prev];
                    updated[updated.length - 1] = {
                        ...updated[updated.length - 1],
                        content: updated[updated.length - 1].content + msg,
                    };
                    return updated;
                });
            },
            err => {
                message.error(err).then(r => setLoading(false));
            },
            () => {
                setLoading(false)
            }
        )
    };

    // --- 创建新话题 ---
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
        );
        setActiveTopic(conversationId as string);
        await handleActiveChange(conversationId as string)
        setIsCreatingTopic(false);
        setNewTopicTitle('');
    };

    // --- 移除附件 ---
    const handleRemoveAttachment = (file: { uid: any; }): void => {
        setAttachments(prev => prev.filter(item => item.uid !== file.uid));
    };

    return (
        <div style={{height: '100%', overflow: 'hidden'}}>
            <Splitter style={{height: '100%'}}>
                {/* 聊天内容/发送 */}
                <Splitter.Panel defaultSize="85%" min="50%" max="85%">
                    <div style={{height: '100%', display: 'flex', flexDirection: 'column'}}>
                        <div style={{textAlign: 'center', background: '#f8f8f8'}}>
                            <Title level={4} style={{
                                height: '30px', lineHeight: '30px',
                                margin: 0, padding: '0 8px', display: 'inline-block',
                            }}>
                                {conversation.find(t => t.key === activeTopic)?.label}
                            </Title>
                        </div>
                        <Divider style={{margin: '0', background: '#eeeeee', height: '2px'}}/>
                        <ChatMessageList messages={messages} messagesEndRef={messagesEndRef} activeTopic={activeTopic}/>
                        <ChatSender
                            input={input}
                            onInputChange={setInput}
                            onSend={handleSend}
                            attachments={attachments}
                            setAttachments={setAttachments}
                            open={open}
                            loading={loading}
                            setOpen={setOpen}
                            disabled={!activeTopic}
                        />
                    </div>
                </Splitter.Panel>
                {/* 右侧话题管理 */}
                <Splitter.Panel collapsible>
                    <TopicSidebar
                        isCreatingTopic={isCreatingTopic}
                        handleCreateTopic={handleCreateTopic}
                        newTopicTitle={newTopicTitle}
                        setNewTopicTitle={setNewTopicTitle}
                        conversations={conversation}
                        activeTopic={activeTopic}
                        onActiveChange={async v => {
                            setActiveTopic(v);
                            await handleActiveChange(v);
                        }}
                        menuConfig={menuConfig}
                    />
                </Splitter.Panel>
            </Splitter>
        </div>
    );
};

export default AIChatPages;