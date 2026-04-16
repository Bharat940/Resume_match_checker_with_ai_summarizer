package resumematcher;

import java.sql.*;
import java.util.StringJoiner;

public class MatchDAO {

    public void saveMatch(String resumeTitle, String resumeContent,
            String company, String role, String jobContent,
            MatchResult result) throws SQLException {

        String insertResume = "INSERT INTO resumes(title, content) VALUES(?, ?)";
        String insertJob = "INSERT INTO jobs(company, role, content) VALUES(?, ?, ?)";
        String insertResult = "INSERT INTO match_results(resume_id, job_id, match_score, matched_keywords, missing_keywords) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement resumeStmt = conn.prepareStatement(insertResume, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement jobStmt = conn.prepareStatement(insertJob, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement resultStmt = conn.prepareStatement(insertResult)) {
                resumeStmt.setString(1, resumeTitle);
                resumeStmt.setString(2, resumeContent);
                resumeStmt.executeUpdate();

                int resumeId;
                try (ResultSet rs = resumeStmt.getGeneratedKeys()) {
                    rs.next();
                    resumeId = rs.getInt(1);
                }

                jobStmt.setString(1, company);
                jobStmt.setString(2, role);
                jobStmt.setString(3, jobContent);
                jobStmt.executeUpdate();

                int jobId;
                try (ResultSet rs = jobStmt.getGeneratedKeys()) {
                    rs.next();
                    jobId = rs.getInt(1);
                }

                resultStmt.setInt(1, resumeId);
                resultStmt.setInt(2, jobId);
                resultStmt.setDouble(3, result.getMatchPercentage());
                resultStmt.setString(4, joinSet(result.getMatchedKeywords()));
                resultStmt.setString(5, joinSet(result.getMissingKeywords()));
                resultStmt.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public java.util.List<String[]> getAllMatches() throws SQLException {
        java.util.List<String[]> results = new java.util.ArrayList<>();
        String query = """
                SELECT r.title, j.company, j.role, m.match_score, m.created_at,
                       r.content as res_content, j.content as job_content,
                       m.matched_keywords, m.missing_keywords
                FROM match_results m
                JOIN resumes r ON m.resume_id = r.id
                JOIN jobs j ON m.job_id = j.id
                ORDER BY m.created_at DESC
                """;

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                results.add(new String[] {
                        rs.getString("title"),
                        rs.getString("company"),
                        rs.getString("role"),
                        String.format("%.2f%%", rs.getDouble("match_score")),
                        rs.getString("created_at"),
                        rs.getString("res_content"),
                        rs.getString("job_content"),
                        rs.getString("matched_keywords"),
                        rs.getString("missing_keywords")
                });
            }
        }
        return results;
    }

    private String joinSet(Iterable<String> values) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String value : values) {
            joiner.add(value);
        }
        return joiner.toString();
    }
}
