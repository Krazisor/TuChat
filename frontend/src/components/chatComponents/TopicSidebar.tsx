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
        style={{height: '100%', overflow: 'auto', borderRadius: '0'}}
        styles={{body: {padding: '15px'}}}
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
            <div style={{marginBottom: '16px'}}>
                <Input
                    placeholder="输入话题标题"
                    value={newTopicTitle}
                    onChange={e => setNewTopicTitle(e.target.value)}
                    onPressEnter={handleCreateTopic}
                    autoFocus
                />
            </div>
        )}
        <Conversations
            menu={menuConfig}
            items={conversations}
            activeKey={activeTopic}
            onActiveChange={onActiveChange}
            style={{padding: '0'}}
        />
    </Card>
);

export default TopicSidebar;