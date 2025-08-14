package es.uniovi.raul.teams.roster;

/**
 * Represents a student with a student ID and GitHub username.
 *
 * @param name            The name of the student
 * @param group           The group of the student
 * @param rosterId        The unique identifier for the student in the roster
 * @param githubUsername  The GitHub username of the student
 */
public record Student(String name, String group, String rosterId, String githubUsername) {
    public Student {

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name must not be null or empty");

        if (group == null || group.isBlank())
            throw new IllegalArgumentException("Group must not be null or empty");

        if (rosterId == null || rosterId.isBlank())
            throw new IllegalArgumentException("Roster ID must not be null or empty");

        if (githubUsername == null || githubUsername.isBlank())
            throw new IllegalArgumentException("GitHub username cannot be null or blank.");

    }

}
