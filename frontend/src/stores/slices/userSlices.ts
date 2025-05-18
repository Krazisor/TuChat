import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type {UserBaseType} from "../../api/userApi.ts";

export interface userSliceType {
    isSignedIn: boolean
    saToken: string | null
    userInfo: UserBaseType | null
}

const initialState: userSliceType = {
    isSignedIn: false,
    saToken: null,
    userInfo: null
};

const userSlice = createSlice({
    name: "user", // 命名空间
    initialState,
    reducers: {
        setLoginStatus(state, action:PayloadAction<string>) {
            state.isSignedIn = true;
            state.saToken = action.payload;
        },
        setLogoutStatus(state) {
            state.isSignedIn = false;
            state.saToken = null;
        },
        setUserInfo(state, action:PayloadAction<UserBaseType>) {
            state.userInfo = action.payload;
        },
        clearUserInfo(state) {
            state.userInfo = null;
        }
    },
});

// 导出操作函数
export const {
    setLoginStatus,
    setLogoutStatus,
    setUserInfo,
    clearUserInfo
} = userSlice.actions;

// 导出 reducer
export default userSlice.reducer;