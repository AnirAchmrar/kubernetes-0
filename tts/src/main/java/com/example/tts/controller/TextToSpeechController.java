package com.example.tts.controller;

import com.example.tts.data.TtsRequest;
import com.example.tts.service.TextToSpeechService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/tts")
public class TextToSpeechController {

    private static final Logger log = LoggerFactory.getLogger(TextToSpeechController.class);
    private final TextToSpeechService service;
    private final HttpServletRequest httpServletRequest;

    public TextToSpeechController(TextToSpeechService service, HttpServletRequest httpServletRequest) {
        this.service = service;
        this.httpServletRequest = httpServletRequest;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "audio/wav"
    )
    public ResponseEntity<InputStreamResource> textToSpeech(@RequestBody TtsRequest request,
                                                            HttpServletRequest httpServletRequest) throws IOException {
        log.info("textToSpeech request: {}", request);
        Path wavFile = service.textToSpeech(request, httpServletRequest);
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(wavFile.toAbsolutePath()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/wav"));
        headers.setContentDisposition(ContentDisposition
                .builder("attachment")
                .filename(wavFile.getFileName().toString())
                .build()
        );
        headers.setContentLength(Files.size(wavFile));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

}
