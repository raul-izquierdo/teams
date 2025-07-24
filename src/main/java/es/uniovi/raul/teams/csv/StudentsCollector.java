package es.uniovi.raul.teams.csv;

@FunctionalInterface
public interface StudentsCollector {
    void collectStudentData(long line, String studentId, String githubUsername);
}
