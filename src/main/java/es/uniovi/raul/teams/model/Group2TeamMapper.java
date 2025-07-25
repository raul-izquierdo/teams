package es.uniovi.raul.teams.model;

/**
 * Interface for mapping group IDs to team display names.
 * This allows different strategies for generating team names based on group IDs.
 * Strategy pattern to allow different implementations for team name generation.
 */
@FunctionalInterface
public interface Group2TeamMapper {
    String getTeamDisplayName(String groupId);
}
