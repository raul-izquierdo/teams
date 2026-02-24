package es.uniovi.raul.teams.organization;

/**
 * Strategy for naming teams based on their group.
 *
 * An organizaciont can have many teams, and only some of them are groups that correspond to groups of students. To identify which teams are groups of students we need a naming strategy, that is just appending a "group " prefix to the group id.
 *
 * Examples of groups and their corresponding teams:
 * - "01" <-> "group 01"
 * - "i02" <-> "group i02"
 *
 * There is no need for a proper Strategy pattern. This strategy is not expected to change, and if it does, it will likely be a complete overhaul rather than a small modification.
 */

public final class TeamNaming {

    private static final String PREFIX = "group ";

    /**
     * Converts a group name to its corresponding team name by adding the prefix "group " to the group name.
     * "01" -> "group 01"
     */
    public static String toTeam(String group) {
        return PREFIX + group;
    }

    /**
     * Extracts the group name from a given team name by removing the prefix "group " from the team name.
     * "group 01" -> "01"
     */
    public static String toGroup(String teamName) {
        if (!isGroupTeam(teamName))
            throw new IllegalArgumentException(
                    "Team name does not correspond to a group team: it should start with '" + PREFIX + "'");

        return teamName.substring(PREFIX.length());
    }

    /**
     * Determines whether a given team name corresponds to a group team, which is defined as a team whose name starts with the prefix "group ".
     */
    public static boolean isGroupTeam(String team) {
        if (team == null)
            throw new IllegalArgumentException("Team name cannot be null");
        return team.startsWith(PREFIX);
    }

}
