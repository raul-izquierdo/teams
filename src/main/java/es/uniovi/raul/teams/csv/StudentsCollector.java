package es.uniovi.raul.teams.csv;

/**
 * Interface for collecting student data from a CSV file.
 * Strategy pattern to allow different implementations for collecting student data.
 */
@FunctionalInterface
public interface StudentsCollector {
    void collectStudentData(long line, String studentId, String githubUsername) throws IllegalArgumentException;
}
