package es.uniovi.raul.teams.main;

import static es.uniovi.raul.teams.cli.CommandLine.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import es.uniovi.raul.teams.csv.*;
import es.uniovi.raul.teams.github.Organization;
import es.uniovi.raul.teams.github.Organization.ProcessResult;
import es.uniovi.raul.teams.model.*;

public class Main {
    public static void main(String[] args) {

        Map<String, String> cliArguments = getCommandLineArguments(args);
        if (!validArguments(cliArguments)) {
            printHelp();
            return;
        }

        var teamNameProvider = new PrefixBasedStrategy(cliArguments.get(TEAMS_PREFIX_FLAG));
        TeamsCollector teamsCollector = new TeamsCollector(teamNameProvider);
        try {
            ModelLoader.readStudents(cliArguments.get("csv"), teamsCollector);

            if (teamsCollector.hasErrors()) {
                System.err.println("Errors occurred while reading the CSV file. Please fix them and try again.");
                return;
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            return;
        }

        String token = cliArguments.get(TOKEN_FLAG);
        String organization = cliArguments.get(ORGANIZATION_FLAG);
        var result = Organization.updateOrganization(token, organization, teamsCollector.getTeams());

        printSummary(result);

        printCredits();
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
                System.out.println("   " + student.studentId());
        }
        System.out.println("- Already in team: " + result.alreadyInTeam().size());
        System.out.println("- Missing GitHub usernames: " + result.missingUsername().size());
        System.out.print("- Student additions rejected: ");
        if (result.rejectedStudents().isEmpty())
            System.out.println("0");
        else {
            System.out.println();
            for (Student student : result.rejectedStudents())
                System.out.println("      " + student.studentId() + ", " + student.githubUsername());
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

class PrefixBasedStrategy implements TeamNamingStrategy {
    private final String prefix;

    PrefixBasedStrategy(String prefix) {
        if (prefix == null)
            throw new IllegalArgumentException("Prefix cannot be null");
        this.prefix = prefix;
    }

    @Override
    public String getTeamName(String groupId) {
        return prefix + groupId;
    }
}
