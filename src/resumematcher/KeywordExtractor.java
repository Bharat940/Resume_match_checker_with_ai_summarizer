package resumematcher;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordExtractor {

    // Special tech tokens: C++, C#, .NET, Node.js, ASP.NET, etc.
    private static final Pattern TECH_PATTERN = Pattern.compile(
            "\\.NET|Node\\.js|ASP\\.NET|[A-Za-z][A-Za-z0-9]*\\+\\+|[A-Za-z][A-Za-z0-9]*#",
            Pattern.CASE_INSENSITIVE);

    // Hyphenated compound terms: low-latency, full-stack, real-time
    private static final Pattern HYPHEN_PATTERN = Pattern.compile(
            "[a-zA-Z]{2,}-[a-zA-Z]{2,}(?:-[a-zA-Z]{2,})?");

    // Regular words (min 2 chars)
    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z]{2,}");

    /**
     * Tech abbreviation alias map.
     * Key = abbreviation (lowercase) as it might appear in a resume
     * Value = full term(s) to ALSO add so matching works both ways
     */
    private static final Map<String, List<String>> ALIASES = new HashMap<>();
    static {
        // JS / JavaScript
        ALIASES.put("js", List.of("javascript"));
        ALIASES.put("javascript", List.of("js"));
        // TS / TypeScript
        ALIASES.put("ts", List.of("typescript"));
        ALIASES.put("typescript", List.of("ts"));
        // Node.js
        ALIASES.put("node.js", List.of("node", "nodejs"));
        ALIASES.put("node", List.of("node.js", "nodejs"));
        ALIASES.put("nodejs", List.of("node.js", "node"));
        // React / ReactJS
        ALIASES.put("react", List.of("reactjs"));
        ALIASES.put("reactjs", List.of("react"));
        // MongoDB
        ALIASES.put("mongo", List.of("mongodb"));
        ALIASES.put("mongodb", List.of("mongo"));
        // PostgreSQL
        ALIASES.put("postgres", List.of("postgresql"));
        ALIASES.put("postgresql", List.of("postgres"));
        // Kubernetes / K8s
        ALIASES.put("k8s", List.of("kubernetes"));
        ALIASES.put("kubernetes", List.of("k8s"));
        // Other common abbreviations
        ALIASES.put("ml", List.of("machine learning"));
        ALIASES.put("ai", List.of("artificial intelligence"));
        ALIASES.put("ui", List.of("frontend", "front-end"));
        ALIASES.put("ux", List.of("user experience"));
        ALIASES.put("db", List.of("database"));
        ALIASES.put("api", List.of("api")); // keep as-is
    }

    public static Set<String> extractKeywords(String text) {
        Set<String> keywords = new TreeSet<>();
        if (text == null || text.trim().isEmpty())
            return keywords;

        // --- Pass 1: Preserve tech tokens (C++, C#, .NET, Node.js) ---
        Matcher techMatcher = TECH_PATTERN.matcher(text);
        while (techMatcher.find()) {
            String token = techMatcher.group().toLowerCase();
            keywords.add(token);
            expandAliases(token, keywords);
        }
        String afterTech = TECH_PATTERN.matcher(text).replaceAll(" ");

        // --- Pass 2: Preserve hyphenated compounds (low-latency, full-stack) ---
        Matcher hyphenMatcher = HYPHEN_PATTERN.matcher(afterTech);
        while (hyphenMatcher.find()) {
            String compound = hyphenMatcher.group().toLowerCase();
            String[] parts = compound.split("-");
            boolean allStop = Arrays.stream(parts).allMatch(StopWords.WORDS::contains);
            if (!allStop) {
                keywords.add(compound);
                expandAliases(compound, keywords);
            }
        }
        String afterHyphen = HYPHEN_PATTERN.matcher(afterTech).replaceAll(" ");

        // --- Pass 3: Normal word extraction ---
        String cleaned = afterHyphen.replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase();
        Matcher wordMatcher = WORD_PATTERN.matcher(cleaned);
        while (wordMatcher.find()) {
            String word = wordMatcher.group().trim();
            if (word.length() >= 2 && !StopWords.WORDS.contains(word)) {
                String stemmed = stem(word);
                if (!StopWords.WORDS.contains(stemmed)) {
                    keywords.add(stemmed);
                    expandAliases(stemmed, keywords);
                }
            }
        }

        return keywords;
    }

    private static void expandAliases(String token, Set<String> keywords) {
        List<String> aliases = ALIASES.get(token);
        if (aliases != null)
            keywords.addAll(aliases);
    }

    private static String stem(String word) {
        // Don't stem short all-caps abbreviations (AWS, SQL, API, JVM, HTML)
        if (word.length() <= 5 && word.toUpperCase().equals(word))
            return word;
        // Don't stem tokens with digits (es6, html5, java11)
        if (word.chars().anyMatch(Character::isDigit))
            return word;
        // Don't stem words ending in important tech suffixes
        if (word.endsWith("script"))
            return word; // javascript, typescript
        if (word.endsWith("base"))
            return word; // database, codebase

        // Conservative stemming only — order matters, most specific first
        if (word.endsWith("ing") && word.length() > 6)
            return word.substring(0, word.length() - 3);
        if (word.endsWith("ed") && word.length() > 5)
            return word.substring(0, word.length() - 2);
        if (word.endsWith("er") && word.length() > 5)
            return word.substring(0, word.length() - 2);
        if (word.endsWith("ly") && word.length() > 5)
            return word.substring(0, word.length() - 2);
        // Strip trailing 's' only (NOT 'es' — avoids database→databas)
        if (word.endsWith("s") && word.length() > 4 && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}
