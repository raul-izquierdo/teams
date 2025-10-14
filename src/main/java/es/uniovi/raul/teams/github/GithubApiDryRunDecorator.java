package es.uniovi.raul.teams.github;

import java.io.IOException;
import java.util.*;

/**
 * Decorator for GithubApi that disables write operations while allowing reads.
 * Useful for dry-run mode to see the actions that would be performed without
 * making any changes in GitHub.
 */
public final class GithubApiDryRunDecorator implements GithubApi {

    private final GithubApi delegate;

    public GithubApiDryRunDecorator(GithubApi delegate) {
        if (delegate == null)
            throw new IllegalArgumentException("Delegate cannot be null.");
        this.delegate = delegate;
    }

    // Read operations: delegate
    @Override
    public List<Team> getTeams(String organization)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {
        return delegate.getTeams(organization);
    }

    @Override
    public List<String> getTeamMembers(String organization, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {
        return delegate.getTeamMembers(organization, teamSlug);
    }

    @Override
    public List<String> getTeamInvitations(String organization, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {
        return delegate.getTeamInvitations(organization, teamSlug);
    }

    // Write operations: no-ops
    @Override
    public Optional<String> createTeam(String organization, String teamDisplayName)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {
        // Dry-run: do not create anything. Returning empty Optional is safe for callers in this project.
        return Optional.empty();
    }

    @Override
    public void deleteTeam(String organization, String teamSlug)
            throws IOException, InterruptedException, RejectedOperationException {
        // Dry-run: do nothing
    }

    @Override
    public void inviteStudentToTeam(String organization, String teamSlug, String githubUsername)
            throws RejectedOperationException, IOException, InterruptedException {
        // Dry-run: do nothing
    }

    @Override
    public void removeStudentFromTeam(String organization, String teamSlug, String githubUsername)
            throws RejectedOperationException, IOException, InterruptedException {
        // Dry-run: do nothing
    }

    @Override
    public void removeMemberFromOrganization(String organization, String githubUsername)
            throws RejectedOperationException, IOException, InterruptedException {
        // Dry-run: do nothing
    }
}
