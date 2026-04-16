package resumematcher;

import java.util.Set;
import java.util.TreeSet;

public class MatchService {

    public MatchResult compareTexts(String resumeText, String jobText) {
        Set<String> resumeKeywords = KeywordExtractor.extractKeywords(resumeText);
        Set<String> jobKeywords = KeywordExtractor.extractKeywords(jobText);

        // Case-insensitive match: convert both to lowercase for comparison
        Set<String> resumeLower = new TreeSet<>();
        for (String k : resumeKeywords)
            resumeLower.add(k.toLowerCase());

        Set<String> jobLower = new TreeSet<>();
        for (String k : jobKeywords)
            jobLower.add(k.toLowerCase());

        // Matched = job keywords found in resume
        Set<String> matched = new TreeSet<>(jobLower);
        matched.retainAll(resumeLower);

        // Missing = job keywords NOT found in resume
        Set<String> missing = new TreeSet<>(jobLower);
        missing.removeAll(resumeLower);

        double matchPercentage = 0.0;
        if (!jobLower.isEmpty()) {
            matchPercentage = (matched.size() * 100.0) / jobLower.size();
        }

        return new MatchResult(matchPercentage, matched, missing);
    }
}
