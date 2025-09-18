package es.uniovi.raul.teams.github;

import java.io.IOException;
import java.util.*;

/**
 * Interface for interacting with the GitHub API to manage teams and their members within an organization.
 */
public interface GithubConnection {

    /**
     * Downloads the list of teams from the specified organization.
     *
     * @param organization Organization name
     * @return List of teams in the organization
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws UnexpectedFormatException if the response format is unexpected
     */
    List<Team> getTeams(String organization)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException;

    /**
     * Creates a new team in the specified organization with the given display name.
     * If the team already exists, it returns an empty Optional.
     *
     * @param organization   Organization name
     * @param teamDisplayName Display name for the new team
     * @return Optional containing the created team slug or empty if the team already exists
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws UnexpectedFormatException if the response format is unexpected
     */
    Optional<String> createTeam(String organization, String teamDisplayName)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException;

    /**
     * Removes a team from the specified organization.
     * If the team already exists, it returns an empty Optional.
     *
     * @param organization   Organization name
     * @param teamSlug       Slug of the team to remove
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws UnexpectedFormatException if the response format is unexpected
     */
    void deleteTeam(String organization, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException;

    /**
     * Invites a student to a team in the specified organization.
     * <p>
     * If the operation is successful (student is invited or already a member), the method returns normally.
     * If the operation is rejected by the GitHub API, a {@link RejectedOperationException} is thrown.
     * <p>
     *
     * @param organization   Organization name
     * @param teamSlug       Slug of the team to which the student will be invited
     * @param githubUsername GitHub username of the student
     * @throws IOException if a network error occurs
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws InterruptedException if the operation is interrupted
     */
    void inviteStudentToTeam(String organization, String teamSlug, String githubUsername)
            throws RejectedOperationException, IOException, InterruptedException;

    /**
    * Removes a student from a team in the specified organization.
    * <p>
    * If the operation is successful (student is removed or was not a member), the method returns normally.
    * If the operation is rejected by the GitHub API, a {@link RejectedOperationException} is thrown.
    * <p>
    *
    * @param organization   Organization name
    * @param teamSlug       Slug of the team from which the student will be removed
    * @param githubUsername GitHub username of the student
    * @throws IOException if a network error occurs
    * @throws RejectedOperationException if the operation is rejected by GitHub API
    * @throws InterruptedException if the operation is interrupted
    */
    void removeStudentFromTeam(String organization, String teamSlug, String githubUsername)
            throws RejectedOperationException, IOException, InterruptedException;

    /**
    * Returns a list of GitHub usernames (logins) for the members of a given team in the specified organization.
    *
    * @param organization Organization name
    * @param teamSlug     Slug of the team
    * @return List of GitHub usernames (logins) in the team
    * @throws IOException if a network error occurs
    * @throws RejectedOperationException if the operation is rejected by GitHub API
    * @throws UnexpectedFormatException if the response format is unexpected
    * @throws InterruptedException if the operation is interrupted
    */
    List<String> getTeamMembers(String organization, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException;

    /**
    * Exception thrown when the response format is unexpected. Probably the format has changed and this code needs to be updated.
    */
    class UnexpectedFormatException extends Exception {
        public UnexpectedFormatException(String message) {
            super(message);
        }
    }

    /**
    * Exception thrown when the operation is rejected by GitHub API.
    */
    class RejectedOperationException extends Exception {
        public RejectedOperationException(String message) {
            super(message);
        }
    }
}
