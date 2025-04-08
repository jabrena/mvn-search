package info.jab.cli;

import java.util.List;

public record Dependency(String groupId, String artifactId, String packaging, List<String> versions) {} 