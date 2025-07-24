package es.uniovi.raul.teams.main;

import java.util.*;

import es.uniovi.raul.teams.csv.StudentsCollector;
import es.uniovi.raul.teams.model.*;

class TeamsCollector implements StudentsCollector {

    private Map<String, Team> teams = new HashMap<>();
    private boolean hasErrors = false;
    private TeamNamingStrategy teamNamingStrategy;

    TeamsCollector(TeamNamingStrategy teamNamingStrategy) {
        if (teamNamingStrategy == null)
            throw new IllegalArgumentException("TeamNamingStrategy cannot be null.");
        this.teamNamingStrategy = teamNamingStrategy;
    }

    public List<Team> getTeams() {
        return new ArrayList<>(teams.values());
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    @Override
    public void collectStudentData(long line, String studentId, String githubUsername) {
        try {
            var student = new Student(studentId, githubUsername);

            var teamName = teamNamingStrategy.getTeamName(studentId);
            Team team = teams.computeIfAbsent(teamName, Team::new);

            team.addStudent(student);

        } catch (IllegalArgumentException e) {
            printError("Line %d. Reading student %s: %s", line, studentId, e.getMessage());
        }

    }

    private void printError(String message, Object... args) {
        System.err.printf(message, args);
        System.err.println();
        hasErrors = true;
    }

}

interface TeamNamingStrategy {
    String getTeamName(String groupId);
}
