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
    onComplete?: () => void,      // 新增
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
                    onMessage('\n');
                } else if (event.data === '[[SPACE]]') {
                    onMessage(' ');
                } else if (event.data) {
                    onMessage(event.data);
                }
            }
        });

        // 循环读取流数据
        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            parser.feed(new TextDecoder().decode(value));
        }
        // 读取完毕后执行 onComplete
        if (onComplete) onComplete();
    } catch (err) {
        if (onError) onError(err);
        // 可选：异常时也可执行 onComplete，通常业务上更推荐只执行 onError
        if (onComplete) onComplete();
        else console.error(err);
    }
}