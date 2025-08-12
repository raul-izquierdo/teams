package es.uniovi.raul.teams.model;

/**
 * Represents a student with a student ID and GitHub username.
 *
 * @param rosterId       The unique identifier for the student, which must contain a hyphen.
 * @param githubUsername  The GitHub username of the student.
 */
public record Student(String rosterId, String githubUsername) {
    public Student {
        if (rosterId == null || !rosterId.matches(".+-.+"))
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
        int hyphenIndex = rosterId.indexOf('-');

        return rosterId.substring(0, hyphenIndex);
    }
}
