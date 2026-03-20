package org.example.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubRepoDto(
        String name,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("private") boolean isPrivate
) {}
