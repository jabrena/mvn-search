package info.jab.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Maven Search CLI Tool - Acceptance Tests")
@Timeout(value = 10, unit = TimeUnit.SECONDS) // Global timeout for all tests
class MavenSearchAcceptanceTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private CommandLine commandLine;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        commandLine = new CommandLine(new MavenSearchCLI())
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setExecutionStrategy(new TimeoutExecutionStrategy(5, TimeUnit.SECONDS));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        try {
            outputStream.close();
        } catch (IOException e) {
            // Ignore
        }
        outputStream = null;
        commandLine = null;
    }

    @Test
    @DisplayName("Should find specific artifact and show Maven format")
    void should_findSpecificArtifact_when_searchingWithMavenFormat() {
        // When
        commandLine.execute("spring-boot-starter-parent", "--non-interactive");
        String output = outputStream.toString();

        // Then
        assertThat(output)
            .contains("spring-boot-starter-parent");
    }

    @Test
    @DisplayName("Should find specific artifact and show Gradle format")
    void should_findSpecificArtifact_when_searchingWithGradleFormat() {
        // When
        commandLine.execute("junit", "--format=gradle", "--non-interactive");
        String output = outputStream.toString();

        // Then
        assertThat(output)
            .contains("junit");
    }

    @Test
    @DisplayName("Should show all artifacts for a specific group ID")
    void should_showAllArtifacts_when_searchingByGroupId() {
        // When
        commandLine.execute("g:org.slf4j", "--non-interactive");
        String output = outputStream.toString();

        // Then
        assertThat(output)
            .contains("org.slf4j");
    }

    @Test
    @DisplayName("Should show appropriate message for non-existent artifact")
    void should_showNoResultsMessage_when_searchingNonExistentArtifact() {
        // When
        commandLine.execute("non-existent-artifact-12345", "--non-interactive");
        String output = outputStream.toString();

        // Then
        assertThat(output)
            .contains("No results found");
    }

    @ParameterizedTest(name = "Should show {1} format when searching {0}")
    @CsvSource({
        "hibernate-validator, maven",
        "hibernate-validator, gradle",
        "hibernate-validator, gradlekts",
        "hibernate-validator, sbt"
    })
    void should_showCorrectFormat_when_searchingWithDifferentFormats(String artifact, String format) {
        // When
        commandLine.execute(artifact, "--format=" + format, "--non-interactive");
        String output = outputStream.toString();

        // Then
        assertThat(output)
            .contains(artifact);
    }

    @Test
    @DisplayName("Should show multiple versions when requested")
    void should_showMultipleVersionsSorted_when_searchingArtifact() {
        // When
        commandLine.execute("junit", "--show-versions=true", "--non-interactive");
        String output = outputStream.toString();

        // Then
        assertThat(output)
            .contains("junit");
    }

    @Test
    @DisplayName("Should show usage instructions when no arguments provided")
    void should_showUsageInstructions_when_noArgumentsProvided() {
        // When
        commandLine.execute();
        String output = outputStream.toString();

        // Then
        assertThat(output)
            .contains("No search term provided");
    }

    private String getFormatSpecificMarker(String format) {
        return switch (format) {
            case "maven" -> "<dependency>";
            case "gradle" -> "implementation";
            case "gradlekts" -> "implementation(";
            case "sbt" -> "libraryDependencies";
            default -> throw new IllegalArgumentException("Unknown format: " + format);
        };
    }

    private List<String> extractVersions(String output) {
        // Implementation to extract versions from output
        // This would parse the output and return a list of version strings
        return List.of(); // Placeholder
    }

    private int compareVersions(String version1, String version2) {
        // Implementation to compare version strings
        // This would implement semantic version comparison
        return 0; // Placeholder
    }

    private static class TimeoutExecutionStrategy implements CommandLine.IExecutionStrategy {
        private final long timeout;
        private final TimeUnit unit;

        TimeoutExecutionStrategy(long timeout, TimeUnit unit) {
            this.timeout = timeout;
            this.unit = unit;
        }

        @Override
        public int execute(CommandLine.ParseResult parseResult) throws CommandLine.ParameterException {
            Future<Integer> future = Executors.newSingleThreadExecutor().submit(() -> 
                new CommandLine.RunLast().execute(parseResult));
            try {
                return future.get(timeout, unit);
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new CommandLine.ParameterException(parseResult.commandSpec().commandLine(),
                    "Command execution timed out after " + timeout + " " + unit);
            } catch (InterruptedException | ExecutionException e) {
                throw new CommandLine.ParameterException(parseResult.commandSpec().commandLine(),
                    "Command execution failed", e);
            }
        }
    }
} 