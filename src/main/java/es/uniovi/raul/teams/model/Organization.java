package es.uniovi.raul.teams.model;

import java.io.IOException;
import java.util.*;

import es.uniovi.raul.teams.github.GithubApi;
import es.uniovi.raul.teams.github.GithubApi.*;

public final class Organization {

    private String token;
    private String name;

    public Organization(String token, String name) {
        if (token == null || token.isBlank())
            throw new IllegalArgumentException("Token cannot be null or blank.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Organization cannot be null or blank.");

        this.token = token;
        this.name = name;
    }

    public void createTeams(List<String> teamDisplayNames)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        for (String displayName : teamDisplayNames) {

            var teamSlug = GithubApi.createTeam(token, name, displayName);

            teamSlug.ifPresent(slug -> {
                System.out.printf("Team '%s' created successfully%n", displayName);
            });
        }
    }

    public List<Team> downloadTeamsInfo()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        return GithubApi.downloadTeamsInfo(token, name);
    }

    public void addStudentsToTeams(List<Student> students, Group2TeamMapper teamMapper)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<Team> teams = downloadTeamsInfo();

        for (Student student : students) {

            findTeam(teams, teamMapper.getTeamDisplayName(student.groupId()))
                    .ifPresentOrElse(
                            team -> {
                                try {
                                    GithubApi.addStudentToTeam(token, name, team.slug(), student.githubUsername());
                                } catch (Exception e) {
                                    System.err.printf("ERROR. Failed to add student '%s' to team '%s': %s%n",
                                            student.rosterId(), team.displayName(), e.getMessage());
                                }
                            },
                            () -> System.err.printf("No team found for group ID '%s'. Skipping student '%s'.%n",
                                    student.groupId(), student.rosterId()));
        }

    }

    private Optional<Team> findTeam(List<Team> teams, String teamDisplayName) {
        return teams.stream()
                .filter(team -> team.displayName().equals(teamDisplayName))
                .findFirst();
    }

}
