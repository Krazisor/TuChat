import {env} from "../env.ts";
import store from "../stores/BaseStore.ts";
import {createParser} from 'eventsource-parser';

export const BASE_URL = env.VITE_APP_API_URL

export interface AIRequestDTO {
    conversationId: string;
    question: string;
    model: string;
}

export async function fetchAIResponseStream(
    payload: AIRequestDTO,
    onMessage: (msg: string) => void,
    onError?: (err: any) => void,
) {
    try {
        const satoken = store.getState().user.saToken ?? '';
        const response = await fetch(BASE_URL + '/ai/getAIResponse', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream',
                'satoken': `${satoken}`,
            },
            body: JSON.stringify(payload),
        });

        if (!response.body) {
            throw new Error('No response body');
        }

        const reader = response.body.getReader();
        const parser = createParser({
            onEvent: (event) => {
                if (event.data === '[[LINEBREAKS]]') {
                    // 空data行转换为换行符
                    onMessage('\n');
                } else if (event.data === '[[SPACE]]') {
                    // 特殊标识符转换为空格
                    onMessage(' ');
                } else if (event.data) {
                    // 正常数据传递
                    onMessage(event.data);
                }
            }
        });

        // 循环读取流数据
        while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            // 喂数据到解析器，它会自动处理SSE格式并触发回调
            parser.feed(new TextDecoder().decode(value));
        }
    } catch (err) {
        if (onError) onError(err);
        else console.error(err);
    }
}