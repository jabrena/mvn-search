package info.jab.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Command(name = "mvn-search", 
        mixinStandardHelpOptions = true,
        version = "0.1.0",
        description = "Search Maven Central for dependencies")
public class MavenSearchCLI implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MavenSearchCLI.class);
    
    private final MavenSearchService searchService;
    private final BufferedReader reader;
    
    @Parameters(
        index = "0", 
        description = "Search term", defaultValue = "")
    private String searchTerm;

    @Option(
        names = {"-f", "--format"}, 
        description = "Dependency format (maven, gradle, gradlekts, gradlegroovy, sbt)", defaultValue = "MAVEN")
    private DependencyFormat dependencyFormat;

    @Option(
        names = {"-o", "--show-versions"},
        description = "Show old versions automatically after selecting a dependency",
        defaultValue = "false")
    private boolean showVersions;

    @Option(
        names = {"--non-interactive"},
        description = "Run in non-interactive mode (for testing)",
        defaultValue = "false")
    private boolean nonInteractive;
    
    private List<Dependency> lastSearchResults;
    
    public MavenSearchCLI() {
        this(new MavenSearchService(new MavenCentralRepository()), new BufferedReader(new InputStreamReader(System.in)));
    }

    // Constructor for testing
    MavenSearchCLI(MavenSearchService searchService, BufferedReader reader) {
        this.searchService = searchService;
        this.reader = reader;
    }
    
    @Override
    public void run() {
        if (!searchTerm.trim().isEmpty()) {
            startSearch(searchTerm);
        } else {
            System.out.println("No search term provided");
        }
    }
    
    private void startSearch(String searchTerm) {
        try {
            lastSearchResults = searchService.search(searchTerm);
            if (!lastSearchResults.isEmpty()) {
                displayResults();
            } else {
                System.out.println("No results found");
            }
        } catch (Exception e) {
            logger.error("Error starting search", e);
        }
    }
    
    private void displayResults() {
        try {
            List<String> choices = searchService.formatSearchResults(lastSearchResults);
            
            for (String choice : choices) {
                System.out.println(choice);
            }
            
            // In non-interactive mode, just show the first result
            int index = 0;
            
            if (!nonInteractive) {
                System.out.print("Select dependency (1-" + choices.size() + "): ");
                String selection = reader.readLine();
                index = Integer.parseInt(selection) - 1;
            }
            
            if (index >= 0 && index < lastSearchResults.size()) {
                Dependency selected = lastSearchResults.get(index);
                String formattedDependency = searchService.formatDependency(selected, dependencyFormat);
                System.out.println(formattedDependency);
                
                if (showVersions) {
                    searchOlderVersions(selected);
                }
            }
        } catch (Exception e) {
            logger.error("Error displaying results", e);
        }
    }
    
    private void searchOlderVersions(Dependency dependency) {
        try {
            List<String> versions = searchService.getVersions(dependency.groupId(), dependency.artifactId());
            System.out.println("Available versions:");
            versions.forEach(v -> System.out.println("- " + v));
        } catch (Exception e) {
            logger.error("Error searching older versions", e);
        }
    }
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MavenSearchCLI())
            .setCaseInsensitiveEnumValuesAllowed(true)
            .execute(args);
        System.exit(exitCode);
    }
}
