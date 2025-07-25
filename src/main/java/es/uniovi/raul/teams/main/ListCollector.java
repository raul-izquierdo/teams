package es.uniovi.raul.teams.main;

import static java.lang.String.*;

import java.util.*;

import es.uniovi.raul.teams.csv.StudentsCollector;
import es.uniovi.raul.teams.model.Student;

public final class ListCollector implements StudentsCollector {

    private List<Student> students = new ArrayList<>();

    @Override
    public void collectStudentData(long line, String studentId, String githubUsername) {
        try {

            // Only interested in students with github usernames
            if (githubUsername == null || githubUsername.isBlank())
                return;

            students.add(new Student(studentId, githubUsername));

        } catch (IllegalArgumentException e) {
            // Add line number to the exception message
            throw new IllegalArgumentException(format("Line %d. %s", line, e.getMessage()), e);
        }
    }

    public List<Student> getStudents() {
        return students;
    }

}
