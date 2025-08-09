package com.thr.tuchat.ai.sub;


import lombok.Data;

@Data
public class MyPromptTemplate {

    public static final String promptTemplate = """
            <query>
            
            以下是参考信息：
            
            ---------------------
            <context>
            ---------------------
            
            请只根据以上 context 内容并结合以下要求回答问题：
            
            1. 如果 context 未包含所需信息，请在回答时表明数据不来自知识库，但你依然可以正常回答内容。
            2. 不要使用“根据 context…”、“以上信息显示…”等类似开头语。
            3. 回答必须为中文。
            """;
}
