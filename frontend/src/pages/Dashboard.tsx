import React, {useEffect, useState} from "react";
import {useAppSelector} from "../stores/StoreHook.ts";
import {type IdTokenClaims, useLogto} from "@logto/react";


const Dashboard: React.FC = () => {

    const userInfo = useAppSelector(state => state.user)
    const { isAuthenticated, getIdTokenClaims } = useLogto();
    const [user, setUser] = useState<IdTokenClaims>();

    useEffect(() => {
        (async () => {
            if (isAuthenticated) {
                const claims = await getIdTokenClaims();
                setUser(claims);
            }
        })();
    }, [getIdTokenClaims, isAuthenticated]);

    return (
        <>
            <h1>{userInfo.isSignedIn}</h1>
            <h1>{userInfo.saToken}</h1>
            {isAuthenticated && user && (
                <table>
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Value</th>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.entries(user).map(([key, value]) => (
                        <tr key={key}>
                            <td>{key}</td>
                            <td>{typeof value === 'string' ? value : JSON.stringify(value)}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
            );
        </>
    )
}

export default Dashboard;