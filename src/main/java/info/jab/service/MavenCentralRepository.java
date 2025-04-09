package info.jab.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MavenCentralRepository {
    private static final Logger logger = LoggerFactory.getLogger(MavenCentralRepository.class);
    private static final String MAVEN_SEARCH_URL = "https://search.maven.org/solrsearch/select";
    
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public MavenCentralRepository() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<Dependency> search(String searchTerm) {
        try {
            String encodedTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
            String url = MAVEN_SEARCH_URL + "?rows=100&q=" + encodedTerm;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
                
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return processSearchResponse(response.body());
        } catch (Exception e) {
            logger.error("Error performing search", e);
            return List.of();
        }
    }

    public List<String> getVersions(String groupId, String artifactId) {
        try {
            String encodedGroupId = URLEncoder.encode(groupId, StandardCharsets.UTF_8);
            String encodedArtifactId = URLEncoder.encode(artifactId, StandardCharsets.UTF_8);
            String url = MAVEN_SEARCH_URL + "?rows=98&q=g:" + encodedGroupId +
                "+AND+a:" + encodedArtifactId + "&core=gav";
                
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
                
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return processVersionResponse(response.body());
        } catch (Exception e) {
            logger.error("Error searching versions", e);
            return List.of();
        }
    }

    private List<Dependency> processSearchResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode docs = root.path("response").path("docs");
        
        if (!docs.isArray() || docs.size() == 0) {
            return List.of();
        }
        
        List<Dependency> results = new ArrayList<>();
        for (JsonNode doc : docs) {
            Dependency dep = new Dependency(
                doc.path("g").asText(),
                doc.path("a").asText(),
                doc.path("p").asText(),
                List.of(doc.path("latestVersion").asText())
            );
            results.add(dep);
        }
        return results;
    }

    private List<String> processVersionResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode docs = root.path("response").path("docs");
        
        List<String> versions = new ArrayList<>();
        for (JsonNode doc : docs) {
            versions.add(doc.path("v").asText());
        }
        return versions;
    }
} 