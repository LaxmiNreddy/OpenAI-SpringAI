package com.llm.service;


import com.llm.dtos.GroundingRequest;
import com.llm.dtos.GroundingResponse;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
//import org.springframework.ai.vectorstore.SearchRequest;
//import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GroundingService {

    private static final Logger log = LoggerFactory.getLogger(GroundingService.class);

    private final ChatClient chatClient;

    private String handbookContent;

    @Value("classpath:/prompt-templates/RAG-Prompt.st")
    private Resource ragPrompt;

//    private final PgVectorStore vectorStore;


    @Value("classpath:/prompt-templates/RAG-QA-Prompt.st")
    private Resource ragQAPrompt;

    public GroundingService(ChatClient.Builder chatClientBuilder
//                            ,@Qualifier("qaVectorStore") PgVectorStore vectorStore
    ) {
        this.chatClient = chatClientBuilder.build();
       // this.vectorStore=vectorStore;
    }

    public GroundingResponse grounding(GroundingRequest groundingRequest) {

        PromptTemplate promptTemplate = new PromptTemplate(ragPrompt);
        var promptMessage = promptTemplate.createMessage(
                Map.of("input", groundingRequest.prompt(),
                        "context", handbookContent));

        var prompt = new Prompt(List.of(promptMessage));
        var response = chatClient.prompt(prompt)
                .call()
                .content();

       return new GroundingResponse(response);
    }


    @PostConstruct
    public void init() throws IOException {

        Path filePath = Paths.get("explore-rag/src/main/resources/docs/technova-handbook.txt");
        handbookContent = Files.readString(filePath);

    }

//    public GroundingResponse retrieveAnswer(GroundingRequest groundingRequest) {
//
//
//        var results = vectorStore
//                .doSimilaritySearch(SearchRequest.builder()
//                        .query(groundingRequest.prompt())
//                        .build());
//
//        log.info("results size : {} ", results.size());
//
//        var context = results
//                .stream()
//                .filter(Objects::nonNull)
//                .filter(result -> result.getScore() > 0.8)
//                .limit(2)
//                .map(result -> result.getText())
//                .collect(Collectors.joining("\n"));
//
//
//
//        if(StringUtils.isNotEmpty(context))
//        {
//            log.info("Matched context :  {} ", context);
//
//            PromptTemplate promptTemplate = new PromptTemplate(ragQAPrompt);
//            var promptMessage = promptTemplate.createMessage(
//                    Map.of("input", groundingRequest.prompt(),
//                            "context", context));
//
//            var prompt = new Prompt(List.of(promptMessage));
//            var response = chatClient.prompt(prompt)
//                    .call()
//                    .content();
//            log.info("response : {} ", response);
//            return new GroundingResponse(response);
//
//        }else{
//            log.info("No relevant context, so sending default response");
//            return new GroundingResponse("Sorry No Relevant Info Found");
//        }
//
//
//    }
}
