package es.uniovi.raul.teams.organization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uniovi.raul.teams.github.*;
import es.uniovi.raul.teams.github.GithubConnection.*;

@ExtendWith(MockitoExtension.class)
class OrganizationDeleteGroupTeamsTest {

    @Mock
    private GithubConnection github;

    @Test
    void deletes_only_group_teams()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        when(github.getTeams("org")).thenReturn(List.of(
                new Team("group A", "group-a"),
                new Team("group B", "group-b"),
                new Team("some other", "other")));

        when(github.getTeamMembers("org", "group-a")).thenReturn(List.of());
        when(github.getTeamMembers("org", "group-b")).thenReturn(List.of());
        when(github.getTeamInvitations("org", "group-a")).thenReturn(List.of());
        when(github.getTeamInvitations("org", "group-b")).thenReturn(List.of());

        var organization = new Organization("org", github);
        organization.deleteGroupTeams();

        verify(github, times(1)).getTeams("org");
        verify(github, times(1)).getTeamMembers("org", "group-a");
        verify(github, times(1)).getTeamMembers("org", "group-b");
        verify(github, times(1)).getTeamInvitations("org", "group-a");
        verify(github, times(1)).getTeamInvitations("org", "group-b");
        verify(github, times(1)).deleteTeam("org", "group-a");
        verify(github, times(1)).deleteTeam("org", "group-b");

        verifyNoMoreInteractions(github);
    }

    @Test
    void no_group_teams_deleted()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        when(github.getTeams("org")).thenReturn(Collections.emptyList());

        var organization = new Organization("org", github);
        organization.deleteGroupTeams();

        verify(github, times(1)).getTeams("org");

        verifyNoMoreInteractions(github);
    }

    @Test
    void propagates_errors_from_api()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        when(github.getTeams("org")).thenThrow(new GithubConnection.UnexpectedFormatException("boom"));

        var organization = new Organization("org", github);

        assertThrows(UnexpectedFormatException.class, organization::deleteGroupTeams);
    }

    @Test
    void deletes_group_teams_and_removes_members()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        when(github.getTeams("org")).thenReturn(List.of(
                new Team("group A", "group-a"),
                new Team("group B", "group-b")));

        when(github.getTeamMembers("org", "group-a")).thenReturn(List.of("alice", "bob"));
        when(github.getTeamMembers("org", "group-b")).thenReturn(List.of("carol"));

        when(github.getTeamInvitations("org", "group-a")).thenReturn(List.of("david"));
        when(github.getTeamInvitations("org", "group-b")).thenReturn(List.of());

        var organization = new Organization("org", github);
        organization.deleteGroupTeams();

        // Remove members from org for each collected username (alice, bob, carol, david)
        verify(github).removeMemberFromOrganization("org", "alice");
        verify(github).removeMemberFromOrganization("org", "bob");
        verify(github).removeMemberFromOrganization("org", "carol");
        verify(github).removeMemberFromOrganization("org", "david");

        // Finally, delete both teams
        verify(github).deleteTeam("org", "group-a");
        verify(github).deleteTeam("org", "group-b");

        verifyNoMoreInteractions(github);
    }

    @Test
    void continues_when_removal_fails_for_some_users()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        when(github.getTeams("org")).thenReturn(List.of(
                new Team("group A", "group-a")));

        when(github.getTeamMembers("org", "group-a")).thenReturn(List.of("owner", "student"));
        when(github.getTeamInvitations("org", "group-a")).thenReturn(List.of());

        // Simulate failure removing org owner, success for student
        doThrow(new GithubConnection.RejectedOperationException("Cannot remove organization owner"))
                .when(github).removeMemberFromOrganization("org", "owner");

        var organization = new Organization("org", github);
        organization.deleteGroupTeams();

        // Both removal attempts should have been made
        verify(github).removeMemberFromOrganization("org", "owner");
        verify(github).removeMemberFromOrganization("org", "student");

        // Team still deleted despite error for owner
        verify(github).deleteTeam("org", "group-a");

        verifyNoMoreInteractions(github);
    }
}
