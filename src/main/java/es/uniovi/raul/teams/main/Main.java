package es.uniovi.raul.teams.main;

import java.io.IOException;
import java.util.Optional;

import es.uniovi.raul.teams.cli.*;
import es.uniovi.raul.teams.github.GithubApi;
import es.uniovi.raul.teams.github.GithubApi.*;
import es.uniovi.raul.teams.github.GithubApiImpl;
import es.uniovi.raul.teams.github.GithubApiDryRunDecorator;
import es.uniovi.raul.teams.organization.Organization;
import es.uniovi.raul.teams.roster.RosterLoader;
import es.uniovi.raul.teams.roster.RosterLoader.InvalidRosterFormatException;

/**
 * Entry point for the application.
 */
public class Main {

    public static void main(String[] args) {

        Optional<Arguments> argumentsOpt = ArgumentsParser.parse(args);
        if (argumentsOpt.isEmpty())
            System.exit(1);

        try {

            run(argumentsOpt.get());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Operation was interrupted.");
            System.exit(1);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

        System.exit(0);

    }

    private static void run(Arguments arguments) throws UnexpectedFormatException,
            RejectedOperationException, IOException, InterruptedException, InvalidRosterFormatException {

        GithubApi connection = new GithubApiImpl(arguments.token);
        if (arguments.dryRun) {
            System.out.println("[DRY-RUN] No changes will be performed.");
            connection = new GithubApiDryRunDecorator(connection);
        }
        var organization = new Organization(arguments.organization, connection);

        if (arguments.exclusive.cleanTeams)
            organization.deleteGroupTeams();
        else {

            System.out.printf("%nProceeding to update the organization '%s' using the roster file '%s'...%n",
                    arguments.organization, arguments.exclusive.rosterFile);

            organization.updateWith(RosterLoader.load(arguments.exclusive.rosterFile));

            System.out.println("""

                    REMEMBER. Students have been invited to join their groups, but they are not members yet!!!
                    Each student must accept the invitation sent to their email before they appear in the groups.
                    """);
        }
    }

}
