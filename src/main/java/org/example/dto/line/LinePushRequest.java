package org.example.dto.line;

import lombok.Builder;
import java.util.List;

@Builder
public record LinePushRequest(String to, List<LineMessage> messages) {
}
