package es.uniovi.raul.teams.roster;

import java.io.*;

import org.apache.commons.csv.*;

public class Roster {

    public static void readStudents(String csvFile, StudentsCollector collector) throws IOException {

        var reader = new java.io.FileReader(csvFile);
        readStudents(reader, collector);
        reader.close();
    }

    public static void readStudents(Reader reader, StudentsCollector collector) throws IOException {
        try (CSVParser parser = new CSVParser(reader,
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {

            for (CSVRecord csvRecord : parser) {

                String rosterId = csvRecord.get("identifier");
                String githubUsername = csvRecord.get("github_username");

                collector.collectStudentData(csvRecord.getRecordNumber(), rosterId, githubUsername);
            }
        }
    }
}
