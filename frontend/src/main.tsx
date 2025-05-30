import {createRoot} from 'react-dom/client'
import './index.css'
import MyApp from './MyApp.tsx'
import {BrowserRouter} from "react-router";
import {App} from "antd";
import {LogtoProvider} from "@logto/react";
import {logtoConfig} from "./config/LogtoConfig.ts";
import {PersistGate} from "redux-persist/integration/react";
import {Provider} from "react-redux";
import store, {persistor} from "./stores/BaseStore.ts";
import '@ant-design/v5-patch-for-react-19';

createRoot(document.getElementById('root')!).render(
    <Provider store={store}>
        <PersistGate loading={null} persistor={persistor}>
            <LogtoProvider config={logtoConfig}>
                <BrowserRouter>
                    <App>
                        <MyApp/>
                    </App>
                </BrowserRouter>
            </LogtoProvider>
        </PersistGate>
    </Provider>
)
