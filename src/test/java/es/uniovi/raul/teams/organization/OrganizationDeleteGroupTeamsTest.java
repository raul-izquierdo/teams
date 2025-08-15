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

        when(github.getTeamsInfo("org")).thenReturn(List.of(
                new Team("group A", "group-a"),
                new Team("group B", "group-b"),
                new Team("some other", "other")));

        var organization = new Organization("org", github);
        organization.deleteGroupTeams();

        verify(github, times(1)).getTeamsInfo("org");
        verify(github, times(1)).deleteTeam("org", "group-a");
        verify(github, times(1)).deleteTeam("org", "group-b");

        verifyNoMoreInteractions(github);
    }

    @Test
    void no_group_teams_deleted()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        when(github.getTeamsInfo("org")).thenReturn(Collections.emptyList());

        var organization = new Organization("org", github);
        organization.deleteGroupTeams();

        verify(github, times(1)).getTeamsInfo("org");

        verifyNoMoreInteractions(github);
    }

    @Test
    void propagates_errors_from_api()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        when(github.getTeamsInfo("org")).thenThrow(new GithubConnection.UnexpectedFormatException("boom"));

        var organization = new Organization("org", github);

        assertThrows(UnexpectedFormatException.class, organization::deleteGroupTeams);
    }
}
