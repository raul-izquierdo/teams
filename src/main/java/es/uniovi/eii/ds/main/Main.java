package es.uniovi.eii.ds.main;

import es.uniovi.eii.ds.csv.ModelLoader;
import es.uniovi.eii.ds.github.GithubApi;
import es.uniovi.eii.ds.model.Team;

import static es.uniovi.eii.ds.cli.CommandLine.*;

import java.util.*;

public class Main {
    public static void main(String[] args) {

        Map<String, String> cliArguments = getCommandLineArguments(args);
        if (!validArguments(cliArguments)) {
            printHelp();
            return;
        }

        String token = cliArguments.get("-t");
        String organization = cliArguments.getOrDefault("-o", "eii");
        String csvFile = cliArguments.get("csv");
        List<Team> teams = ModelLoader.loadModel(csvFile);
        if (teams.isEmpty())
            return;

        GitHubResult result = createGroups(token, organization, teams);
        if (result == null)
            return;

        printSummary(result);
    }

    private static GitHubResult createGroups(String token, String org, List<Team> teams) {
        GithubApi github = new GithubApi(token, org);

        Set<String> createdTeams = new HashSet<>();
        Set<String> alreadyCreatedTeams = new HashSet<>();
        List<String> addedStudents = new ArrayList<>();

        int alreadyInTeam = 0;
        int missingUsername = 0;
        int rejected = 0;
        try {
            Set<String> existingTeams = github.getExistingTeams();

            for (Team team : teams) {
                String teamName = team.getName();
                if (existingTeams.contains(teamName))
                    alreadyCreatedTeams.add(teamName);
                else {
                    var result = github.createTeam(teamName);
                    if (result == GithubApi.ApiResult.OK)
                        createdTeams.add(teamName);
                    else if (result == GithubApi.ApiResult.ALREADY_EXISTS)
                        alreadyCreatedTeams.add(teamName);
                    // else REJECTED: already logged
                }
            }

            for (Team team : teams) {
                for (var student : team.getStudents()) {
                    if (!student.hasGithubUsername()) {
                        missingUsername++;
                        continue;
                    }

                    var result = github.addStudentToTeam(team.getName(), student.getGithubUsername());
                    if (result == GithubApi.ApiResult.OK)
                        addedStudents.add(student.getIdentifier() + " -> " + team.getName());
                    else if (result == GithubApi.ApiResult.ALREADY_EXISTS)
                        alreadyInTeam++;
                    else if (result == GithubApi.ApiResult.REJECTED)
                        rejected++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error with GitHub API: " + e.getMessage());
            return null;
        }
        return new GitHubResult(createdTeams, alreadyCreatedTeams, addedStudents, alreadyInTeam, missingUsername,
                rejected);
    }

    private static record GitHubResult(
            Set<String> createdTeams,
            Set<String> alreadyCreatedTeams,
            List<String> addedStudents,
            int alreadyInTeam,
            int missingUsername,
            int rejected) {
    }

    private static void printSummary(GitHubResult result) {
        System.out.println("""
                ## Groups
                - Created teams: %s
                - Already created teams: %s
                """.formatted(
                String.join(", ", result.createdTeams),
                String.join(", ", result.alreadyCreatedTeams)));

        System.out.println("## Students\n- Added students:");
        for (String student : result.addedStudents)
            System.out.println("   " + student);

        System.out.println("""

                - Skipped students:
                   Already in team: %d
                   Missing GitHub username: %d
                   API rejected: %d
                """.formatted(result.alreadyInTeam, result.missingUsername, result.rejected));
    }

}
