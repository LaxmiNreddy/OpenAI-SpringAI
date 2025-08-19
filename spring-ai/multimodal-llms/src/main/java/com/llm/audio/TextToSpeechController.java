package com.llm.audio;

import com.llm.dto.TTSInput;
import com.llm.dto.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.llm.utils.AudioUtil.writeMP3ToFile;

@RestController
public class TextToSpeechController {

    private static final Logger log = LoggerFactory.getLogger(TextToSpeechController.class);
    public static String OUTPUT_PATH = "multimodal-llms/src/main/resources/files/audio";

    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;

    public TextToSpeechController(OpenAiAudioSpeechModel openAiAudioSpeechModel) {
        this.openAiAudioSpeechModel = openAiAudioSpeechModel;
    }

    @PostMapping("/v1/tts")
    public ResponseEntity<String> audio(@RequestBody UserInput userInput){

        log.info("userInput message : {} ",userInput);
          var speechPrompt = new SpeechPrompt(userInput.prompt());
        var speechResponse = openAiAudioSpeechModel.call(speechPrompt);
        byte[] responseBytes = speechResponse.getResult().getOutput();
        writeMP3ToFile(responseBytes, OUTPUT_PATH + "/speech.mp3");
        return ResponseEntity.ok("Audio Generated Successfully");
    }

    @PostMapping("/v2/tts")
    public ResponseEntity<String> audioV2(@RequestBody TTSInput ttsInput){

        log.info("userInput message : {} ",ttsInput);

        var speechOptions = OpenAiAudioSpeechOptions.builder()
                .model(ttsInput.model().value)
                .speed(ttsInput.speed())
                .responseFormat(ttsInput.responseFormat())
                .voice(ttsInput.voice())
                .build();
        var speechPrompt = new SpeechPrompt(ttsInput.prompt(), speechOptions);
        var speechResponse = openAiAudioSpeechModel.call(speechPrompt);
        byte[] responseBytes = speechResponse.getResult().getOutput();

        //multimodal-llms/src/main/resources/files/audio/speech.mp3
        var outputPath = String.format(
                "%s/%s.%s",
                OUTPUT_PATH,
                ttsInput.fileName(),
                ttsInput.responseFormat()
        );
        log.info("outputPath : {} ", outputPath);
        writeMP3ToFile(responseBytes, outputPath);
        return ResponseEntity.ok("Audio Generated Successfully");
    }

}
