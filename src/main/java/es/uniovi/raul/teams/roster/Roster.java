package es.uniovi.raul.teams.roster;

import static java.lang.String.*;

import java.io.*;
import java.util.*;

import org.apache.commons.csv.*;

/**
 * The Roster is the list of students in a GH Classroom that can be downloaded in CSV format from the classroom.
 */
public class Roster {

    public static List<Student> load(String rosterFile)
            throws IOException, InvalidRosterFormatException {

        try (var reader = new java.io.FileReader(rosterFile)) {

            return load(reader);

        } catch (InvalidRosterFormatException e) {
            throw new InvalidRosterFormatException(
                    format("'%s' is not a valid roster file. %s.", rosterFile, e.getMessage()));
        }
    }

    public static List<Student> load(Reader reader)
            throws IOException, InvalidRosterFormatException {

        List<Student> roster = new ArrayList<>();

        try (CSVParser parser = new CSVParser(reader,
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {

            validateHeader(parser);

            for (CSVRecord csvRecord : parser) {

                Optional<String> githubUsername = findValue(csvRecord, "github_username");
                if (githubUsername.isEmpty())
                    continue; // Skip records without a GitHub username

                String rosterId = getValue(csvRecord, "identifier");

                var studentName = RosterNaming.extractStudentName(rosterId);
                var group = RosterNaming.extractGroup(rosterId);

                roster.add(new Student(studentName, group, rosterId, githubUsername.get()));
            }
        }

        if (roster.isEmpty())
            throw new InvalidRosterFormatException("No students found in the roster file. Please check the content.");

        return roster;
    }

    // Checks that the header is exactly this four columnos (no more, no less): "identifier","github_username","github_id","name"
    private static void validateHeader(CSVParser parser) throws InvalidRosterFormatException {
        if (parser.getHeaderMap().size() != 4)
            throw new InvalidRosterFormatException("CSV header must contain exactly 4 columns.");

        for (String header : List.of("identifier", "github_username", "github_id", "name"))
            if (!parser.getHeaderMap().containsKey(header))
                throw new InvalidRosterFormatException("CSV does not contain '" + header + "' column.");

    }

    private static String getValue(CSVRecord csvRecord, String column) throws InvalidRosterFormatException {

        var value = findValue(csvRecord, column);

        if (value.isEmpty())
            throw new InvalidRosterFormatException(format("Record #%d: '%s' -> column '%s' cannot be blank",
                    csvRecord.getRecordNumber(), join(", ", csvRecord), column));

        return value.get();
    }

    private static Optional<String> findValue(CSVRecord csvRecord, String column) {

        try {
            String value = csvRecord.get(column);

            if (value == null || value.isBlank()) // For example `,a`
                return Optional.empty();

            return Optional.of(value);

        } catch (ArrayIndexOutOfBoundsException e) { // Column does not exist
            return Optional.empty();
        }
    }

    /**
     * Exception thrown when the roster file is not in the expected format.
     */
    public static class InvalidRosterFormatException extends Exception {
        public InvalidRosterFormatException(String message) {
            super(message);
        }
    }
}
