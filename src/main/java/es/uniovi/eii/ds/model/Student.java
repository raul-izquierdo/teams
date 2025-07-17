package es.uniovi.eii.ds.model;

public class Student {
    private final String identifier;
    private final String githubUsername;

    public Student(String identifier, String githubUsername) {
        this.identifier = identifier;
        this.githubUsername = githubUsername;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public boolean hasGithubUsername() {
        return githubUsername != null && !githubUsername.isBlank();
    }
}
