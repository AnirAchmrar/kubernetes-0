package com.example.tts.data;

public record TtsRequest(String text) {

    public String getError(){
        if (text == null || text.isEmpty() || text.isBlank())
            return "Empty or blank text.";
        if (text.trim().length() > 25000)
            return "Max length exceeded (25000 character).";
        return null;
    }
}
