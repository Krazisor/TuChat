import {fetchAPI} from "./BaseApi.ts";

export interface MessageBaseType {
    messageId: number,
    conversationId: string,
    role: string,
    content: string,
    createTime: string,
    attachment: string
}

export const getMessageListByConversationId = async (conversationId: string): Promise<MessageBaseType[]> => {
    // 拼接参数
    const params = new URLSearchParams({ conversationId }).toString();
    // 调用fetchAPI
    return await fetchAPI<MessageBaseType[]>(`/message/list?${params}`, {
        method: 'GET'
    }) as MessageBaseType[];
}