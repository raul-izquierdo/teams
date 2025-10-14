package es.uniovi.raul.teams.organization;

/**
 * Simple logger that outputs messages to the console.
 */
public final class ConsoleLogger implements Logger {

    @Override
    public void log(String message) {
        System.out.println(message);
    }
}
