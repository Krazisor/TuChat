package com.thr.tuchat.rag.sub;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MarkdownDocumentLoader {

//    private final ResourcePatternResolver resourcePatternResolver;

//    MarkdownDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
//        this.resourcePatternResolver = resourcePatternResolver;
//    }

    public List<Document> loadMarkdowns (List<File> fileList, Map<String, Object> metadata){
        List<Document> allDocuments = new ArrayList<>();
        // 将file转换为Resource格式
        List<Resource> resources = fileList.stream()
                .map(FileSystemResource::new)
                .collect(Collectors.toList());
        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .withIncludeBlockquote(false)
                    .withIncludeBlockquote(false)
                    .withAdditionalMetadata("filename", fileName == null ? "unknownFile" : fileName)
                    .withAdditionalMetadata(metadata)
                    .build();
            MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
            allDocuments.addAll(markdownDocumentReader.get());
        }
        return allDocuments;
    }
}
