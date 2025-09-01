import { fetchAPI } from "./BaseApi";

export type RoleEnum = 'OWNER' | 'EDITOR' | 'VIEWER' | 'NONE'; // 根据实际后端枚举值调整

export interface KnowledgeBaseListResponse {
    knowledgeBaseId: string;
    name: string;
    description: string;
    createTime: string; // ISO 8601，如 '2025-08-22T12:34:56Z'
    role: RoleEnum;
    joinTime: string; // ISO 8601
}

export interface KnowledgeUpdateRequest {
    knowledgeBaseId: string;
    name: string;
    description?: string;
    editorList?: string[];
    viewerList?: string[];
}

export interface KnowledgeBaseReOwnerRequest {
    knowledgeBaseId: string;
    newOwnerId: string;
    newRole: RoleEnum;
}

export interface NewKnowledgeBaseRequest {
    name: string;
    description: string;
}

export interface KnowledgeBaseMember {
    EDITOR: string[];
    VIEWER: string[];
}

export const getKnowledgeBaseList = async (): Promise<KnowledgeBaseListResponse[] | null> => {
    return await fetchAPI('/knowledgeBase/list', {})
}

export const addKnowledgeBase = async (request: NewKnowledgeBaseRequest): Promise<boolean | null> => {
    return await fetchAPI('/knowledgeBase/add', {
        method: 'POST',
        body: JSON.stringify(request)
    })
}

export const deleteKnowledgeBase = async (knowledgeBaseId: string): Promise<boolean | null> => {
    const params = new URLSearchParams({ knowledgeBaseId }).toString();
    return await fetchAPI(`/knowledgeBase/delete?${params}`, {})
}

export const updateKnowledgeBase = async (request: KnowledgeUpdateRequest): Promise<boolean | null> => {
    return await fetchAPI('/knowledgeBase/update', {
        method: 'POST',
        body: JSON.stringify(request)
    })
}

export const reOwnerKnowledgeBase = async (request: KnowledgeBaseReOwnerRequest): Promise<boolean | null> => {
    return await fetchAPI('/knowledgeBase/reOwner', {
        method: 'POST',
        body: JSON.stringify(request)
    })
}

export const getKnowledgeBaseMembers = async (knowledgeBaseId: string): Promise<KnowledgeBaseMember | null> => {
    const params = new URLSearchParams({ knowledgeBaseId }).toString();
    return await fetchAPI(`/knowledgeBase/member?${params}`, {})
}
