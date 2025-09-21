package es.uniovi.raul.teams.organization;

import static es.uniovi.raul.teams.organization.TeamNaming.*;
import static java.lang.String.*;

import java.io.IOException;
import java.util.List;

import es.uniovi.raul.teams.github.GithubConnection;
import es.uniovi.raul.teams.github.GithubConnection.*;
import es.uniovi.raul.teams.roster.Student;

/**
 * Represents a GitHub organization and provides methods to synchronize teams and members
 * with a list of students and their group assignments.
 */
public final class Organization {

    private String organizationName;
    private GithubConnection githubApi;
    private Logger logger;

    public Organization(String organizationName, GithubConnection githubApi) {
        this(organizationName, githubApi, new ConsoleLogger());
    }

    public Organization(String organizationName, GithubConnection githubApi, Logger logger) {
        if (githubApi == null)
            throw new IllegalArgumentException("GithubApi cannot be null.");
        if (organizationName == null || organizationName.isBlank())
            throw new IllegalArgumentException("Organization cannot be null or blank.");
        if (logger == null)
            throw new IllegalArgumentException("Logger cannot be null.");

        this.organizationName = organizationName;
        this.githubApi = githubApi;
        this.logger = logger;
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
    public void updateWith(List<Student> students)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        updateTeams(students.stream()
                .map(Student::group)
                .distinct()
                .toList());

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
        var groupTeams = getGroupTeams();
        if (groupTeams.isEmpty())
            return;

        // Collect all usernames that belong to any group team (members and pending invitations)
        var usernamesToRemove = new java.util.HashSet<String>();

        for (var team : groupTeams) {
            var members = githubApi.getTeamMembers(organizationName, team.slug());
            usernamesToRemove.addAll(members);

            var invites = githubApi.getTeamInvitations(organizationName, team.slug());
            usernamesToRemove.addAll(invites);
        }

        // Remove users from the organization (idempotent if already not members)
        for (var login : usernamesToRemove) {
            try {
                githubApi.removeMemberFromOrganization(organizationName, login);
                logger.log(String.format("[Removed member from org] '%s'", login));
            } catch (RejectedOperationException e) {
                logger.log(String.format("[WARNING] Could not remove member '%s' from organization '%s': %s",
                        login, organizationName, e.getMessage()));
                // Continue with next member
            }
        }

        // Finally, delete all group teams
        for (var team : groupTeams) {
            githubApi.deleteTeam(organizationName, team.slug());
            logger.log("[Deleted team] " + team.displayName());
        }
    }

    /**
     * Makes sure that the teams in the organization match the groups in the class.
     * This method will create new teams for groups that do not have a team yet,
     * and remove teams that are no longer needed.
     *
     * Only teams that correspond to groups teams (follow the naming convention) will be removed.
     */
    private void updateTeams(List<String> requiredGroups)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        var existingTeams = getGroupTeams();
        lookForNewTeams(requiredGroups, existingTeams);
        lookForTeamsToRemove(requiredGroups, existingTeams);
    }

    /**
     * Updates the members of each team based on the provided list of students.
     */
    private void updateTeamsMembers(List<Student> students)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        for (var team : getGroupTeams()) {

            var studentsInTeam = students.stream()
                    .filter(student -> student.group().equals(team.group()))
                    .toList();

            updateTeamMembers(team, studentsInTeam);
        }

    }

    private void updateTeamMembers(GroupTeam team, List<Student> students)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<String> alreadyInvited = githubApi.getTeamMembers(organizationName, team.slug());
        alreadyInvited.addAll(githubApi.getTeamInvitations(organizationName, team.slug()));

        for (var student : students)
            if (!alreadyInvited.contains(student.login())) {
                githubApi.inviteStudentToTeam(organizationName, team.slug(), student.login());
                logger.log(format("[Invited student] '%s' to team '%s'", student.name(), team.displayName()));
            }

        // Remove extra members
        List<String> studentsLogin = students.stream().map(Student::login).toList();
        for (String existingLogin : alreadyInvited)
            if (!studentsLogin.contains(existingLogin)) {
                try {
                    githubApi.removeStudentFromTeam(organizationName, team.slug(), existingLogin);
                    logger.log(format("[Removed student] '%s' from team '%s'", existingLogin, team.displayName()));
                } catch (RejectedOperationException e) {
                    logger.log(format("[WARNING] Could not remove '%s' from team '%s': %s",
                            existingLogin, team.displayName(), e.getMessage()));
                    // Continue with next member
                }
            }
    }

    private void lookForNewTeams(List<String> groups, List<GroupTeam> existingTeams)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<String> teamsToCreate = groups.stream()
                .map(TeamNaming::toTeam)
                .filter(teamName -> existingTeams.stream()
                        .noneMatch(existingTeam -> existingTeam.displayName().equals(teamName)))
                .toList();

        for (String team : teamsToCreate) {
            githubApi.createTeam(organizationName, team);
            logger.log(format("[Created team] '%s'", team));
        }
    }

    private void lookForTeamsToRemove(List<String> groups, List<GroupTeam> existingTeams)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<GroupTeam> teamsToRemove = existingTeams.stream()
                .filter(team -> groups.stream().noneMatch(team::isAssociatedWith))
                .toList();

        for (var team : teamsToRemove) {
            githubApi.deleteTeam(organizationName, team.slug());
            logger.log(format("[Removed team] '%s'", team.displayName()));
        }
    }

    // Only return the teams that correspond to groups
    private List<GroupTeam> getGroupTeams()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        return githubApi.getTeams(organizationName).stream()
                .filter(team -> isGroupTeam(team.displayName()))
                .map(team -> new GroupTeam(team.displayName(), team.slug(), toGroup(team.displayName())))
                .toList();
    }
}

record GroupTeam(String displayName, String slug, String group) {

    boolean isAssociatedWith(String otherGroup) {
        return this.group.equals(otherGroup);
    }
}
