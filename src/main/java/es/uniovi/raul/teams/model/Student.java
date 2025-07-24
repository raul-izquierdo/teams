package es.uniovi.raul.teams.model;

public record Student(String studentId, String githubUsername) {
    public Student {
        if (studentId == null || !studentId.matches(".+_.+"))
            throw new IllegalArgumentException("Student ID must contain an underscore with characters on both sides.");
    }

    public boolean hasGithubUsername() {
        return githubUsername != null && !githubUsername.isBlank();
    }
}
