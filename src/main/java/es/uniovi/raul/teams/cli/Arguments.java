package es.uniovi.raul.teams.cli;

import picocli.CommandLine.*;

// CHECKSTYLE:OFF

@Command(name = "teams", version = "2.2.0", showDefaultValues = true, mixinStandardHelpOptions = true, usageHelpAutoWidth = true, description = Messages.DESCRIPTION, footer = Messages.CREDITS)
public class Arguments {

    @ArgGroup(exclusive = true) // multiplicity = "1" means exactly one required
    public Exclusive exclusive = new Exclusive();

    public static class Exclusive {
        @Parameters(index = "0", defaultValue = "classroom_roster.csv", description = "The roster CSV file downloaded from GitHub Classroom. Cannot be used together with '--clean'.")
        public String rosterFile;

        @Option(names = "--clean", description = "Remove all teams derived from groups and its members from the organization (other teams in the organization will be preserved). Cannot be used together with <rosterFile>.")
        public boolean cleanTeams;
    }

    @Option(names = "-t", description = "GitHub API access token. If not provided, it will try to read from the GITHUB_TOKEN environment variable or from a '.env' file.")
    public String token;

    @Option(names = "-o", description = "GitHub organization name. If not provided, it will try to read from the GITHUB_ORG environment variable or from a '.env' file.")
    public String organization;

}

class Messages {
    static final String DESCRIPTION = """

            This program updates the GitHub organization to ensure that its teams and their members match the provided list of students.
            For more information, visit: https://github.com/raul-izquierdo/teams
            """;

    static final String CREDITS = """

            Escuela de Ingeniería Informática, Universidad de Oviedo.
            Raúl Izquierdo Castanedo (raul@uniovi.es)
            """;

}
