package com.llm.image;

import com.llm.dto.ImageInput;
import com.llm.dto.UserInput;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.llm.utils.ImageUtil.decodeBase64ToImage;
import static com.llm.utils.ImageUtil.saveImageToFile;

/**
 * https://platform.openai.com/docs/api-reference/images/create
 */
@RestController
public class ImageController {
    private static final Logger log = LoggerFactory.getLogger(ImageController.class);
    public OpenAiImageModel openAiImageModel;

    @Value("${spring.ai.openai.image.options.responseFormat}")
    private String responseFormat;

    public ImageController(OpenAiImageModel openAiImageModel) {
        this.openAiImageModel = openAiImageModel;
    }

    @PostMapping("/v1/images")
    public ImageResponse images(@RequestBody UserInput userInput){
        log.info("userInput message prompt is : {} ", userInput);
        var response = openAiImageModel.call(new ImagePrompt(userInput.prompt()));

        if(responseFormat.equals("b64_json")){
            var b64Json = response.getResult().getOutput().getB64Json();
            var image = decodeBase64ToImage(b64Json);
            String filePath  = "output_image.png"; // specify the desired file path and format
            boolean success = saveImageToFile(image, "png", filePath);
            if(success){
                log.info("Image successfully parsed and saved as '" + filePath + "'!");
            } else {
                log.info("Failed to save the image.");
            }

        }
        return response;
    }


}
