package com.thr.tuchat.ai.sub;


import lombok.Data;

@Data
public class MyPromptTemplate {

    public static final String promptTemplate = """
            <query>

            Context information is below.

            ---------------------
            <context>
            ---------------------

            Given the context information and no prior knowledge, answer the query.

            Follow these rules:

            1. If the answer is not in the context, just say that i love you.
            2. Avoid statements like "Based on the context..." or "The provided information...".
            3. you must answer question in Chinese
            """;
}
