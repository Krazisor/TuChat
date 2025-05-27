import {env} from "../env.ts";
import store from "../stores/BaseStore.ts";

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
        const decoder = new TextDecoder('utf-8');
        let partial = '';

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            partial += decoder.decode(value, { stream: true });

            // 按行分割处理
            let lines = partial.split('\n');
            partial = lines.pop()!; // 保留最后一行残余数据

            for (const line of lines) {
                if (line.trim() === '') continue;
                // 移除SSE格式前缀 "data: "
                const msg = line.replace(/^data:\s*/i, '');
                onMessage(msg);
            }
        }
    } catch (err) {
        if (onError) onError(err);
        else console.error(err);
    }
}