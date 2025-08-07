package com.thr.tuchat.rag;

import com.thr.tuchat.rag.sub.MarkdownDocumentLoader;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class DocumentLoaderTest {

    @Resource
    private MarkdownDocumentLoader documentLoader;

//    @Test
//    void loadMarkdowns() {
//        documentLoader.loadMarkdowns();
//    }
}