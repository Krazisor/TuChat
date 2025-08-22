import { fetchAPI } from "./BaseApi";

export type RoleEnum = 'owner' | 'editor' | 'viewer'; // 根据实际后端枚举值调整

export interface KnowledgeBaseListResponse {
    knowledgeBaseId: string;
    name: string;
    description: string;
    createTime: string; // ISO 8601，如 '2025-08-22T12:34:56Z'
    role: RoleEnum;
    joinTime: string; // ISO 8601
}

export interface NewKnowledgeBaseRequest {
    name: string;
    description: string;
}

export const getKnowledgeBaseList = async (): Promise<KnowledgeBaseListResponse[] | null> => {
    return await fetchAPI('/knowledgeBase/list', {})
}

export const addKnowledgeBase = async (request: NewKnowledgeBaseRequest) : Promise<boolean | null> => {
    return await fetchAPI('/knowledgeBase/add', {
        method: 'POST',
        body: JSON.stringify(request)
    })
}