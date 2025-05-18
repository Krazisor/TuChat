import React, { useEffect, useState } from "react";

import { useLogto } from "@logto/react";
import useApp from "antd/es/app/useApp";
import HomePage from "../../pages/Homepage.tsx";
import { useNavigate } from "react-router";
import FullScreenLoading from "../universalTool/FullScreenLoading.tsx";
import {useAppSelector} from "../../stores/StoreHook.ts";

const ProtectedRoute: React.FC<{ element: React.ReactNode }> = ({ element }) => {
    const { isAuthenticated, getAccessToken } = useLogto();
    const user = useAppSelector(state => state.user)
    const [isAuthorized, setIsAuthorized] = useState<boolean | null>(null);
    const { message } = useApp()
    const nav = useNavigate()
    useEffect(() => {
        const checkAuth = async () => {
            const token = await getAccessToken();
            if (!token || !user.isSignedIn) {
                message.info('请先登录');
                setIsAuthorized(false);
                nav('/')
            } else {
                setIsAuthorized(true);
            }
        };

        checkAuth();
    }, [getAccessToken, isAuthenticated, user.isSignedIn, message, nav]);

    if (isAuthorized === null) {
        // Optionally, you could return a loader or spinner here while you're checking
        return <FullScreenLoading text="登录验证中" />;
    }

    return isAuthorized ? <>{element}</> : <HomePage />;
};

export default ProtectedRoute;