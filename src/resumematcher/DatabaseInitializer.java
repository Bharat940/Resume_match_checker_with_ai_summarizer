package resumematcher;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        String createResumesTable = """
                CREATE TABLE IF NOT EXISTS resumes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL
                );
                """;

        String createJobsTable = """
                CREATE TABLE IF NOT EXISTS jobs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    company TEXT NOT NULL,
                    role TEXT NOT NULL,
                    content TEXT NOT NULL
                );
                """;

        String createMatchResultsTable = """
                CREATE TABLE IF NOT EXISTS match_results (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    resume_id INTEGER NOT NULL,
                    job_id INTEGER NOT NULL,
                    match_score REAL NOT NULL,
                    matched_keywords TEXT,
                    missing_keywords TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (resume_id) REFERENCES resumes(id),
                    FOREIGN KEY (job_id) REFERENCES jobs(id)
                );
                """;

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute(createResumesTable);
            stmt.execute(createJobsTable);
            stmt.execute(createMatchResultsTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
