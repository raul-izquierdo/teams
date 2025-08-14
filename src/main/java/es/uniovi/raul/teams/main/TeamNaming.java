package es.uniovi.raul.teams.main;

/**
 * Strategy for naming teams based on their group.
 */

// There is no need for a proper Strategy Pattern
public final class TeamNaming {

    private static final String PREFIX = "group ";

    public static String toTeam(String group) {
        return PREFIX + group;
    }

    public static boolean isGroupTeam(String team) {
        if (team == null)
            throw new IllegalArgumentException("Team name cannot be null");
        return team.startsWith(PREFIX);
    }

}
