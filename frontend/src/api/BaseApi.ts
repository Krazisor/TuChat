import {env} from "../env.ts";
import store from "../stores/BaseStore.ts";
import {message} from "antd";

export const BASE_URL = env.VITE_APP_API_URL

export interface Result<T> {
    code: number;
    message: string;
    data: T;
}

/**
 * 通用 Fetch 方法
 */
export const fetchAPI = async <T>(url: string, options?: RequestInit): Promise<T | null> => {
    if (!url.includes('/login')) {
        //非登录接口自动加上请求头
        if (options) {
            const satoken = store.getState().user.saToken ?? '';
            options.headers = {
                'satoken': `${satoken}`,
                'Content-Type': 'application/json',
                ...options.headers,
            }
        }
    }
    const response = await fetch(`${BASE_URL}${url}`, options);

    if (!response.ok) {
        message.error('网络错误')
        throw new Error("网络错误")
    }
    const result: Result<T> = await response.json();
    if (result.code != 200) {
        message.error(result.message);
        throw new Error(result.message)
    } else {
        return result.data;
    }
};

// 文件上传接口，resultType 是接口返回的结构类型
export const uploadFileAPI = async <T>(
    url: string,
    file: File,
    additionalData?: Record<string, string | Blob>
): Promise<T | null> => {
    const formData = new FormData();
    formData.append("file", file);
    // 支持额外参数
    if (additionalData) {
        Object.entries(additionalData).forEach(([key, value]) => {
            formData.append(key, value);
        });
    }

    // 拼 headers
    const satoken = store.getState().user.saToken ?? '';

    const headers: Record<string, string> = {
        'satoken': `${satoken}`,
        // 不要手动加 'Content-Type'
    };

    const response = await fetch(`${BASE_URL}${url}`, {
        method: 'POST',
        body: formData,
        headers // fetch会自动加multipart边界
    });

    if (!response.ok) {
        message.error('网络错误');
        throw new Error("网络错误");
    }

    const result: Result<T> = await response.json();
    if (result.code !== 200) {
        message.error(result.message);
        throw new Error(result.message);
    } else {
        return result.data;
    }
};