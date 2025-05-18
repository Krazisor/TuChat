import {fetchAPI} from "./BaseApi.ts";

export interface UserBaseType {
    userId: string,
    userName: string,
    email: string,
    createTime: string,
    avatar: string
}


export function getUserBaseInfo(): Promise<UserBaseType | null> {
    return fetchAPI(`/user/getInfo`, {})
}