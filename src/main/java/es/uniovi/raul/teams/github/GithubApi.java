package es.uniovi.raul.teams.github;

import static es.uniovi.raul.teams.cli.CommandLine.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.*;

public final class GithubApi {
    public enum ApiResult {
        OK,
        ALREADY_EXISTS,
        REJECTED
    }

    private final String token;
    private final String organization;
    private final HttpClient client;

    public GithubApi(String token, String organization) {
        this.token = token;
        this.organization = organization;
        this.client = HttpClient.newHttpClient();
    }

    public Set<String> getExistingTeams() throws IOException, InterruptedException {
        Set<String> teams = new HashSet<>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/orgs/" + organization + "/teams"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String body = response.body();
            // Simple parsing to extract team slugs/names
            String[] parts = body.split("\\{");
            for (String part : parts) {
                int i = part.indexOf("\"name\":");
                if (i != -1) {
                    String name = part.substring(i + 8).split(",")[0].replaceAll("[\"{}]", "").trim();
                    teams.add(name);
                }
            }
        }
        return teams;
    }

    public ApiResult createTeam(String teamName) throws IOException, InterruptedException {
        String json = String.format("{\"name\":\"%s\",\"privacy\":\"closed\"}", teamName);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/orgs/" + organization + "/teams"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201)
            return ApiResult.OK;
        if (response.statusCode() == 422)
            return ApiResult.ALREADY_EXISTS;
        printError("Failed to create team '" + teamName + "'. Status: " + response.statusCode()
                + ". Response: " + response.body());
        return ApiResult.REJECTED;
    }

    public ApiResult addStudentToTeam(String teamName, String username) throws IOException, InterruptedException {
        String url = String.format("https://api.github.com/orgs/%s/teams/%s/memberships/%s", organization,
                teamName.toLowerCase(), username);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201)
            return ApiResult.OK;
        if (response.statusCode() == 200)
            return ApiResult.ALREADY_EXISTS;
        printError("Failed to add user '" + username + "' to team '" + teamName + "'. Status: "
                + response.statusCode() + ". Response: " + response.body());
        return ApiResult.REJECTED;
    }
}
