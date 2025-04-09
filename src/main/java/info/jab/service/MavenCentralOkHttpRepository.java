package info.jab.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MavenCentralOkHttpRepository {
    private static final Logger logger = LoggerFactory.getLogger(MavenCentralOkHttpRepository.class);
    private static final String MAVEN_SEARCH_HOST = "search.maven.org";
    private static final String SOLRSEARCH_PATH = "/solrsearch";
    
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public MavenCentralOkHttpRepository() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    public List<Dependency> search(String searchTerm) {
        try {
            HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(MAVEN_SEARCH_HOST)
                .addPathSegment("solrsearch")
                .addPathSegment("select")
                .addQueryParameter("q", searchTerm)
                .addQueryParameter("rows", "100")
                .addQueryParameter("wt", "json")
                .build();
                
            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
                
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error performing search: HTTP {}", response.code());
                    return List.of();
                }
                return processSearchResponse(response.body().string());
            }
        } catch (Exception e) {
            logger.error("Error performing search", e);
            return List.of();
        }
    }

    public List<String> getVersions(String groupId, String artifactId) {
        try {
            HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(MAVEN_SEARCH_HOST)
                .addPathSegment("solrsearch")
                .addPathSegment("select")
                .addQueryParameter("q", "g:" + groupId + " AND a:" + artifactId)
                .addQueryParameter("core", "gav")
                .addQueryParameter("rows", "98")
                .addQueryParameter("wt", "json")
                .build();
                
            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
                
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error searching versions: HTTP {}", response.code());
                    return List.of();
                }
                return processVersionResponse(response.body().string());
            }
        } catch (Exception e) {
            logger.error("Error searching versions", e);
            return List.of();
        }
    }
    
    public List<String> getSuggestions(String partialTerm) {
        try {
            HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(MAVEN_SEARCH_HOST)
                .addPathSegment("solrsearch")
                .addPathSegment("suggest")
                .addQueryParameter("q", partialTerm)
                .addQueryParameter("wt", "json")
                .build();
                
            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
                
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error getting suggestions: HTTP {}", response.code());
                    return List.of();
                }
                return processSuggestResponse(response.body().string());
            }
        } catch (Exception e) {
            logger.error("Error getting suggestions", e);
            return List.of();
        }
    }
    
    public List<String> browse(String groupId, String artifactId) {
        try {
            HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host(MAVEN_SEARCH_HOST)
                .addPathSegment("solrsearch")
                .addPathSegment("browse")
                .addQueryParameter("wt", "json");
                
            if (groupId != null) {
                urlBuilder.addQueryParameter("g", groupId);
            }
            
            if (artifactId != null) {
                urlBuilder.addQueryParameter("a", artifactId);
            }
            
            Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();
                
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error browsing repository: HTTP {}", response.code());
                    return List.of();
                }
                return processBrowseResponse(response.body().string());
            }
        } catch (Exception e) {
            logger.error("Error browsing repository", e);
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
            String latestVersion = doc.has("latestVersion") 
                ? doc.path("latestVersion").asText() 
                : doc.path("v").asText();
            
            Dependency dep = new Dependency(
                doc.path("g").asText(),
                doc.path("a").asText(),
                doc.path("p").asText(),
                List.of(latestVersion)
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
    
    private List<String> processSuggestResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode suggestions = root.path("suggestions");
        
        List<String> results = new ArrayList<>();
        if (suggestions.isArray()) {
            for (JsonNode suggestion : suggestions) {
                results.add(suggestion.asText());
            }
        }
        return results;
    }
    
    private List<String> processBrowseResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        // Process according to actual response structure
        // This is a placeholder implementation
        return new ArrayList<>();
    }
} 