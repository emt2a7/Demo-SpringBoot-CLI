package org.example.dto.line;

import lombok.Builder;

@Builder
public record LineMessage(String type, String text) {
}
