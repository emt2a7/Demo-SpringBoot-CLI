package org.example.dto.github;

public record GithubWorkflowDto(
        long id,
        String name,
        String state,
        String path
) {}
