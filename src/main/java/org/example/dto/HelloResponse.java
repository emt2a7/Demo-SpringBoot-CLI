package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回傳給客戶端的問候 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelloResponse {
    private String message;
}


