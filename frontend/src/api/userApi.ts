import {fetchAPI, uploadFileAPI} from "./BaseApi.ts";

export interface UserBaseType {
    userId: string,
    userName: string,
    email: string,
    createTime: string,
    avatar: string
}


export async function getUserBaseInfo(): Promise<UserBaseType | null> {
    return await fetchAPI(`/user/getInfo`, {})
}

export const updateUserAvatar = async (file: File): Promise<string | null> => {
    return await uploadFileAPI('/user/updateAvatar', file)
}