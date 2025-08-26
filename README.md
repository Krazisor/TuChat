# 关于weaviate的配置
## 使用docker的docker-compose.yml进行配置
    version: '3.8'
    
    services:
    weaviate:
    image: cr.weaviate.io/semitechnologies/weaviate:1.31.0
    container_name: weaviate
    restart: unless-stopped

    ports:
      - "50050:8080"
      - "50051:50051"
      
    environment:
      QUERY_DEFAULTS_LIMIT: 50
      AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED: 'true'
      PERSISTENCE_DATA_PATH: "/var/lib/weaviate"
      CLUSTER_HOSTNAME: 'node1'
    volumes:
      - weaviate_data:/var/lib/weaviate

    volumes:
        weaviate_data:
## 通过请求创建相应的class文件名为document-class.json
    {
        "class": "SpringAiWeaviate",
        "description": "A class to store content and metadata",
        "properties": [{
            "name": "content",
            "description": "Main content",
            "dataType": ["text"]
        },{
            "name": "metadata",
            "description": "Any metadata info",
            "dataType": ["text"]
        }]
    }
## 在相应路径下作如下请求
### 如果是windows环境
    $body = Get-Content .\document-class.json -Raw
    Invoke-WebRequest -Uri "http://localhost:50050/v1/schema" `
                      -Method POST `
                      -Headers @{'Content-Type' = 'application/json'} `
                      -Body $body
### 如果是linux环境
    curl -X POST http://localhost:8080/v1/schema \
         -H "Content-Type: application/json" \
         -d @document-class.json
    