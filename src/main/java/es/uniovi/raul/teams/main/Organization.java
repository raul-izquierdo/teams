package es.uniovi.raul.teams.main;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import es.uniovi.raul.teams.github.*;
import es.uniovi.raul.teams.github.GithubConnection.*;
import es.uniovi.raul.teams.roster.Student;

/**
 * Represents a GitHub organization and provides methods to synchronize teams and members
 * with a list of students and their group assignments.
 */
public final class Organization {

    private String organizationName;
    private GithubConnection githubApi;

    public Organization(String organizationName, GithubConnection githubApi) {
        if (githubApi == null)
            throw new IllegalArgumentException("GithubApi cannot be null.");
        if (organizationName == null || organizationName.isBlank())
            throw new IllegalArgumentException("Organization cannot be null or blank.");

        this.organizationName = organizationName;
        this.githubApi = githubApi;
    }

    /**
     * Updates the organization to ensure that its teams and their members match the provided list of students.
     * <p>
     * This method performs the following actions:
     * <ul>
     *   <li>Ensures that there is a team for each group represented by the students, creating new teams if necessary and removing teams that are no longer needed.</li>
     *   <li>Updates the membership of each team so that it matches the students assigned to each group.</li>
     * </ul>
     *
     * @param students the list of students whose group assignments should be reflected in the organization
     * @throws IOException if a network or I/O error occurs
     * @throws RejectedOperationException if an operation is rejected by the GitHub API
     * @throws UnexpectedFormatException if the data format from the GitHub API is unexpected
     * @throws InterruptedException if the operation is interrupted
     */
    public void update(List<Student> students)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        updateTeams(students.stream()
                .map(Student::group)
                .distinct());

        updateTeamsMembers(students);
    }

    /**
     * Deletes all group teams associated with the organization. Other teams not related to groups
     * will remain intact.
     *
     * @throws UnexpectedFormatException if the format of the group teams data is unexpected.
     * @throws RejectedOperationException if the operation is rejected by the GitHub API.
     * @throws IOException if an I/O error occurs during the operation.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void deleteGroupTeams()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        for (Team team : getGroupTeams())
            githubApi.deleteTeam(organizationName, team.slug());
    }

    /**
     * Makes sure that the teams in the organization match the groups in the class.
     * This method will create new teams for groups that do not have a team yet,
     * and remove teams that are no longer needed.
     *
     * Only teams that correspond to groups teams (follow the naming convention) will be removed.
     *
     * @param requiredGroups the stream of group names that should end up as teams
     * @throws UnexpectedFormatException if the format of the data from the GitHub API is unexpected
     * @throws RejectedOperationException if an operation is rejected by the GitHub API
     * @throws IOException if a network or I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public void updateTeams(Stream<String> requiredGroups)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        var existingTeams = getGroupTeams();
        lookForNewTeams(requiredGroups, existingTeams);
        lookForTeamsToRemove(requiredGroups, existingTeams);
    }

    private void updateTeamsMembers(List<Student> students)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        for (Team team : getGroupTeams()) {

            var studentsInTeam = students.stream()
                    .filter(student -> TeamNaming.toTeam(student.group()).equals(team.displayName()));

            updateTeamMembers(team, studentsInTeam);
        }

    }

    private void updateTeamMembers(Team team, Stream<Student> students)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<String> currentMembers = githubApi.getTeamMembers(organizationName, team.slug());
        List<String> studentUsernames = students.map(Student::githubUsername).toList();

        // Add missing students
        for (String username : studentUsernames)
            if (!currentMembers.contains(username))
                githubApi.addStudentToTeam(organizationName, team.slug(), username);

        // Remove extra members
        for (String member : currentMembers)
            if (!studentUsernames.contains(member))
                githubApi.removeStudentFromTeam(organizationName, team.slug(), member);

    }

    private void lookForNewTeams(Stream<String> groups, List<Team> existingTeams)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<String> teamsToCreate = groups
                .map(TeamNaming::toTeam)
                .filter(teamName -> existingTeams.stream()
                        .noneMatch(existingTeam -> existingTeam.displayName().equals(teamName)))
                .toList();

        for (String team : teamsToCreate)
            githubApi.createTeam(organizationName, team);
    }

    private void lookForTeamsToRemove(Stream<String> groups, List<Team> existingTeams)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<Team> teamsToRemove = existingTeams.stream()
                .filter(team -> groups.noneMatch(group -> TeamNaming.toTeam(group).equals(team.displayName())))
                .toList();

        for (Team team : teamsToRemove)
            githubApi.deleteTeam(organizationName, team.slug());
    }

    // Only return the teams that correspond to groups
    private List<Team> getGroupTeams()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        return githubApi.getTeamsInfo(organizationName).stream()
                .filter(team -> TeamNaming.isGroupTeam(team.displayName()))
                .toList();
    }
}
