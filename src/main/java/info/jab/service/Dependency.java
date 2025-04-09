package info.jab.service;

import java.util.List;

public record Dependency(String groupId, String artifactId, String packaging, List<String> versions) {} 