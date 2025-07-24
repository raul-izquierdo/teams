package es.uniovi.raul.teams.cli;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Map;

public class CommandLine {
    public static Map<String, String> getCommandLineArguments(String[] args) {

        Map<String, String> map = new HashMap<>();
        int i = 0;
        while (i < args.length) {
            if (args[i].equals("-o") && i + 1 < args.length)
                map.put("-o", args[++i]);
            else if (args[i].equals("-t") && i + 1 < args.length)
                map.put("-t", args[++i]);
            else if (args[i].endsWith(".csv"))
                map.put("csv", args[i]);
            else if (args[i].equals("-h") || args[i].equals("--help"))
                map.put("-h", "");
            i++;
        }

        addEnvironmentIfAbsent(map, "-t", "GITHUB_TOKEN");
        addEnvironmentIfAbsent(map, "-o", "GITHUB_ORG");

        if (!map.containsKey("csv"))
            map.put("csv", "classroom_roster.csv");

        return map;
    }

    public static boolean validArguments(Map<String, String> args) {

        if (args.containsKey("-h"))
            return false;

        if (!args.containsKey("csv")) {
            printError("CSV file is required. Download the roster from your Github Classroom.");
            return false;
        }

        if (!args.containsKey("-o")) {
            printError("GitHub organization name is required. Use -o <organization>.");
            return false;
        }

        if (!args.containsKey("-t")) {
            printError("GitHub API token is required. Use -t <token> or set GITHUB_TOKEN environment variable.");
            return false;
        }

        return true;
    }

    public static void printError(String message) {
        System.err.println("\n\n --> Error!!! " + message + "\n");
    }

    public static void printError(String message, Object... args) {
        printError(String.format(message, args));
    }

    public static void printHelp() {
        System.out.println(
                """
                        Usage: java -jar teams.jar [<csvfile>] [-t <token>] [-o <organization>]

                        Options:
                          <csvfile>            The roster CSV file downloaded from the classroom
                                               (default = "classroom_roster.csv").
                          -t <token>           GitHub API access token. If not provided, it will try to
                                               read from the GITHUB_TOKEN environment variable or from
                                               an .env file.
                          -o <organization>    GitHub organization name. If not provided, it will try to
                                               read from the GITHUB_ORG environment variable or from
                                               an .env file.
                          -h, --help           Show this help message.

                        This program creates GitHub teams and adds students to them based on the CSV
                        file generated from the classroom roster. The students ids in the roster should
                        follow the format "<group_id>-<name>" (01-John Doe).

                        If a team or student already exists, it is ignored. Students without GitHub
                        username or id are skipped.
                        """);

        printCredits();
    }

    public static void printCredits() {
        System.out.println("""

                Escuela de Ingenieria Informatica. Universidad de Oviedo.
                Raúl Izquierdo Castanedo (raul@uniovi.es)
                """);
    }

    private static void addEnvironmentIfAbsent(Map<String, String> map, String key, String envKey) {
        if (!map.containsKey(key))
            getEnvOrDotenv(envKey).ifPresent(value -> map.put(key, value));
    }

    private static java.util.Optional<String> getEnvOrDotenv(String key) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String value = dotenv.get(key);
        if (value == null)
            value = System.getenv(key);
        return java.util.Optional.ofNullable(value);
    }
}
