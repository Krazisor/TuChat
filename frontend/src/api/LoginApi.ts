import {fetchAPI} from "./BaseApi.ts";


export function loginToBE(accessToken: string): Promise<string | null> {
    return fetchAPI(`/login?token=${accessToken}`)
}