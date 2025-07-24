package es.uniovi.raul.teams.csv;

import static es.uniovi.raul.teams.cli.CommandLine.*;

import java.io.*;
import java.util.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import es.uniovi.raul.teams.model.*;

public class ModelLoader {

    public static List<Team> loadModel(String csvFile) {
        try (java.io.Reader reader = new java.io.FileReader(csvFile)) {
            return createModel(reader);
        } catch (Exception e) {
            System.err.println("Error reading CSV: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public static List<Team> createModel(Reader reader) throws Exception {

        Map<String, Team> teams = new HashMap<>();

        try (CSVParser parser = new CSVParser(reader,
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {

            for (CSVRecord csvRecord : parser) {

                String identifier = csvRecord.get("identifier");
                if (identifier == null || identifier.length() < 2) {
                    printError("Error in csv line %d: identifier '%s' is too short or missing%n",
                            csvRecord.getRecordNumber(), identifier);
                    continue;
                }

                String githubUsername = csvRecord.get("github_username");

                String groupCode = "G" + identifier.substring(0, 2);
                Team team = teams.computeIfAbsent(groupCode, Team::new);

                team.addStudent(new Student(identifier, githubUsername));
            }
        }
        return new ArrayList<>(teams.values());
    }

}
