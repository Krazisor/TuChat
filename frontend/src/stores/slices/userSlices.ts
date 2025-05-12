import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

export interface userSliceType {
    isSignedIn: boolean
    saToken: string | undefined
}

const initialState: userSliceType = {
    isSignedIn: false,
    saToken: undefined
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
            state.saToken = undefined;
        }
    },
});

// 导出操作函数
export const {
    setLoginStatus,
    setLogoutStatus
} = userSlice.actions;

// 导出 reducer
export default userSlice.reducer;