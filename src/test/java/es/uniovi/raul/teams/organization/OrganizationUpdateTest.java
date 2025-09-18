package es.uniovi.raul.teams.organization;

import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uniovi.raul.teams.github.GithubConnection;
import es.uniovi.raul.teams.github.GithubConnection.RejectedOperationException;
import es.uniovi.raul.teams.github.GithubConnection.UnexpectedFormatException;
import es.uniovi.raul.teams.github.Team;
import es.uniovi.raul.teams.roster.Student;

@ExtendWith(MockitoExtension.class)
class OrganizationUpdateTest {

    @Mock
    private GithubConnection github;

    @Test
    void update_creates_teams_and_syncs_members()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        // Establecer lo que tiene que simular devolver cada método de la conexión cuando se les llame
        when(github.getTeams("org"))
                .thenReturn(List.of()) // La primera invocación que devuelva una lista vacía
                .thenReturn(List.of(
                        new Team("group A", "group-a"),
                        new Team("group B", "group-b")));
        when(github.getTeamMembers("org", "group-a")).thenReturn(List.of());
        when(github.getTeamMembers("org", "group-b")).thenReturn(List.of());
        when(github.createTeam("org", "group A")).thenReturn(of("group-a"));
        when(github.createTeam("org", "group B")).thenReturn(of("group-b"));

        var organization = new Organization("org", github);
        List<Student> students = List.of(
                new Student("Alice", "A", "Alice (A)", "alice"),
                new Student("Bob", "A", "Bob (A)", "bob"),
                new Student("Carol", "B", "Carol (B)", "carol"));
        organization.updateWith(students);

        verify(github, times(2)).getTeams("org");
        verify(github, times(1)).createTeam("org", "group A");
        verify(github, times(1)).createTeam("org", "group B");
        verify(github, never()).deleteTeam(anyString(), anyString());

        verify(github, times(1)).getTeamMembers("org", "group-a");
        verify(github, times(1)).getTeamMembers("org", "group-b");
        verify(github, times(1)).inviteStudentToTeam("org", "group-a", "alice");
        verify(github, times(1)).inviteStudentToTeam("org", "group-a", "bob");
        verify(github, times(1)).inviteStudentToTeam("org", "group-b", "carol");
        verify(github, never()).removeStudentFromTeam(anyString(), anyString(), anyString());

        verifyNoMoreInteractions(github);
    }

    @Test
    void update_propagates_errors()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        when(github.getTeams("org")).thenThrow(new UnexpectedFormatException("boom"));

        var organization = new Organization("org", github);

        assertThrows(UnexpectedFormatException.class, () -> organization.updateWith(List.of()));
    }

    @Test
    void update_creates_missing_and_deletes_extra_group_teams()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        var teamA = new Team("group A", "group-a");
        var teamB = new Team("group B", "group-b");
        var teamC = new Team("group C", "group-c");
        var other = new Team("random", "random"); // not a group team, must be ignored

        // First call: existing teams (A, C, random). Second: after reconciled (A, B)
        when(github.getTeams("org"))
                .thenReturn(List.of(teamA, teamC, other))
                .thenReturn(List.of(teamA, teamB));

        // No members initially
        when(github.getTeamMembers("org", "group-a")).thenReturn(List.of());
        when(github.getTeamMembers("org", "group-b")).thenReturn(List.of());

        // Create B, keep A, delete C
        when(github.createTeam("org", "group B")).thenReturn(of("group-b"));

        var organization = new Organization("org", github);
        List<Student> students = List.of(
                new Student("Alice", "A", "Alice (A)", "alice"),
                new Student("Bob", "B", "Bob (B)", "bob"));

        organization.updateWith(students);

        // Two snapshots of teams
        verify(github, times(2)).getTeams("org");

        // Team reconciliation
        verify(github, times(1)).createTeam("org", "group B");
        verify(github, times(1)).deleteTeam("org", "group-c");
        // Not touching non-group team nor recreating existing A
        verify(github, never()).deleteTeam("org", "random");
        verify(github, never()).createTeam("org", "group A");

        // Member sync for A and B
        verify(github, times(1)).getTeamMembers("org", "group-a");
        verify(github, times(1)).getTeamMembers("org", "group-b");
        verify(github, times(1)).inviteStudentToTeam("org", "group-a", "alice");
        verify(github, times(1)).inviteStudentToTeam("org", "group-b", "bob");

        verify(github, never()).removeStudentFromTeam(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(github);
    }

    @Test
    void update_syncs_members_adds_and_removes()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        var teamA = new Team("group A", "group-a");
        var teamB = new Team("group B", "group-b");

        // Teams already correct before and after reconciliation
        when(github.getTeams("org"))
                .thenReturn(List.of(teamA, teamB))
                .thenReturn(List.of(teamA, teamB));

        // Initial members: A has bob (to remove), B has carol (kept)
        when(github.getTeamMembers("org", "group-a")).thenReturn(List.of("bob"));
        when(github.getTeamMembers("org", "group-b")).thenReturn(List.of("carol"));

        var organization = new Organization("org", github);
        // Desired: A -> alice, B -> carol
        List<Student> students = List.of(
                new Student("Alice", "A", "Alice (A)", "alice"),
                new Student("Carol", "B", "Carol (B)", "carol"));

        organization.updateWith(students);

        // Two snapshots of teams (update + member sync)
        verify(github, times(2)).getTeams("org");

        // No create/delete of teams
        verify(github, never()).createTeam(anyString(), anyString());
        verify(github, never()).deleteTeam(anyString(), anyString());

        // Member sync: add alice to A, remove bob from A; B unchanged
        verify(github, times(1)).getTeamMembers("org", "group-a");
        verify(github, times(1)).getTeamMembers("org", "group-b");
        verify(github, times(1)).inviteStudentToTeam("org", "group-a", "alice");
        verify(github, times(1)).removeStudentFromTeam("org", "group-a", "bob");

        verify(github, never()).inviteStudentToTeam("org", "group-b", "carol");
        verifyNoMoreInteractions(github);
    }
}
