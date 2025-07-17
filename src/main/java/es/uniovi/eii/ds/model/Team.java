package es.uniovi.eii.ds.model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private final String name;
    private final List<Student> students = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void addStudent(Student student) {
        students.add(student);
    }
}
