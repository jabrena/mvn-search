package info.jab.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MavenSearchService {
    private static final Logger logger = LoggerFactory.getLogger(MavenSearchService.class);
    
    private final MavenCentralRepository repository;
    
    public MavenSearchService(MavenCentralRepository repository) {
        this.repository = repository;
    }
    
    public List<Dependency> search(String searchTerm) {
        return repository.search(searchTerm);
    }
    
    public List<String> getVersions(String groupId, String artifactId) {
        return repository.getVersions(groupId, artifactId);
    }
    
    public String formatDependency(Dependency dependency, DependencyFormat format) {
        return switch (format) {
            case GRADLE, GRADLEKTS -> String.format("""
                implementation("%s:%s:%s")""",
                dependency.groupId(), dependency.artifactId(), dependency.versions().get(0));
            case GRADLEGROOVY -> String.format("""
                implementation '%s:%s:%s'""",
                dependency.groupId(), dependency.artifactId(), dependency.versions().get(0));
            case SBT -> String.format("""
                libraryDependencies += "%s" %% "%s" %% "%s\"""",
                dependency.groupId(), dependency.artifactId(), dependency.versions().get(0));
            case MAVEN -> String.format("""
                <dependency>
                    <groupId>%s</groupId>
                    <artifactId>%s</artifactId>
                    <version>%s</version>
                </dependency>""",
                dependency.groupId(), dependency.artifactId(), dependency.versions().get(0));
        };
    }
    
    public List<String> formatSearchResults(List<Dependency> dependencies) {
        List<String> choices = new ArrayList<>();
        for (int i = 0; i < dependencies.size(); i++) {
            Dependency dep = dependencies.get(i);
            choices.add(String.format("%d) %s:%s:%s",
                i + 1,
                dep.groupId(),
                dep.artifactId(),
                dep.versions().get(0)));
        }
        return choices;
    }
} 