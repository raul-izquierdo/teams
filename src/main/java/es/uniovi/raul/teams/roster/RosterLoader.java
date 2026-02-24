package es.uniovi.raul.teams.roster;

import static java.lang.String.*;

import java.io.*;
import java.util.*;

import org.apache.commons.csv.*;

/**
 * The Roster is the list of students in a GH Classroom that can be downloaded in CSV format from the classroom.
 *
 * The format of the CSV file is expected to be exactly this one (no more, no less columns, and with these exact headers):
 * "identifier","github_username","github_id","name"
 *
 * The fileds are as follows:
 * - The "identifier" column contains the roster ID, which is in the format "student name (group)". For example: "John Doe (01)" or "Izquierdo Castanedo, Raúl (i02)".
 * - The "github_username" column contains the student's GitHub username, which is used to link the student to their GitHub account and repositories.
 * - The "github_id" and "name" columns are ignored by this loader.
 */
public final class RosterLoader {

    /**
     * Loads the roster from a CSV file path.
     *
     * This method is a shortcut to {@link #load(Reader)}
     */
    public static List<Student> load(String rosterFile)
            throws IOException, InvalidRosterFormatException {

        try (var reader = new java.io.FileReader(rosterFile)) {

            return load(reader);

        } catch (InvalidRosterFormatException e) {
            throw new InvalidRosterFormatException(
                    format("'%s' is not a valid roster file. %s.", rosterFile, e.getMessage()));
        }
    }

    /**
     * Loads the roster from a CSV file reader.
     *
     * Only students with a non-blank GitHub username are included in the roster. Records without a GitHub username are skipped.
     */
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

                try {

                    var studentName = RosterNaming.extractStudentName(rosterId);
                    var group = RosterNaming.extractGroup(rosterId);

                    roster.add(new Student(studentName, group, rosterId, githubUsername.get()));

                } catch (IllegalArgumentException e) {
                    throw new InvalidRosterFormatException(
                            format("Record #%d: '%s' -> %s", csvRecord.getRecordNumber(), join(", ", csvRecord),
                                    e.getMessage()));
                }

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

        if (!csvRecord.isSet(column))
            return Optional.empty();

        String value = csvRecord.get(column);
        return (value == null || value.isBlank()) ? Optional.empty() : Optional.of(value);

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
