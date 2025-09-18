package es.uniovi.raul.teams.github;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.Builder;
import java.util.*;

import com.fasterxml.jackson.databind.*;

/**
 * Github API implementation.
 */
public final class GithubConnectionImpl implements GithubConnection {

    private String token;

    public GithubConnectionImpl(String token) {
        if (token == null || token.isBlank())
            throw new IllegalArgumentException("Token cannot be null or blank.");

        this.token = token;
    }

    @Override
    public List<Team> getTeams(String organization)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<Team> teams = new ArrayList<>();
        try (HttpClient client = HttpClient.newHttpClient()) {

            String url = "https://api.github.com/orgs/" + organization + "/teams";
            HttpRequest request = createHttpRequestBuilder(url).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new RejectedOperationException(
                        "Failed to get existing teams for organization '%s'. Status: %d. Response: %s",
                        organization, response.statusCode(), response.body());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            if (!root.isArray())
                throw new UnexpectedFormatException("Expected a JSON array for teams, got: %s", root.getNodeType());

            for (JsonNode node : root) {
                JsonNode nameNode = node.get("name");
                JsonNode slugNode = node.get("slug");
                if (nameNode == null || !nameNode.isTextual() || slugNode == null || !slugNode.isTextual())
                    throw new UnexpectedFormatException(
                            "Expected 'name' and 'slug' fields of type string in each team object, got: %s", node);

                teams.add(new Team(nameNode.asText(), slugNode.asText()));
            }
            return teams;
        }
    }

    @Override
    public Optional<String> createTeam(String organization, String teamDisplayName)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        try (HttpClient client = HttpClient.newHttpClient()) {

            String url = "https://api.github.com/orgs/" + organization + "/teams";
            String json = String.format("{\"name\":\"%s\",\"privacy\":\"closed\"}", teamDisplayName);

            HttpRequest request = createHttpRequestBuilder(url)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode slugNode = root.get("slug");
                if (slugNode == null || !slugNode.isTextual()) {
                    throw new UnexpectedFormatException(
                            "Expected 'slug' field of type string in created team object, got: %s", root);
                }
                return Optional.of(slugNode.asText());
            }

            if (response.statusCode() == 422) // Team already exists
                return Optional.empty();

            throw new RejectedOperationException(
                    "Failed to create team '%s' in organization '%s'. Status: %d. Response: %s",
                    teamDisplayName, organization, response.statusCode(), response.body());
        }
    }

    @Override
    public void deleteTeam(String organization, String teamSlug)
            throws IOException, InterruptedException, RejectedOperationException {

        try (HttpClient client = HttpClient.newHttpClient()) {
            String url = String.format("https://api.github.com/orgs/%s/teams/%s", organization, teamSlug);
            HttpRequest request = createHttpRequestBuilder(url)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // GitHub returns 204 if deleted, 404 if not found (treat both as success)
            if (response.statusCode() == 204 || response.statusCode() == 404)
                return;

            throw new RejectedOperationException(
                    "Failed to delete team (slug) '%s' in organization '%s'. Status: %d. Response: %s",
                    teamSlug, organization, response.statusCode(), response.body());
        }
    }

    @Override
    public void inviteStudentToTeam(String organization, String teamSlug, String githubUsername)
            throws RejectedOperationException, IOException, InterruptedException {

        try (HttpClient client = HttpClient.newHttpClient()) {

            String url = String.format("https://api.github.com/orgs/%s/teams/%s/memberships/%s",
                    organization, teamSlug, githubUsername);
            HttpRequest request = createHttpRequestBuilder(url)
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // GitHub returns 201 if added; 200 if already a member. Treat both as success.
            if (response.statusCode() == 201 || response.statusCode() == 200)
                return;

            throw new RejectedOperationException(
                    "Failed to add user '%s' to team (slug) '%s' in organization '%s'. Status: %d. Response: %s",
                    githubUsername, teamSlug, organization, response.statusCode(), response.body());
        }
    }

    @Override
    public void removeStudentFromTeam(String organization, String teamSlug, String githubUsername)
            throws RejectedOperationException, IOException, InterruptedException {

        try (HttpClient client = HttpClient.newHttpClient()) {

            String url = String.format("https://api.github.com/orgs/%s/teams/%s/memberships/%s", organization,
                    teamSlug, githubUsername);
            HttpRequest request = createHttpRequestBuilder(url)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // GitHub returns 204 if removed; 404 if the user is not a member. Treat both as success.
            if (response.statusCode() == 204 || response.statusCode() == 404)
                return;

            throw new RejectedOperationException(
                    "Failed to remove user '%s' from team (slug) '%s' in organization '%s'. Status: %d. Response: %s",
                    githubUsername, teamSlug, organization, response.statusCode(), response.body());
        }
    }

    @Override
    public List<String> getTeamMembers(String organization, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<String> members = new ArrayList<>();

        try (HttpClient client = HttpClient.newHttpClient()) {
            String url = String.format("https://api.github.com/orgs/%s/teams/%s/members", organization, teamSlug);
            HttpRequest request = createHttpRequestBuilder(url)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RejectedOperationException(
                        "Failed to get team members for organization '%s'. Status: %d. Response: %s",
                        organization, response.statusCode(), response.body());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            if (!root.isArray())
                throw new UnexpectedFormatException(
                        "Expected a JSON array for team members, got: %s", root.getNodeType());

            for (JsonNode node : root) {
                JsonNode loginNode = node.get("login");
                if (loginNode == null || !loginNode.isTextual())
                    throw new UnexpectedFormatException(
                            "Expected 'login' field of type string in each member object, got: %s", node);

                members.add(loginNode.asText());
            }
            return members;
        }
    }

    @Override
    public List<String> getTeamInvitations(String organization, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<String> invites = new ArrayList<>();

        try (HttpClient client = HttpClient.newHttpClient()) {
            String url = String.format("https://api.github.com/orgs/%s/teams/%s/invitations", organization, teamSlug);
            HttpRequest request = createHttpRequestBuilder(url).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RejectedOperationException(
                        "Failed to get team invitations for organization '%s'. Status: %d. Response: %s",
                        organization, response.statusCode(), response.body());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            if (!root.isArray())
                throw new UnexpectedFormatException(
                        "Expected a JSON array for team invitations, got: %s", root.getNodeType());

            for (JsonNode node : root) {
                JsonNode inviteeLogin = node.get("login");
                if (!inviteeLogin.isTextual())
                    throw new UnexpectedFormatException(
                            "Expected 'login' field of type string in each invitation object, got: %s", node);

                invites.add(inviteeLogin.asText());
            }
            return invites;
        }
    }

    //# Auxiliary methods -----------------------------------

    private Builder createHttpRequestBuilder(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json");
    }

}
