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