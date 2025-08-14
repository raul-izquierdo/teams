package es.uniovi.raul.teams.organization;

/**
 * Strategy for naming teams based on their group.
 */

// There is no need for a proper Strategy Pattern
public final class TeamNaming {

    private static final String PREFIX = "group ";

    public static String toTeam(String group) {
        return PREFIX + group;
    }

    public static String toGroup(String teamName) {
        if (!isGroupTeam(teamName))
            throw new IllegalArgumentException(
                    "Team name does not correspond to a group team: it should start with '" + PREFIX + "'");

        return teamName.substring(PREFIX.length());
    }

    public static boolean isGroupTeam(String team) {
        if (team == null)
            throw new IllegalArgumentException("Team name cannot be null");
        return team.startsWith(PREFIX);
    }

}
