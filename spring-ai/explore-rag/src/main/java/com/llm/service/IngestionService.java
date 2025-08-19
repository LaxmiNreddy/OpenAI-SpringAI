package com.llm.service;

import com.llm.utils.RagUtiils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IngestionService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;

    @Value("classpath:/docs/Flexora_FAQ.pdf")
    private Resource faqPdf;

    @Value("${ingestion.enabled:false}")
    private boolean ingestionEnabled;

    public IngestionService(@Qualifier(value = "qaVectorStore") PgVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {

//        ingestPDFDocs(faqPdf);
    }

    private void ingestPDFDocs(String ingestType, Resource pdfResource) {
        log.info("Ingesting PDF docs");
        var docs = getPDFDocuments(ingestType, pdfResource);
        vectorStore.add(docs);
        log.info("Ingested {} documents from pdf successfully", docs.size());
    }

    private static List<Document> getPDFDocuments(String ingestType, Resource pdfResource) {

        try{
            return  switch (ingestType){
                case "page" -> new PagePdfDocumentReader(pdfResource).get();
                case "paragraph" -> new ParagraphPdfDocumentReader(pdfResource).get();
                default -> throw new IllegalArgumentException("Invalid ingest type: " + ingestType);
            };
        }catch (Exception e ){
            log.error("Error while reading PDF document: {}", e.getMessage(), e);
            throw new RuntimeException("Error while reading PDF document", e);
        }


    }

    public void ingest(byte[] fileContent, String filename, String ingestType) {

        log.info("IngestionService is invoked - ingest with fileName: {}, ingestType: {}", filename, ingestType);
        Resource docSource = new ByteArrayResource(fileContent){
            @Override
            public String getFilename() {
                return filename;
            }
        };

        var fileExtension = RagUtiils.getFileExtension(filename);
        switch(fileExtension){
            case "pdf" -> {
                log.info("Ingesting PDF file: {}", filename);
                ingestPDFDocs(ingestType, docSource);
            }
            case "docx" -> {
                log.info("Ingesting DOCX file: {}", filename);
                // Implement DOCX ingestion logic here
                ingestWordDocs(ingestType, docSource);
            }
            case "txt" -> {
                log.info("Ingesting txt file: {}", filename);
                // Implement DOCX ingestion logic here
                ingestTextDocs(ingestType, docSource);
            }
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileExtension);
        }


    }

    private void ingestTextDocs(String ingestType, Resource docSource) {
        log.info("Ingesting Text docs");
        var textReader = new TextReader(docSource);
        textReader.getCustomMetadata().put("filename", docSource.getFilename());
        var docs = textReader.read();
        vectorStore.add(docs);
        log.info("Ingested {} documents from text file successfully", docs.size());

    }

    private void ingestWordDocs( String ingestType, Resource docSource) {
        log.info("Ingesting Word docs");
        var docs = getWordDocuments(docSource, ingestType);
        vectorStore.add(docs);
        log.info("Ingested {} documents from word successfully", docs.size());
    }

    private static List<Document> getWordDocuments(Resource docSource, String ingestType){

        var docs = new TikaDocumentReader(docSource).get();
        return switch (ingestType){
            case "token" -> {
//                TokenTextSplitter splitter = new TokenTextSplitter();
                TokenTextSplitter splitter = new TokenTextSplitter(250, 150,
                        10, 5000, true);
                yield splitter.apply(docs);
            }
            default -> docs;

        };

    }
}
