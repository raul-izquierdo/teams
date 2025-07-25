
package es.uniovi.raul.teams.github;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.*;

import com.fasterxml.jackson.databind.*;

import es.uniovi.raul.teams.model.Team;

public final class GithubApi {

    public static class UnexpectedFormatException extends Exception {
        public UnexpectedFormatException(String message) {
            super(message);
        }
    }

    public static class RejectedOperationException extends Exception {
        public RejectedOperationException(String message) {
            super(message);
        }
    }

    /**
     * Downloads the list of teams from the specified organization.
     *
     * @param token        GitHub API token
     * @param organization Organization name
     * @return List of teams in the organization
     * @throws UnexpectedFormatException if the response format is unexpected
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     */
    public static List<Team> downloadTeamsInfo(String token, String organization)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<Team> teams = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/orgs/" + organization + "/teams"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RejectedOperationException("Failed to get existing teams. Status: " + response.statusCode()
                    + ". Response: " + response.body());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        if (!root.isArray())
            throw new UnexpectedFormatException("Expected a JSON array for teams, got: " + root.getNodeType());

        for (JsonNode node : root) {
            JsonNode nameNode = node.get("name");
            JsonNode slugNode = node.get("slug");
            if (nameNode == null || !nameNode.isTextual() || slugNode == null || !slugNode.isTextual())
                throw new UnexpectedFormatException(
                        "Expected 'name' and 'slug' fields of type string in each team object, got: "
                                + node.toString());

            teams.add(new Team(nameNode.asText(), slugNode.asText()));
        }
        return teams;
    }

    /**
     * Creates a new team in the specified organization with the given display name.
     * If the team already exists, it returns an empty Optional.
     *
     * @param token          GitHub API token
     * @param organization   Organization name
     * @param teamDisplayName Display name for the new team
     * @return Optional containing the created team slug or empty if the team already exists
     * @throws UnexpectedFormatException if the response format is unexpected
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     */
    public static Optional<String> createTeam(String token, String organization, String teamDisplayName)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        String json = String.format("{\"name\":\"%s\",\"privacy\":\"closed\"}", teamDisplayName);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/orgs/" + organization + "/teams"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
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
                        "Expected 'slug' field of type string in created team object, got: " + root.toString());
            }
            return Optional.of(slugNode.asText());
        }

        if (response.statusCode() == 422) {
            // Team already exists
            return Optional.empty();
        }

        throw new RejectedOperationException(
                "Failed to create team '" + teamDisplayName + "'. Status: " + response.statusCode()
                        + ". Response: " + response.body());
    }

    /**
     * Adds a student to a team in the specified organization.
     * <p>
     * If the operation is successful (student is added or already a member), the method returns normally.
     * If the operation is rejected by the GitHub API, a {@link RejectedOperationException} is thrown.
     * <p>
     *
     * @param token          GitHub API token
     * @param organization   Organization name
     * @param teamSlug       Slug of the team to which the student will be added
     * @param githubUsername GitHub username of the student
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws IOException if a network error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public static void addStudentToTeam(String token, String organization, String teamSlug, String githubUsername)
            throws RejectedOperationException, IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.github.com/orgs/%s/teams/%s/memberships/%s", organization,
                teamSlug, githubUsername);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201 || response.statusCode() == 200)
            return;

        throw new RejectedOperationException(
                "Failed to add user '" + githubUsername + "' to team (slug) '" + teamSlug + "'. Status: "
                        + response.statusCode() + ". Response: " + response.body());
    }
}
