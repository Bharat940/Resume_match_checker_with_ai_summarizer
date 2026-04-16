package resumematcher;

import java.util.Set;

public class MatchResult {
    private final double matchPercentage;
    private final Set<String> matchedKeywords;
    private final Set<String> missingKeywords;

    public MatchResult(double matchPercentage, Set<String> matchedKeywords, Set<String> missingKeywords) {
        this.matchPercentage = matchPercentage;
        this.matchedKeywords = matchedKeywords;
        this.missingKeywords = missingKeywords;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public Set<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public Set<String> getMissingKeywords() {
        return missingKeywords;
    }
}
