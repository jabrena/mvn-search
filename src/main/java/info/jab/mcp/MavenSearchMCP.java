///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus:quarkus-bom:3.21.1@pom
//DEPS io.quarkiverse.mcp:quarkus-mcp-server-stdio:1.0.0
//SOURCES MavenCentralOkHttpRepository.java

package info.jab.mcp;

import java.util.List;

import info.jab.service.Dependency;
import info.jab.service.MavenCentralOkHttpRepository;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;

public class MavenSearchMCP {
    
    private final MavenCentralOkHttpRepository mavenCentralOkHttpRepository;

    public MavenSearchMCP() {
        this.mavenCentralOkHttpRepository = new MavenCentralOkHttpRepository();
    }

    public MavenSearchMCP(MavenCentralOkHttpRepository mavenCentralOkHttpRepository) {
        this.mavenCentralOkHttpRepository = mavenCentralOkHttpRepository;
    }

    @Tool(description = "Search in Maven Central for a dependency")
    public List<Dependency> search(
        @ToolArg(description = "search term") String searchTerm) {
        return mavenCentralOkHttpRepository.search(searchTerm);
    }

    @Tool(description = "Get the versions of an Maven artifact")
    public List<String> getArtifactVersions(
        @ToolArg(description = "group id") String groupId,
        @ToolArg(description = "artifact id") String artifactId) {
        return mavenCentralOkHttpRepository.getVersions(groupId, artifactId);
    }
}
