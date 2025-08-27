import { fetchAPI } from "./BaseApi";

export interface FileListResponse {
    fileId: string;
    fileName: string;
    fileSize: number;
    knowledgeBaseId: string;
    ownerId: string;
    uploadTime: string;
    isPublic: number;
    WhiteList: string;
    BlackList: string;
}

// 文件批量上传接口
export const uploadFilesToKnowledgeBase = async (
    files: File[],
    knowledgeBaseId: string
): Promise<string[] | null> => {
    const formData = new FormData();
    // 批量加入 files
    files.forEach(file => formData.append('files', file));
    // 加入知识库 id
    formData.append('knowledgeBaseId', knowledgeBaseId);

    return await fetchAPI('/file/uploadFile', {
        method: 'POST',
        body: formData
        // 不需要设置 Content-Type，浏览器自动处理 multipart/form-data
    });
};

// 查询指定知识库的所有文件
export const getFileListByKnowledgeBaseId = async (
    knowledgeBaseId: string
): Promise<FileListResponse[] | null> => {
    // 用 URLSearchParams 拼接 GET 查询参数
    const params = new URLSearchParams({ knowledgeBaseId }).toString();
    return await fetchAPI<FileListResponse[]>(`/file/list?${params}`, {});
};