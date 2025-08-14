package es.uniovi.raul.teams.roster;

/**
 * Implements a naming strategy where the roster ID is formatted as "name (group)".
 */
// There is no need for a proper Strategy pattern

public final class RosterNaming {

    private static final String OPEN = " ("; // Note: Leading space is intentional to match the format "name (group)"
    private static final String CLOSE = ")";

    /**
     * Generates a roster ID based on the student's name and group.
     * Examples:
     * "John Doe", "A" -> "John Doe (A)"
     * "Izquierdo Castanedo, Raúl", "i02" -> "Izquierdo Castanedo, Raúl (i02)"
     *
     * @param name  The student's name
     * @param group The student's group
     * @return A string representing the roster ID in the format "name (group)"
     */
    public static String generateRosterId(String name, String group) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name must not be null or empty");
        if (group == null || group.isBlank())
            throw new IllegalArgumentException("Group must not be null or empty");

        return name + OPEN + group + CLOSE;
    }

    public static String extractStudentName(String rosterId) {
        checkRosterIdStructure(rosterId);

        int openIdx = rosterId.lastIndexOf(OPEN); // No trim() needed as OPEN has a leading space
        return rosterId.substring(0, openIdx);
    }

    public static String extractGroup(String rosterId) {
        checkRosterIdStructure(rosterId);

        int openIdx = rosterId.lastIndexOf(OPEN);
        int closeIdx = rosterId.lastIndexOf(CLOSE);
        return rosterId.substring(openIdx + OPEN.length(), closeIdx);
    }

    private static void checkRosterIdStructure(String rosterId) {
        int openIdx = rosterId.lastIndexOf(OPEN);
        int closeIdx = rosterId.lastIndexOf(CLOSE);

        // Error si no hay paréntesis o están mal colocados
        if (openIdx == -1 || closeIdx == -1 || openIdx >= closeIdx)
            throwException(rosterId);
        // Si no hay nada antes o dentro de los paréntesis, no es un formato válido
        if (openIdx == 0 || openIdx + OPEN.length() == closeIdx)
            throwException(rosterId);
        // Si hay algo después del cierre
        if (closeIdx + CLOSE.length() != rosterId.length())
            throwException(rosterId);
    }

    private static void throwException(String rosterId) {
        String expectedFormat = "'student name (group)'";
        throw new IllegalArgumentException(
                "Invalid roster ID '" + rosterId + "'. Expected format: " + expectedFormat);
    }
}
