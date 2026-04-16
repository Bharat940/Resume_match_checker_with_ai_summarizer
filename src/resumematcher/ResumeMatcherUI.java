package resumematcher;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResumeMatcherUI extends JFrame {

    private static final Color BG_COLOR = new Color(245, 247, 250);
    private static final Color SECONDARY_BG = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(74, 144, 226);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color AI_COLOR = new Color(138, 43, 226);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);

    private JTextField resumeTitleField, companyField, roleField;
    private JTextArea resumeArea, jobArea, outputArea;
    private JTextArea historyResumeArea, historyJobArea, historyDetailArea;
    private JLabel scoreLabel;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton aiBtn;

    private final MatchService matchService = new MatchService();
    private final MatchDAO matchDAO = new MatchDAO();
    private final GeminiService geminiService = new GeminiService();
    private MatchResult currentResult = null;
    private List<String[]> historyData = new ArrayList<>();

    public ResumeMatcherUI() {
        setTitle("Resume Matcher Pro");
        setSize(1200, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BG_COLOR);
        initUI();
    }

    // ─────────────────────────────── UI INIT ────────────────────────────────

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.addTab("New Analysis", createAnalysisPanel());
        tabs.addTab("Match History", createHistoryPanel());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1)
                loadHistory();
        });
        setContentPane(tabs);
    }

    private JPanel createAnalysisPanel() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top: metadata fields
        JPanel top = new JPanel(new GridLayout(2, 3, 15, 5));
        top.setBackground(SECONDARY_BG);
        top.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)),
                new EmptyBorder(15, 15, 15, 15)));
        resumeTitleField = createStyledTextField();
        companyField = createStyledTextField();
        roleField = createStyledTextField();
        top.add(createLabel("Resume Title"));
        top.add(createLabel("Company Name"));
        top.add(createLabel("Job Role"));
        top.add(resumeTitleField);
        top.add(companyField);
        top.add(roleField);

        // Centre: text areas
        JPanel centre = new JPanel(new GridLayout(1, 2, 20, 0));
        centre.setOpaque(false);
        resumeArea = createStyledTextArea();
        jobArea = createStyledTextArea();
        centre.add(createScrollWithTitle(resumeArea, "Paste Resume Text"));
        centre.add(createScrollWithTitle(jobArea, "Paste Job Description"));

        // Bottom: score + output + buttons
        JPanel bottom = new JPanel(new BorderLayout(15, 15));
        bottom.setOpaque(false);

        scoreLabel = new JLabel("Match Score: 0.00%");
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        scoreLabel.setForeground(ACCENT_COLOR);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);

        outputArea = createStyledTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(230, 235, 240));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnRow.setOpaque(false);
        JButton analyzeBtn = createStyledButton("Analyze Match", SUCCESS_COLOR);
        JButton saveBtn = createStyledButton("Save Result", ACCENT_COLOR);
        aiBtn = createStyledButton("\u2728 AI Suggestions", AI_COLOR);
        JButton clearBtn = createStyledButton("Clear All", DANGER_COLOR);
        btnRow.add(analyzeBtn);
        btnRow.add(saveBtn);
        btnRow.add(aiBtn);
        btnRow.add(clearBtn);

        analyzeBtn.addActionListener(e -> analyzeMatch());
        saveBtn.addActionListener(e -> saveResult());
        aiBtn.addActionListener(e -> getAiSuggestions());
        clearBtn.addActionListener(e -> clearAll());

        bottom.add(scoreLabel, BorderLayout.NORTH);
        bottom.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        bottom.add(btnRow, BorderLayout.SOUTH);

        main.add(top, BorderLayout.NORTH);
        main.add(centre, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);
        return main;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = { "Resume Title", "Company", "Role", "Score", "Date" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                showHistoryDetails(historyTable.getSelectedRow());
        });

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setPreferredSize(new Dimension(0, 300));

        JPanel details = new JPanel(new BorderLayout(10, 10));
        details.setBackground(SECONDARY_BG);
        details.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 10, 10, 10)));

        historyResumeArea = createStyledTextArea();
        historyJobArea = createStyledTextArea();
        historyDetailArea = createStyledTextArea();
        historyResumeArea.setEditable(false);
        historyJobArea.setEditable(false);
        historyDetailArea.setEditable(false);

        JPanel twoCol = new JPanel(new GridLayout(1, 2, 10, 0));
        twoCol.setOpaque(false);
        twoCol.add(createScrollWithTitle(historyResumeArea, "Original Resume Content"));
        twoCol.add(createScrollWithTitle(historyJobArea, "Original Job Description"));
        historyDetailArea.setRows(5);

        details.add(twoCol, BorderLayout.CENTER);
        details.add(createScrollWithTitle(historyDetailArea, "Match Details (Keywords)"),
                BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, details);
        split.setDividerLocation(300);

        JButton refresh = createStyledButton("Refresh History", ACCENT_COLOR);
        refresh.addActionListener(e -> loadHistory());

        panel.add(split, BorderLayout.CENTER);
        panel.add(refresh, BorderLayout.SOUTH);
        return panel;
    }

    // ─────────────────────────────── LOGIC ──────────────────────────────────

    private void analyzeMatch() {
        String resumeText = resumeArea.getText().trim();
        String jobText = jobArea.getText().trim();
        if (resumeText.isEmpty() || jobText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both texts.");
            return;
        }
        currentResult = matchService.compareTexts(resumeText, jobText);
        double score = currentResult.getMatchPercentage();
        scoreLabel.setText(String.format("Match Score: %.2f%%", score));
        if (score >= 75)
            scoreLabel.setForeground(SUCCESS_COLOR);
        else if (score >= 40)
            scoreLabel.setForeground(WARNING_COLOR);
        else
            scoreLabel.setForeground(DANGER_COLOR);

        StringBuilder sb = new StringBuilder("--- ANALYSIS RESULTS ---\n\n");
        sb.append("\u2705 MATCHED KEYWORDS:\n")
                .append(String.join(", ", currentResult.getMatchedKeywords()))
                .append("\n\n\u274C MISSING KEYWORDS:\n")
                .append(String.join(", ", currentResult.getMissingKeywords()));
        outputArea.setText(sb.toString());
    }

    private void saveResult() {
        if (currentResult == null) {
            JOptionPane.showMessageDialog(this, "Analyze first!");
            return;
        }
        String title = resumeTitleField.getText().trim();
        String company = companyField.getText().trim();
        String role = roleField.getText().trim();
        if (title.isEmpty() || company.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in the details above.");
            return;
        }
        try {
            matchDAO.saveMatch(title, resumeArea.getText(), company, role,
                    jobArea.getText(), currentResult);
            JOptionPane.showMessageDialog(this, "Success! Result saved to database.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void getAiSuggestions() {
        if (currentResult == null) {
            JOptionPane.showMessageDialog(this, "Please run 'Analyze Match' first.");
            return;
        }
        if (!geminiService.hasApiKey()) {
            String key = JOptionPane.showInputDialog(this,
                    "Enter your Gemini API Key (from aistudio.google.com):",
                    "Gemini API Key Required", JOptionPane.PLAIN_MESSAGE);
            if (key == null || key.isBlank())
                return;
            geminiService.setApiKey(key.trim());
        }

        aiBtn.setText("\u23f3 Loading...");
        aiBtn.setEnabled(false);
        outputArea.setText(outputArea.getText() + "\n\n\u23f3 Contacting Gemini AI...");

        String resumeText = resumeArea.getText();
        String jobText = jobArea.getText();
        String matched = String.join(", ", currentResult.getMatchedKeywords());
        String missing = String.join(", ", currentResult.getMissingKeywords());
        String score = String.format("%.2f%%", currentResult.getMatchPercentage());

        new javax.swing.SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return geminiService.getResumeSuggestions(resumeText, jobText, matched, missing, score);
            }

            @Override
            protected void done() {
                aiBtn.setText("\u2728 AI Suggestions");
                aiBtn.setEnabled(true);
                outputArea.setText(outputArea.getText()
                        .replace("\n\n\u23f3 Contacting Gemini AI...", ""));
                try {
                    showAiResultDialog(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ResumeMatcherUI.this,
                            "AI Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ─────────────────────────── AI RESULT DIALOG ───────────────────────────

    private void showAiResultDialog(String markdown) {
        JEditorPane pane = new JEditorPane("text/html", markdownToHtml(markdown));
        pane.setEditable(false);
        pane.setBackground(new Color(250, 251, 252));
        pane.setBorder(new EmptyBorder(15, 15, 15, 15));
        pane.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(pane);
        scroll.setPreferredSize(new Dimension(750, 500));
        scroll.setBorder(null);

        JDialog dialog = new JDialog(this, "\u2728 Gemini AI Analysis", true);
        dialog.setLayout(new BorderLayout());

        // Purple header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AI_COLOR);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel title = new JLabel("\u2728  AI-Powered Resume Analysis  |  Gemini 2.5 Flash");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        JButton close = new JButton("Close");
        close.setFont(new Font("SansSerif", Font.BOLD, 12));
        close.setForeground(Color.WHITE);
        close.setBackground(new Color(100, 30, 180));
        close.setFocusPainted(false);
        close.setBorder(new EmptyBorder(6, 18, 6, 18));
        close.setCursor(new Cursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> dialog.dispose());
        header.add(title, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** Converts Markdown to HTML for rendering in JEditorPane. */
    private String markdownToHtml(String md) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Segoe UI,SansSerif;font-size:13pt;"
                + "color:#2c3e50;line-height:1.7;margin:10px;'>");

        boolean inUL = false, inOL = false;
        for (String raw : md.split("\n")) {
            String line = raw.trim();

            boolean isUL = line.startsWith("* ") || line.startsWith("- ") || line.startsWith("+ ");
            boolean isOL = line.matches("^\\d+\\..*");

            if (!isUL && inUL) {
                html.append("</ul>");
                inUL = false;
            }
            if (!isOL && inOL) {
                html.append("</ol>");
                inOL = false;
            }

            if (line.isEmpty()) {
                html.append("<br/>");
            } else if (line.startsWith("### ")) {
                html.append("<h3 style='color:#8a2be2;margin:10px 0 4px;'>")
                        .append(inline(line.substring(4))).append("</h3>");
            } else if (line.startsWith("## ")) {
                html.append("<h2 style='color:#4a90e2;border-bottom:2px solid #e0e0e0;"
                        + "padding-bottom:6px;margin:14px 0 6px;'>")
                        .append(inline(line.substring(3))).append("</h2>");
            } else if (line.startsWith("# ")) {
                html.append("<h1 style='color:#2c3e50;'>")
                        .append(inline(line.substring(2))).append("</h1>");
            } else if (isOL) {
                if (!inOL) {
                    html.append("<ol style='margin:6px 0 6px 24px;'>");
                    inOL = true;
                }
                html.append("<li style='margin-bottom:6px;'>")
                        .append(inline(line.replaceFirst("^\\d+\\.\\s*", ""))).append("</li>");
            } else if (isUL) {
                if (!inUL) {
                    html.append("<ul style='margin:6px 0 6px 24px;'>");
                    inUL = true;
                }
                html.append("<li style='margin-bottom:6px;'>")
                        .append(inline(line.substring(2))).append("</li>");
            } else {
                html.append("<p style='margin:4px 0;'>").append(inline(line)).append("</p>");
            }
        }
        if (inUL)
            html.append("</ul>");
        if (inOL)
            html.append("</ol>");
        html.append("</body></html>");
        return html.toString();
    }

    /** Converts inline Markdown (**bold**, *italic*, `code`) to HTML. */
    private String inline(String t) {
        t = t.replaceAll("\\*\\*\\*(.+?)\\*\\*\\*", "<b><i>$1</i></b>");
        t = t.replaceAll("\\*\\*(.+?)\\*\\*", "<b style='color:#2c3e50;'>$1</b>");
        t = t.replaceAll("\\*(.+?)\\*", "<i>$1</i>");
        t = t.replaceAll("`(.+?)`",
                "<code style='background:#eef2f7;padding:1px 5px;border-radius:3px;"
                        + "font-family:monospace;color:#c0392b;'>$1</code>");
        return t;
    }

    // ─────────────────────────────── HISTORY ────────────────────────────────

    private void showHistoryDetails(int index) {
        if (index < 0 || index >= historyData.size())
            return;
        String[] row = historyData.get(index);
        historyResumeArea.setText(row[5]);
        historyJobArea.setText(row[6]);
        historyDetailArea.setText("\u2705 MATCHED:\n" + row[7] + "\n\n\u274C MISSING:\n" + row[8]);
        historyResumeArea.setCaretPosition(0);
        historyJobArea.setCaretPosition(0);
    }

    private void loadHistory() {
        try {
            tableModel.setRowCount(0);
            historyData = matchDAO.getAllMatches();
            for (String[] row : historyData)
                tableModel.addRow(new Object[] { row[0], row[1], row[2], row[3], row[4] });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load history: " + e.getMessage());
        }
    }

    private void clearAll() {
        resumeTitleField.setText("");
        companyField.setText("");
        roleField.setText("");
        resumeArea.setText("");
        jobArea.setText("");
        outputArea.setText("");
        scoreLabel.setText("Match Score: 0.00%");
        scoreLabel.setForeground(ACCENT_COLOR);
        currentResult = null;
    }

    // ─────────────────────────────── HELPERS ────────────────────────────────

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(TEXT_COLOR);
        return l;
    }

    private JTextField createStyledTextField() {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 5, 5, 5)));
        return f;
    }

    private JTextArea createStyledTextArea() {
        JTextArea a = new JTextArea();
        a.setFont(new Font("Monospaced", Font.PLAIN, 13));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        return a;
    }

    private JScrollPane createScrollWithTitle(JTextArea area, String title) {
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(220, 220, 220)), title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12), TEXT_COLOR));
        return sp;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 25, 10, 25));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}
