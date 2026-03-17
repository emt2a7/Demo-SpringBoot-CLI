package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record TelegramRequest (
    @JsonProperty("chat_id") String chatId,
    String text
) {}
