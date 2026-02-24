package es.uniovi.raul.teams.roster;

/**
 * Methods to extract the student's name and group from a roster ID, which is in the format "student name (group)".
 *
 * Examples roster IDs and their components:
 * "John Doe (01)" ->  ("John Doe", "01")
 * "Izquierdo Castanedo, Raúl (i02)" -> ("Izquierdo Castanedo, Raúl", "i02")
 *
 * There is no need for a proper Strategy pattern. This strategy is not expected to change, and if it does, it will likely be a complete overhaul rather than a small modification.
 */

public final class RosterNaming {

    private static final String OPEN = " ("; // Note: Leading space is intentional to match the format "name (group)"
    private static final String CLOSE = ")";

    /**
     * Extracts the student's name from a given roster ID.
     * "John Doe (01)" -> "John Doe"
     *
     * @param rosterId The roster ID in the format "name (group)"
     * @return The student's name extracted from the roster ID
     */
    public static String extractStudentName(String rosterId) {
        checkRosterIdStructure(rosterId);

        int openIdx = rosterId.lastIndexOf(OPEN); // No trim() needed as OPEN has a leading space
        return rosterId.substring(0, openIdx);
    }

    /**
     * Extracts the student's group from a given roster ID.
     * "John Doe (01)" -> "01"
     */
    public static String extractGroup(String rosterId) {
        checkRosterIdStructure(rosterId);

        int openIdx = rosterId.lastIndexOf(OPEN);
        int closeIdx = rosterId.lastIndexOf(CLOSE);
        return rosterId.substring(openIdx + OPEN.length(), closeIdx);
    }

    //# ------------------------------------------------------------------
    //# Auxiliary methods
    //# ------------------------------------------------------------------

    // Auxiliary method to ensure that the roster ID has the correct structure "student-name (group)"
    private static void checkRosterIdStructure(String rosterId) {
        if (!isValidRosterId(rosterId))
            throw new IllegalArgumentException(
                    "Invalid roster ID '" + rosterId + "'. Expected format: 'student name (group)'");
    }

    private static boolean isValidRosterId(String rosterId) {
        int openIdx = rosterId.lastIndexOf(OPEN);
        int closeIdx = rosterId.lastIndexOf(CLOSE);

        // Error si no hay paréntesis o están mal colocados
        if (openIdx == -1 || closeIdx == -1 || openIdx >= closeIdx)
            return false;
        // Si no hay nada antes o dentro de los paréntesis, no es un formato válido
        if (openIdx == 0 || openIdx + OPEN.length() == closeIdx)
            return false;
        // Si hay algo después del cierre
        if (closeIdx + CLOSE.length() != rosterId.length())
            return false;

        return true;
    }
}
