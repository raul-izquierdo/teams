package es.uniovi.raul.teams.roster;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RosterTest {

    private static Reader readerOf(String s) {
        return new StringReader(s);
    }

    @Test
    void loadsStudents_happyPath_singleAndMultiple() throws Exception {
        String csv = """
                identifier,github_username,github_id,name
                "John Doe (A)",johnd,1001,John Doe
                "Izquierdo Castanedo, Raúl (i02)",raulic,1002,Raúl Izquierdo
                """;

        List<Student> students = RosterLoader.load(readerOf(csv));

        assertEquals(2, students.size());

        Student s1 = students.get(0);
        assertEquals("John Doe", s1.name());
        assertEquals("A", s1.group());
        assertEquals("John Doe (A)", s1.rosterId());
        assertEquals("johnd", s1.login());

        Student s2 = students.get(1);
        assertEquals("Izquierdo Castanedo, Raúl", s2.name());
        assertEquals("i02", s2.group());
        assertEquals("Izquierdo Castanedo, Raúl (i02)", s2.rosterId());
        assertEquals("raulic", s2.login());
    }

    @Test
    void skipsRowsWithoutGithubUsername_andThrowsIfAllSkipped() {
        String csv = """
                identifier,github_username,github_id,name
                "Alice (A)",,1001,Alice
                "Bob (B)","   ",1002,Bob
                """;

        Reader r = readerOf(csv);
        Exception ex = assertThrows(RosterLoader.InvalidRosterFormatException.class, () -> RosterLoader.load(r));
        assertTrue(ex.getMessage().toLowerCase().contains("no students"));
    }

    @Test
    void ignoresRowsWithoutGithubUsername_whenSomeValidRemain() throws Exception {
        String csv = """
                identifier,github_username,github_id,name
                "Alice (A)",,1001,Alice
                "Bob (B)",bobgh,1002,Bob
                """;

        List<Student> students = RosterLoader.load(readerOf(csv));
        assertEquals(1, students.size());
        assertEquals("Bob", students.get(0).name());
        assertEquals("B", students.get(0).group());
        assertEquals("bobgh", students.get(0).login());
    }

    // Parameterized: Header validation failures
    static Stream<Arguments> invalidHeaders() {
        return Stream.of(
                Arguments.of("missing one column", """
                        identifier,github_username,github_id
                        "Alice (A)",a,1,Alice
                        """),
                Arguments.of("extra column", """
                        identifier,github_username,github_id,name,extra
                        "Alice (A)",a,1,Alice
                        """),
                Arguments.of("wrong names", """
                        foo,bar,baz,qux
                        "Alice (A)",a,1,Alice
                        """));
    }

    @ParameterizedTest(name = "header invalid: {0}")
    @MethodSource("invalidHeaders")
    void failsOnInvalidHeader(String name, String csv) {
        Reader r = readerOf(csv);
        Exception ex = assertThrows(RosterLoader.InvalidRosterFormatException.class, () -> RosterLoader.load(r));
        assertTrue(ex.getMessage().toLowerCase().contains("csv"));
    }

    // Parameterized: malformed rosterId (identifier) cases that should throw from extractors
    static Stream<Arguments> badIdentifiers() {
        return Stream.of(
                Arguments.of("no parentheses", """
                        identifier,github_username,github_id,name
                        Alice A,alice,1,Alice
                        """),
                Arguments.of("empty before paren", """
                        identifier,github_username,github_id,name
                        " (A)",alice,1,Alice
                        """),
                Arguments.of("empty inside paren", """
                        identifier,github_username,github_id,name
                        "Alice ()",alice,1,Alice
                        """),
                Arguments.of("extra after close", """
                        identifier,github_username,github_id,name
                        "Alice (A) extra",alice,1,Alice
                        """));
    }

    @ParameterizedTest(name = "bad identifier: {0}")
    @MethodSource("badIdentifiers")
    void failsOnMalformedIdentifier(String name, String csv) {
        Reader r = readerOf(csv);
        Exception ex = assertThrows(RosterLoader.InvalidRosterFormatException.class, () -> RosterLoader.load(r));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid roster id"));
    }

    @Nested
    class ContentValidation {
        @Test
        void blankMandatoryColumns_failWithHelpfulMessage() {
            // github_username is optional per-row (skipped), but others must be non-blank
            String csv = """
                    identifier,github_username,github_id,name
                    ,alice,1,Alice
                    """;
            Reader r = readerOf(csv);
            Exception ex = assertThrows(RosterLoader.InvalidRosterFormatException.class, () -> RosterLoader.load(r));
            assertTrue(ex.getMessage().contains("column 'identifier'"));
        }

    }
}
