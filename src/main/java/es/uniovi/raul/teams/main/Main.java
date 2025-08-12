package es.uniovi.raul.teams.main;

import static es.uniovi.raul.teams.cli.CommandLine.*;

import java.io.IOException;
import java.util.Map;

import es.uniovi.raul.teams.model.*;
import es.uniovi.raul.teams.roster.Roster;

public class Main {
    public static void main(String[] args) {

        Map<String, String> cliArguments = getCommandLineArguments(args);
        if (!validArguments(cliArguments)) {
            printHelp();
            return;
        }

        ListCollector studentsCollector = new ListCollector();
        try {
            Roster.readStudents(cliArguments.get("csv"), studentsCollector);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid values in CSV file. Please fix them and try again.");
            System.err.println(e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("I/O error reading CSV file: " + e.getMessage());
            return;
        }

        var students = studentsCollector.getStudents();

        var teamNameGenerator = new PrefixBasedStrategy(cliArguments.get(TEAMS_PREFIX_FLAG));

        var teamDisplayNames = students.stream()
                .map(Student::groupId)
                .distinct()
                .map(teamNameGenerator::getTeamDisplayName)
                .sorted()
                .toList();

        var organization = new Organization(cliArguments.get(TOKEN_FLAG), cliArguments.get(ORGANIZATION_FLAG));

        try {
            organization.createTeams(teamDisplayNames);
        } catch (Exception e) {
            System.err.println("Error creating teams: " + e.getMessage());
            return;
        }

        try {
            organization.addStudentsToTeams(students, teamNameGenerator);
        } catch (Exception e) {
            System.err.printf("Error adding students to teams: %s%n", e.getMessage());
            return;
        }

        System.out.println("All operations completed successfully.");
        printCredits();

    }

}
