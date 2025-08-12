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

    /**
     * 将文件们批量分片分块处理进行向量化DEMO
     * @param fileList 文件列表
     * @param metadata 元信息
     * @return 处理好的文档
     */
    public List<Document> loadMarkdowns (List<File> fileList, List<Map<String, Object>> metadata){
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
                    .withAdditionalMetadata("filename", fileName == null ? "unknownFile" : fileName)
                    .withAdditionalMetadata(metadata.get(resources.indexOf(resource)))
                    .build();
            MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
            allDocuments.addAll(markdownDocumentReader.get());
        }
        return allDocuments;
    }
}
