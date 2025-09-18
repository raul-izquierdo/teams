package es.uniovi.raul.teams.organization;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uniovi.raul.teams.github.GithubConnection;
import es.uniovi.raul.teams.github.Team;
import es.uniovi.raul.teams.roster.Student;

@ExtendWith(MockitoExtension.class)
class OrganizationUpdateTeamMembersTest {

    @Mock
    private GithubConnection githubApi;

    @Mock
    private Logger logger;

    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = new Organization("test-org", githubApi, logger);
    }

    @Test
    void updateWith_emptyTeam_invitesAllStudents() throws Exception {
        final var existingTeam = new Team("group A", "group-a");
        when(githubApi.getTeams("test-org")).thenReturn(List.of(existingTeam));
        when(githubApi.getTeamMembers("test-org", "group-a")).thenReturn(new ArrayList<>());
        when(githubApi.getTeamInvitations("test-org", "group-a")).thenReturn(new ArrayList<>());

        final var students = List.of(
                new Student("Alice", "A", "Alice (A)", "alice"),
                new Student("Bob", "A", "Bob (A)", "bob"));
        organization.updateWith(students);

        verify(githubApi).inviteStudentToTeam("test-org", "group-a", "alice");
        verify(githubApi).inviteStudentToTeam("test-org", "group-a", "bob");
        verify(githubApi, never()).removeStudentFromTeam(anyString(), anyString(), anyString());
    }

    @Test
    void updateWith_allStudentsAlreadyMembers_noChanges() throws Exception {
        final var existingTeam = new Team("group A", "group-a");
        when(githubApi.getTeams("test-org")).thenReturn(List.of(existingTeam));
        when(githubApi.getTeamMembers("test-org", "group-a")).thenReturn(new ArrayList<>(List.of("alice", "bob")));
        when(githubApi.getTeamInvitations("test-org", "group-a")).thenReturn(new ArrayList<>());

        final var students = List.of(
                new Student("Alice", "A", "Alice (A)", "alice"),
                new Student("Bob", "A", "Bob (A)", "bob"));
        organization.updateWith(students);

        verify(githubApi, never()).inviteStudentToTeam(anyString(), anyString(), anyString());
        verify(githubApi, never()).removeStudentFromTeam(anyString(), anyString(), anyString());
    }

    @Test
    void updateWith_allStudentsAlreadyInvited_noChanges() throws Exception {
        final var existingTeam = new Team("group A", "group-a");
        when(githubApi.getTeams("test-org")).thenReturn(List.of(existingTeam));
        when(githubApi.getTeamMembers("test-org", "group-a")).thenReturn(new ArrayList<>());
        when(githubApi.getTeamInvitations("test-org", "group-a")).thenReturn(new ArrayList<>(List.of("alice", "bob")));

        final var students = List.of(
                new Student("Alice", "A", "Alice (A)", "alice"),
                new Student("Bob", "A", "Bob (A)", "bob"));
        organization.updateWith(students);

        verify(githubApi, never()).inviteStudentToTeam(anyString(), anyString(), anyString());
        verify(githubApi, never()).removeStudentFromTeam(anyString(), anyString(), anyString());
    }

    @Test
    void updateWith_mixedMembersAndInvitations_invitesOnlyMissing() throws Exception {
        final var existingTeam = new Team("group A", "group-a");
        when(githubApi.getTeams("test-org")).thenReturn(List.of(existingTeam));
        when(githubApi.getTeamMembers("test-org", "group-a")).thenReturn(new ArrayList<>(List.of("alice")));
        when(githubApi.getTeamInvitations("test-org", "group-a")).thenReturn(new ArrayList<>(List.of("bob")));

        final var students = List.of(
                new Student("Alice", "A", "Alice (A)", "alice"),
                new Student("Bob", "A", "Bob (A)", "bob"),
                new Student("Carol", "A", "Carol (A)", "carol"));
        organization.updateWith(students);

        verify(githubApi).inviteStudentToTeam("test-org", "group-a", "carol");
        verify(githubApi, never()).inviteStudentToTeam("test-org", "group-a", "alice");
        verify(githubApi, never()).inviteStudentToTeam("test-org", "group-a", "bob");
        verify(githubApi, never()).removeStudentFromTeam(anyString(), anyString(), anyString());
    }

    @Test
    void updateWith_extraMembers_removesUnwantedMembers() throws Exception {
        final var existingTeam = new Team("group A", "group-a");
        when(githubApi.getTeams("test-org")).thenReturn(List.of(existingTeam));
        when(githubApi.getTeamMembers("test-org", "group-a")).thenReturn(new ArrayList<>(List.of("alice", "bob")));
        when(githubApi.getTeamInvitations("test-org", "group-a")).thenReturn(new ArrayList<>());

        final var students = List.of(new Student("Alice", "A", "Alice (A)", "alice"));
        organization.updateWith(students);

        verify(githubApi, never()).inviteStudentToTeam(anyString(), anyString(), anyString());
        verify(githubApi).removeStudentFromTeam("test-org", "group-a", "bob");
        verify(githubApi, never()).removeStudentFromTeam("test-org", "group-a", "alice");
    }

    @Test
    void updateWith_extraInvitations_removesUnwantedInvitations() throws Exception {
        final var existingTeam = new Team("group A", "group-a");
        when(githubApi.getTeams("test-org")).thenReturn(List.of(existingTeam));
        when(githubApi.getTeamMembers("test-org", "group-a")).thenReturn(new ArrayList<>());
        when(githubApi.getTeamInvitations("test-org", "group-a")).thenReturn(new ArrayList<>(List.of("alice", "bob")));

        final var students = List.of(new Student("Alice", "A", "Alice (A)", "alice"));
        organization.updateWith(students);

        verify(githubApi, never()).inviteStudentToTeam(anyString(), anyString(), anyString());
        verify(githubApi).removeStudentFromTeam("test-org", "group-a", "bob");
        verify(githubApi, never()).removeStudentFromTeam("test-org", "group-a", "alice");
    }

    @Test
    void updateWith_complexScenario_addsAndRemovesCorrectly() throws Exception {
        final var existingTeam = new Team("group A", "group-a");
        when(githubApi.getTeams("test-org")).thenReturn(List.of(existingTeam));
        when(githubApi.getTeamMembers("test-org", "group-a")).thenReturn(new ArrayList<>(List.of("alice", "bob")));
        when(githubApi.getTeamInvitations("test-org", "group-a")).thenReturn(new ArrayList<>(List.of("david")));

        final var students = List.of(
                new Student("Alice", "A", "Alice (A)", "alice"),
                new Student("Carol", "A", "Carol (A)", "carol"));
        organization.updateWith(students);

        verify(githubApi).inviteStudentToTeam("test-org", "group-a", "carol");
        verify(githubApi).removeStudentFromTeam("test-org", "group-a", "bob");
        verify(githubApi).removeStudentFromTeam("test-org", "group-a", "david");
        verify(githubApi, never()).inviteStudentToTeam("test-org", "group-a", "alice");
        verify(githubApi, never()).removeStudentFromTeam("test-org", "group-a", "alice");
    }

    @Test
    void updateWith_multipleGroups_handlesEachTeamCorrectly() throws Exception {
        final var teamA = new Team("group A", "group-a");
        final var teamB = new Team("group B", "group-b");
        when(githubApi.getTeams("test-org")).thenReturn(List.of(teamA, teamB));
        when(githubApi.getTeamMembers("test-org", "group-a")).thenReturn(new ArrayList<>(List.of("alice")));
        when(githubApi.getTeamInvitations("test-org", "group-a")).thenReturn(new ArrayList<>());
        when(githubApi.getTeamMembers("test-org", "group-b")).thenReturn(new ArrayList<>());
        when(githubApi.getTeamInvitations("test-org", "group-b")).thenReturn(new ArrayList<>());

        final var students = List.of(
                new Student("Alice", "A", "Alice (A)", "alice"),
                new Student("Bob", "B", "Bob (B)", "bob"),
                new Student("Carol", "A", "Carol (A)", "carol"));
        organization.updateWith(students);

        verify(githubApi).inviteStudentToTeam("test-org", "group-a", "carol");
        verify(githubApi).inviteStudentToTeam("test-org", "group-b", "bob");
        verify(githubApi, never()).inviteStudentToTeam("test-org", "group-a", "alice");
    }

    @Test
    void updateWith_noInvitations_someMembers_partialUpdate() throws Exception {
        final var existingTeam = new Team("group A", "group-a");
        when(githubApi.getTeams("test-org")).thenReturn(List.of(existingTeam));
        when(githubApi.getTeamMembers("test-org", "group-a")).thenReturn(new ArrayList<>(List.of("alice", "david")));
        when(githubApi.getTeamInvitations("test-org", "group-a")).thenReturn(new ArrayList<>());

        final var students = List.of(
                new Student("Alice", "A", "Alice (A)", "alice"),
                new Student("Bob", "A", "Bob (A)", "bob"),
                new Student("Carol", "A", "Carol (A)", "carol"));
        organization.updateWith(students);

        verify(githubApi).inviteStudentToTeam("test-org", "group-a", "bob");
        verify(githubApi).inviteStudentToTeam("test-org", "group-a", "carol");
        verify(githubApi).removeStudentFromTeam("test-org", "group-a", "david");
        verify(githubApi, never()).inviteStudentToTeam("test-org", "group-a", "alice");
        verify(githubApi, never()).removeStudentFromTeam("test-org", "group-a", "alice");
    }

    @Test
    void updateWith_someInvitations_noMembers_partialUpdate() throws Exception {
        final var existingTeam = new Team("group A", "group-a");
        when(githubApi.getTeams("test-org")).thenReturn(List.of(existingTeam));
        when(githubApi.getTeamMembers("test-org", "group-a")).thenReturn(new ArrayList<>());
        when(githubApi.getTeamInvitations("test-org", "group-a"))
                .thenReturn(new ArrayList<>(List.of("alice", "david")));

        final var students = List.of(
                new Student("Alice", "A", "Alice (A)", "alice"),
                new Student("Bob", "A", "Bob (A)", "bob"),
                new Student("Carol", "A", "Carol (A)", "carol"));
        organization.updateWith(students);

        verify(githubApi).inviteStudentToTeam("test-org", "group-a", "bob");
        verify(githubApi).inviteStudentToTeam("test-org", "group-a", "carol");
        verify(githubApi).removeStudentFromTeam("test-org", "group-a", "david");
        verify(githubApi, never()).inviteStudentToTeam("test-org", "group-a", "alice");
        verify(githubApi, never()).removeStudentFromTeam("test-org", "group-a", "alice");
    }
}
