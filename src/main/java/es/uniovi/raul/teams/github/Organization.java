package es.uniovi.raul.teams.github;

import static es.uniovi.raul.teams.cli.CommandLine.*;

import java.util.*;

import es.uniovi.raul.teams.model.*;

public class Organization {
    public static ProcessResult updateOrganization(String token, String org, List<Team> teams) {
        GithubApi github = new GithubApi(token, org);
        ProcessResult result = new ProcessResult();
        try {
            createTeams(teams, github, result);
            addStudentsToTeams(teams, github, result);
        } catch (Exception e) {
            printError("Error with GitHub API: " + e.getMessage());
        }
        return result;
    }

    private static void createTeams(List<Team> teams, GithubApi github, ProcessResult result)
            throws Exception {

        Set<String> existingTeams = github.getExistingTeams();

        // Add already existing teams to the result
        existingTeams.forEach(teamName -> result.alreadyCreatedTeams().add(new Team(teamName)));

        for (Team team : teams) {
            String teamName = team.getName();
            if (existingTeams.contains(teamName))
                continue;

            var apiResult = github.createTeam(teamName);
            if (apiResult == GithubApi.ApiResult.OK)
                result.createdTeams().add(team);
            else if (apiResult == GithubApi.ApiResult.REJECTED)
                result.rejectedTeams().add(team);
        }
    }

    private static void addStudentsToTeams(List<Team> teams, GithubApi github, ProcessResult result)
            throws Exception {

        for (Team team : teams) {
            for (var student : team.getStudents()) {
                if (!student.hasGithubUsername()) {
                    result.missingUsername().add(student);
                    continue;
                }
                var apiResult = github.addStudentToTeam(team.getName(), student.githubUsername());
                if (apiResult == GithubApi.ApiResult.OK)
                    result.addedStudents().add(student);
                else if (apiResult == GithubApi.ApiResult.ALREADY_EXISTS)
                    result.alreadyInTeam().add(student);
                else if (apiResult == GithubApi.ApiResult.REJECTED)
                    result.rejectedStudents().add(student);
            }
        }
    }

    public static record ProcessResult(
            List<Team> createdTeams,
            List<Team> alreadyCreatedTeams,
            List<Team> rejectedTeams,
            List<Student> addedStudents,
            List<Student> alreadyInTeam,
            List<Student> missingUsername,
            List<Student> rejectedStudents) {
        public ProcessResult() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
    }
}
