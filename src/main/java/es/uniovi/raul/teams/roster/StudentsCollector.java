package es.uniovi.raul.teams.roster;

/**
 * Interface for collecting student data from a CSV file.
 * Strategy pattern to allow different implementations for collecting student data.
 */
@FunctionalInterface
public interface StudentsCollector {
    void collectStudentData(long line, String rosterId, String githubUsername) throws IllegalArgumentException;
}
