package es.uniovi.raul.teams.roster;

import static java.lang.String.*;

import java.io.*;
import java.util.*;

import org.apache.commons.csv.*;

/**
 * The Roster is the list of students in a GH Classroom that can be downloaded in CSV format from the classroom.
 */
public final class Roster {

    private List<Student> students;

    public Roster(String rosterFile) throws IOException, InvalidRosterFormatException {
        students = load(rosterFile);
    }

    public Roster(Reader reader) throws IOException, InvalidRosterFormatException {
        students = load(reader);
    }

    public List<Student> getStudents() {
        return new ArrayList<>(students);
    }

    private List<Student> load(String rosterFile)
            throws IOException, InvalidRosterFormatException {

        try (var reader = new java.io.FileReader(rosterFile)) {

            return load(reader);

        } catch (InvalidRosterFormatException e) {
            throw new InvalidRosterFormatException(
                    format("'%s' is not a valid roster file. %s.", rosterFile, e.getMessage()));
        }
    }

    private List<Student> load(Reader reader)
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
    private void validateHeader(CSVParser parser) throws InvalidRosterFormatException {
        if (parser.getHeaderMap().size() != 4)
            throw new InvalidRosterFormatException("CSV header must contain exactly 4 columns.");

        for (String header : List.of("identifier", "github_username", "github_id", "name"))
            if (!parser.getHeaderMap().containsKey(header))
                throw new InvalidRosterFormatException("CSV does not contain '" + header + "' column.");

    }

    private String getValue(CSVRecord csvRecord, String column) throws InvalidRosterFormatException {

        var value = findValue(csvRecord, column);

        if (value.isEmpty())
            throw new InvalidRosterFormatException(format("Record #%d: '%s' -> column '%s' cannot be blank",
                    csvRecord.getRecordNumber(), join(", ", csvRecord), column));

        return value.get();
    }

    private Optional<String> findValue(CSVRecord csvRecord, String column) {

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
