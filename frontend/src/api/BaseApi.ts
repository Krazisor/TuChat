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
            console.log(options.headers)
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