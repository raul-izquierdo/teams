package es.uniovi.raul.teams.csv;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.Test;

import es.uniovi.raul.teams.main.ListCollector;
import es.uniovi.raul.teams.model.Student;

class ModelLoaderTest {
    @Test
    void testReadStudentsWithReader() throws Exception {
        String csv = """
                "identifier","github_username","github_id","name"
                "i01-yaagma","yaagma","40261856",""
                "i02-cesar acebal","","",""
                "i02-yaagma","yaagma","40261856",""
                """;

        csv = stripLeadingSpaces(csv);
        Reader reader = new StringReader(csv);
        ListCollector collector = new ListCollector();
        ModelLoader.readStudents(reader, collector);
        List<Student> students = collector.getStudents();

        assertEquals(2, students.size()); // Only students with github_username should be collected

        var student0 = students.get(0);
        assertEquals("i01-yaagma", student0.studentId());
        assertEquals("yaagma", student0.githubUsername());
        assertEquals("i01", student0.groupId());

        var student1 = students.get(1);
        assertEquals("i02-yaagma", student1.studentId());
        assertEquals("yaagma", student1.githubUsername());
        assertEquals("i02", student1.groupId());
    }

    @Test
    void testReadStudentsMissingColumnThrows() {
        String csv = """
                "identifier","github_id","name"
                "i01-yaagma","40261856",""
                """;
        csv = stripLeadingSpaces(csv);
        Reader reader = new StringReader(csv);
        ListCollector collector = new ListCollector();
        assertThrows(IllegalArgumentException.class, () -> {
            ModelLoader.readStudents(reader, collector);
        });
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "\"identifier\",\"github_username\",\"github_id\",\"name\"\n\"i01yaagma\",\"yaagma\"",
            "\"identifier\",\"github_username\",\"github_id\",\"name\"\n\"-\",\"yaagma\"",
            "\"identifier\",\"github_username\",\"github_id\",\"name\"\n\"01-\",\"yaagma\"",
            "\"identifier\",\"github_username\",\"github_id\",\"name\"\n\"-01\",\"yaagma\""
    })
    void testReadMalformedStudentIdParameterized(String csv) {
        csv = stripLeadingSpaces(csv);
        Reader reader = new StringReader(csv);
        ListCollector collector = new ListCollector();
        assertThrows(Exception.class, () -> {
            ModelLoader.readStudents(reader, collector);
        });
    }

    // Multiline strings can't be used directly when parsing CSV due to some leading spaces problems
    private String stripLeadingSpaces(String multiline) {
        return Arrays.stream(multiline.split("\n"))
                .map(String::stripLeading)
                .filter(line -> !line.isBlank())
                .collect(java.util.stream.Collectors.joining("\n"));
    }
}
