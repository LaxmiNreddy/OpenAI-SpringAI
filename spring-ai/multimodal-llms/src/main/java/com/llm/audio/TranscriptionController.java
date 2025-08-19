package com.llm.audio;

import com.llm.dto.TranscriptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TranscriptionController {
    private static final Logger log = LoggerFactory.getLogger(TranscriptionController.class);

    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    public TranscriptionController(OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel) {
        this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
    }


    @PostMapping("/v1/transcription")
    public ResponseEntity<?> transcription(@RequestParam("file") MultipartFile file){

        log.info("Transcription Initiated for file name : {} ", file.getOriginalFilename());
        var audioFile = file.getResource();

        var transcriptionRequest = new AudioTranscriptionPrompt(audioFile);

        var response = openAiAudioTranscriptionModel.call(transcriptionRequest);

        var transcriptionResponse  = new TranscriptionResponse(response.getResult().getOutput(), file.getOriginalFilename());
        log.info("Transcription Completed for file name : {} ", file.getOriginalFilename());
        return new ResponseEntity<>(transcriptionResponse,HttpStatus.OK);

    }

    @PostMapping("/v2/transcription")
    public ResponseEntity<?> transcriptionV2(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "prompt", required = true) String prompt,
                                               @RequestParam(value = "model", required = true) String model,
                                               @RequestParam(value = "language", required = true) String language,
                                               @RequestParam(value = "response_format", required = true) String responseFormat,
                                               @RequestParam(value = "temperature", required = false) Float temperature) {

        log.info("Transcription Initiated for file name : {} ", file.getOriginalFilename());
        log.info("prompt  : {}, model : {}, language : {}, responseFormat : {}, temperature : {} ", prompt, model, language, responseFormat, temperature);
        var audioFile = file.getResource();

        temperature = (temperature == null || temperature == 0.0f) ? 1.0f : temperature;

        var responseFormatEnum = OpenAiAudioApi.TranscriptResponseFormat.valueOf(responseFormat);

        var transcriptOptions = OpenAiAudioTranscriptionOptions
                .builder()
                .prompt(prompt)
                .model(model)
                .language(language)
                .responseFormat(responseFormatEnum)
                .temperature(temperature)
                .build();

        log.info("transcriptOptions : {} ", transcriptOptions);

        var transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptOptions);

        var response = openAiAudioTranscriptionModel.call(transcriptionRequest);

        var transcriptionResponse  = new TranscriptionResponse(response.getResult().getOutput(), file.getOriginalFilename());
        log.info("Transcription Completed for file name : {} ", file.getOriginalFilename());
        return new ResponseEntity<>(transcriptionResponse,HttpStatus.OK);

    }


}
