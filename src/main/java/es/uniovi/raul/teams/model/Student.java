package es.uniovi.raul.teams.model;

public record Student(String studentId, String githubUsername) {
    public Student {
        if (studentId == null || !studentId.matches(".+-.+"))
            throw new IllegalArgumentException("Student ID must contain a hyphen with characters on both sides.");

        if (githubUsername == null || githubUsername.isBlank())
            throw new IllegalArgumentException("GitHub username cannot be null or blank.");

    }

    /**
     * Returns the group ID extracted from the student ID.
     * The group ID is the part of the student ID before the hyphen.
     * Example: "1234-5678" -> "1234"
     */
    public String groupId() {

        // hyphenIndex should always be valid due to the constructor validation
        int hyphenIndex = studentId.indexOf('-');

        return studentId.substring(0, hyphenIndex);
    }
}
