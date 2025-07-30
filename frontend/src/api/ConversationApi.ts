import {fetchAPI} from "./BaseApi.ts";

export interface ConversationBaseType {
    conversationId: string,
    userId: string,
    title: string,
    createTime: string,
    isMarked: boolean
}


export const getConversationList = async (): Promise<ConversationBaseType[] | null> => {
    return await fetchAPI('/conversation/list', {})
}

export const addNewConversation = async (title: string) : Promise<String | null> => {
    // 拼接参数
    const params = new URLSearchParams({ title }).toString();
    return await fetchAPI(`/conversation/add?${params}`, {})
}

export const deleteConversation = async (conversationId: string) : Promise<boolean | null> => {
    const params = new URLSearchParams({conversationId}).toString();
    return await fetchAPI(`/conversation/delete?${params}`, {})
}

export const updateConversation = async (conversation: ConversationBaseType) : Promise<boolean | null> => {
    return await fetchAPI('/conversation/update', {
        method: 'POST',
        body: JSON.stringify(conversation)
    })
}