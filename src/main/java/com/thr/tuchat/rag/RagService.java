package com.thr.tuchat.rag;

import com.thr.tuchat.rag.sub.MarkdownDocumentLoader;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RagService {

    @Resource
    private VectorStore vectorStore;

    @Resource
    private MarkdownDocumentLoader markdownDocumentLoader;

    public void addDocument(List<File> fileList, Map<String, Object> metadata) {
        log.info("正在将文件:{},metadata:{}载入weaviate", fileList, metadata);
        List<Document> documents = markdownDocumentLoader.loadMarkdowns(fileList, metadata);
        vectorStore.add(documents);
    }

    public void getSimilarDocument(File file, Map<String, Object> metadata) {}
}
