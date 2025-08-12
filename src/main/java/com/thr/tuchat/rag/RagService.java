package com.thr.tuchat.rag;

import com.thr.tuchat.rag.sub.MarkdownDocumentLoader;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RagService {

    @Resource
    private VectorStore vectorStore;

    @Resource
    private MarkdownDocumentLoader markdownDocumentLoader;

    /**
     * 首先将file放到数据库中，然后进行向量化
     * @param fileList
     */
    public void putFileIntoVector (List<File> fileList){
        Map<String, Object> metadata = new HashMap<>();
        for (File file : fileList){

        }
    }

    public void addDocument(List<File> fileList, List<Map<String, Object>> metadata) {
        log.info("正在将文件:{},metadata:{}载入weaviate", fileList, metadata);
        List<Document> documents = markdownDocumentLoader.loadMarkdowns(fileList, metadata);
//        List<Document> documentsWithId =
        vectorStore.add(documents);
    }

    public void getSimilarDocument(File file, Map<String, Object> metadata) {}
}
