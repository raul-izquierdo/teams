package es.uniovi.raul.teams.organization;

public final class ConsoleLogger implements Logger {

    @Override
    public void log(String message) {
        System.out.println(message);
    }
}
