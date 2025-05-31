import React from 'react';
import {Card, Button, Input} from 'antd';
import {PlusOutlined} from '@ant-design/icons';
import {Conversations, type ConversationsProps} from '@ant-design/x';

interface TopicSidebarProps {
    isCreatingTopic: boolean;
    handleCreateTopic: () => void;
    newTopicTitle: string;
    setNewTopicTitle: (v: string) => void;
    conversations: any[];
    activeTopic: string | undefined;
    onActiveChange: (key: string) => void;
    menuConfig: ConversationsProps['menu'];
}

const TopicSidebar: React.FC<TopicSidebarProps> = ({
                                                       isCreatingTopic,
                                                       handleCreateTopic,
                                                       newTopicTitle,
                                                       setNewTopicTitle,
                                                       conversations,
                                                       activeTopic,
                                                       onActiveChange,
                                                       menuConfig
                                                   }) => (
    <Card
        title="话题管理"
        style={{
            height: '100%',
            borderRadius: '0',
            display: 'flex',
            flexDirection: 'column'
        }}
        styles={{
            body: {
                padding: '0px',
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden' // 防止Card内容溢出
            }
        }}
        extra={
            <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={handleCreateTopic}
            >
                {isCreatingTopic ? '保存' : '新建'}
            </Button>
        }
    >
        {isCreatingTopic && (
            <div style={{flexShrink: 0, padding: '10px'}}>
                <Input
                    placeholder="输入话题标题"
                    value={newTopicTitle}
                    onChange={e => setNewTopicTitle(e.target.value)}
                    onPressEnter={handleCreateTopic}
                    autoFocus
                />
            </div>
        )}
        <div style={{flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column'}}>
            <Conversations
                menu={menuConfig}
                items={conversations}
                activeKey={activeTopic}
                onActiveChange={onActiveChange}
            />
        </div>
    </Card>
);

export default TopicSidebar;