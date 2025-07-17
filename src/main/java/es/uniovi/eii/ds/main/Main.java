package es.uniovi.eii.ds.main;

import static es.uniovi.eii.ds.cli.CommandLine.*;

import java.util.*;
import java.util.stream.Collectors;

import es.uniovi.eii.ds.csv.ModelLoader;
import es.uniovi.eii.ds.github.Organization;
import es.uniovi.eii.ds.github.Organization.ProcessResult;
import es.uniovi.eii.ds.model.*;

public class Main {
    public static void main(String[] args) {

        Map<String, String> cliArguments = getCommandLineArguments(args);
        if (!validArguments(cliArguments)) {
            printHelp();
            return;
        }

        String token = cliArguments.get("-t");
        String organization = cliArguments.get("-o");
        String csvFile = cliArguments.get("csv");
        List<Team> teams = ModelLoader.loadModel(csvFile);
        if (teams.isEmpty())
            return;

        var result = Organization.updateOrganization(token, organization, teams);

        printSummary(result);
    }

    private static void printSummary(ProcessResult result) {
        System.out.println("\n## Groups");
        System.out.println("- Created teams: " + teamNames(result.createdTeams()));
        System.out.println("- Already created teams: " + teamNames(result.alreadyCreatedTeams()));
        System.out.println("- Team creations rejected: " + teamNames(result.rejectedTeams()));

        System.out.print("\n## Students\n- Added students: ");
        if (result.addedStudents().isEmpty())
            System.out.println("0");
        else {
            System.out.println();
            for (Student student : result.addedStudents())
                System.out.println("   " + student.identifier());
        }
        System.out.println("- Already in team: " + result.alreadyInTeam().size());
        System.out.println("- Missing GitHub usernames: " + result.missingUsername().size());
        System.out.print("- Student additions rejected: ");
        if (result.rejectedStudents().isEmpty())
            System.out.println("0");
        else {
            System.out.println();
            for (Student student : result.rejectedStudents())
                System.out.println("      " + student.identifier() + ", " + student.githubUsername());
        }
    }

    private static String teamNames(List<Team> teams) {
        return textOrZero(joinTeamNames(teams));
    }

    private static String joinTeamNames(List<Team> teams) {
        return teams.stream()
                .map(Team::getName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private static String textOrZero(String text) {
        return text == null || text.isEmpty() ? "0" : text;
    }

}
