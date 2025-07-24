package es.uniovi.raul.teams.model;

public record Student(String identifier, String githubUsername) {
    public boolean hasGithubUsername() {
        return githubUsername != null && !githubUsername.isBlank();
    }
}
