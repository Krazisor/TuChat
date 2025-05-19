import {useNavigate} from "react-router";
import {useHandleSignInCallback, useLogto} from "@logto/react";
import {BASE_URL} from "../api/BaseApi.ts";
import {loginToBE} from "../api/LoginApi.ts";
import React from "react";
import FullScreenLoading from "../components/universalTool/FullScreenLoading.tsx";
import {useAppDispatch} from "../stores/StoreHook.ts";
import {setLoginStatus} from "../stores/slices/userSlices.ts";
import {getUserBaseInfo} from "../api/userApi.ts";


const Callback: React.FC = () => {
    const nav = useNavigate();
    const {getAccessTokenClaims, getAccessToken} = useLogto();
    const dispatch = useAppDispatch();
    const {isLoading} = useHandleSignInCallback(async () => {
        try {
            const claims = await getAccessTokenClaims(BASE_URL);
            if (claims) {
                const userId = claims.sub;
                const token = await getAccessToken(BASE_URL)
                const saToken = await loginToBE(token!)
                if (saToken) {
                    dispatch(setLoginStatus(saToken))
                    nav('/dashboard');
                } else {
                    nav('/')
                }

            }
        } catch (error) {
            console.error('Error processing login:', error);
            nav('/'); // Redirect to homepage on error
        }
    });

    // Loading indicator
    if (isLoading) {
        return (
            <FullScreenLoading type="bar" size="large" text="正在登录中..." />
        );
    }

    return null;
}

export default Callback;