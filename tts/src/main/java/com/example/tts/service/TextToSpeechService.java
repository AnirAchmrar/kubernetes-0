package com.example.tts.service;

import com.example.tts.data.TtsAnalyticsRequest;
import com.example.tts.data.TtsAnalyticsResponse;
import com.example.tts.data.TtsRequest;
import com.example.tts.errorhandling.exception.InvalidRequestException;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.sound.sampled.AudioFileFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Service
public class TextToSpeechService {

    private static final String VOICE_NAME = "kevin16";
    private static final Logger log = LoggerFactory.getLogger(TextToSpeechService.class);
    @Value("${tts.output.base-path}")
    private String basePath;
    @Value("${tts.analyticsUrl}")
    private String analyticsUrl;
    private final RestTemplate restTemplate;

    public TextToSpeechService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() throws IOException {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        // Make sure the base output directory itself exists:
        Files.createDirectories(Paths.get(basePath));
    }

    public Path textToSpeech(TtsRequest request, HttpServletRequest httpServletRequest) {
        if (request.getError() != null)
            throw new InvalidRequestException(request.getError());
        var analyticsResponse = sendToAnalytics(
                new TtsAnalyticsRequest(
                        httpServletRequest.getRemoteAddr() != null ? httpServletRequest.getRemoteAddr() : "Unknown",
                        httpServletRequest.getHeader("User-Agent") != null ? httpServletRequest.getHeader("User-Agent") : "Unknown"
                )
        );
        doSomethingWithAnalytics(analyticsResponse);
        Voice voice = VoiceManager.getInstance().getVoice(VOICE_NAME);
        if (voice == null) {
            throw new IllegalStateException("Voice not found");
        }
        String filename = UUID.randomUUID().toString();
        Path file = Paths.get(basePath, filename);
        SingleFileAudioPlayer sfap = new SingleFileAudioPlayer(file.toAbsolutePath().toString(), AudioFileFormat.Type.WAVE);
        voice.setAudioPlayer(sfap);
        voice.allocate();
        voice.speak(request.text());
        voice.deallocate();
        sfap.close();
        return file.resolveSibling(file.getFileName() + ".wav");
    }

    private void doSomethingWithAnalytics(TtsAnalyticsResponse analyticsResponse) {
        log.info("do Something with analytics response: {}", analyticsResponse);
    }

    private TtsAnalyticsResponse sendToAnalytics(TtsAnalyticsRequest ttsAnalyticsRequest){
        try {
            return restTemplate.postForObject(analyticsUrl + "/ttsAnalytics", ttsAnalyticsRequest, TtsAnalyticsResponse.class);
        } catch (RestClientException e) {
            //Maybe some retry mechanism is needed
            log.error("sendToAnalytics failed", e);
        }
        return null;
    }


}
