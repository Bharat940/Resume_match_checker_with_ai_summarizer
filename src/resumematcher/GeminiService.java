package resumematcher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiService {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    private final HttpClient httpClient;
    private String apiKey;

    public GeminiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        // Priority: .env file > system env variables
        this.apiKey = loadFromDotEnv();
        if (this.apiKey == null)
            this.apiKey = System.getenv("GEMINI_API_KEY");
        if (this.apiKey == null)
            this.apiKey = System.getenv("GOOGLE_API_KEY");
    }

    /**
     * Reads GEMINI_API_KEY or GOOGLE_API_KEY from a .env file
     * located in the current working directory.
     */
    private String loadFromDotEnv() {
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty())
                    continue;
                if (line.startsWith("GEMINI_API_KEY=")) {
                    return line.substring("GEMINI_API_KEY=".length()).trim().replaceAll("^\"|\"$", "");
                }
                if (line.startsWith("GOOGLE_API_KEY=")) {
                    return line.substring("GOOGLE_API_KEY=".length()).trim().replaceAll("^\"|\"$", "");
                }
            }
        } catch (IOException ignored) {
            // .env file not found — that's fine, fall back to system env
        }
        return null;
    }

    public void setApiKey(String key) {
        this.apiKey = key;
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Calls Gemini 2.5 Flash and returns AI-generated resume suggestions.
     *
     * @param resumeText The resume text
     * @param jobText    The job description text
     * @param matchedKw  Comma-separated matched keywords
     * @param missingKw  Comma-separated missing keywords
     * @param score      Match score percentage string
     * @return AI suggestions as a plain string
     * @throws Exception If the API call fails
     */
    public String getResumeSuggestions(String resumeText, String jobText,
            String matchedKw, String missingKw, String score) throws Exception {
        String prompt = buildPrompt(resumeText, jobText, matchedKw, missingKw, score);
        String json = buildRequestJson(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API error " + response.statusCode() + ":\n" + response.body());
        }

        return extractTextFromResponse(response.body());
    }

    private String buildPrompt(String resume, String job, String matched, String missing, String score) {
        return String.format("""
                You are an expert technical recruiter and career coach.

                A candidate has submitted their resume for a job.
                The keyword match score is: %s

                Matched Keywords: %s
                Missing Keywords: %s

                RESUME:
                %s

                JOB DESCRIPTION:
                %s

                Please provide a concise, professional analysis with:
                1. A 2-sentence fit summary for this role.
                2. Top 3 missing skills the candidate should address.
                3. Two actionable, specific resume improvement tips targeted at this job.
                4. One recommended job title variation they might also apply for.
                """, score, matched, missing, resume, job);
    }

    private String buildRequestJson(String prompt) {
        // Manually escape the prompt for safe JSON embedding
        String escaped = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + escaped + "\" }] }] }";
    }

    private String extractTextFromResponse(String body) {
        // Extracts the first "text" value from the Gemini JSON response
        Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            return matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return "Could not parse AI response. Raw: " + body;
    }
}
