package es.uniovi.eii.ds.model;

public record Student(String identifier, String githubUsername) {
    public boolean hasGithubUsername() {
        return githubUsername != null && !githubUsername.isBlank();
    }
}
