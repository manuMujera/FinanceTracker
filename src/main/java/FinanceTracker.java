import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FinanceTracker extends JFrame {

    private static final Color DARK_BG = new Color(7, 9, 25);
    private static final Color CARD_BG = new Color(12, 8, 25);
    private static final Color ACCENT_GREEN = new Color(52, 199, 89);
    private static final Color ACCENT_RED = new Color(255, 59, 48);
    private static final Color ACCENT_BLUE = new Color(0, 122, 255);
    private static final Color ACCENT_PURPLE = new Color(29, 14, 51);
    private static final Color ACCENT_GOLD = new Color(255, 204, 0);
    private static final Color TEXT_PRIMARY = new Color(120, 120, 120);
    private static final Color TEXT_SECONDARY = new Color(142, 142, 147);
    private static final Color BORDER_COLOR = new Color(44, 44, 46);


    private JLabel balanceLabel;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JTextField amountField;
    private JTextField descriptionField;
    private JTextArea transactionArea;
    private JComboBox<String> categoryCombo;
    private JButton addIncomeBtn;
    private JButton addExpenseBtn;
    private JButton clearBtn;

    private double balance = 0.0;
    private double totalIncome = 0.0;
    private double totalExpenses = 0.0;
    
    private List<Transaction> transactions = new ArrayList<>();
    private DecimalFormat df = new DecimalFormat("#,##0.00");

  
    public FinanceTracker() {
    // Test database connection first
    if (!DatabaseConnection.testConnection()) {
        JOptionPane.showMessageDialog(null, 
            "Failed to connect to database! Please check your MySQL connection.", 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    initializeGUI();
    setupEventHandlers();
    loadDataFromDatabase(); // Replace addSampleData()
    startAnimations();
}
    private void addTransaction(boolean isIncome) {
    try {
        String amountText = amountField.getText().trim();
        String description = descriptionField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();

        if (amountText.isEmpty() || amountText.equals("0.00")) {
            showError("Please enter a valid amount!", "Amount Required");
            amountField.requestFocus();
            return;
        }

        if (description.isEmpty() || description.equals("Enter description...")) {
            showError("Please provide a description!", "Description Required");
            descriptionField.requestFocus();
            return;
        }

        double amount = Double.parseDouble(amountText);

        if (amount <= 0) {
            showError("Amount must be greater than 0!", "Invalid Amount");
            return;
        }

        // Insert into database
        boolean success = DatabaseConnection.insertTransaction(amount, description, category, isIncome);
        
        if (success) {
            
            loadDataFromDatabase();
            clearForm();

            String message = isIncome ?
                    String.format("Added income of KSh %s", df.format(amount)) :
                    String.format("Recorded expense of KSh %s", df.format(amount));

            showTemporaryMessage(message);
        } else {
            showError("Failed to save transaction to database!", "Database Error");
        }

    } catch (NumberFormatException ex) {
        showError("Please enter a valid number! 🔢", "Invalid Format");
        amountField.requestFocus();
    }
}


private void updateTransactionDisplay() {
    StringBuilder sb = new StringBuilder();

    // Show last 10 transactions
    int displayCount = Math.min(10, transactions.size());
    
    for (int i = 0; i < displayCount; i++) {
        Transaction t = transactions.get(i);
        String type = t.isIncome() ? "INCOME" : "EXPENSE";

        sb.append(String.format("%-12s | %-15s | KSh %s\n",
                type, t.getCategory(), df.format(t.getAmount())));
        sb.append(String.format("Description: %s\n", t.getDescription()));
        sb.append(String.format("Date: %s\n", t.getDateCreated()));
        sb.append("───────────────────────────────────────────────\n");
    }

    if (transactions.isEmpty()) {
        sb.append("           No transactions yet! 💸\n");
        sb.append("         Start by adding some income or expenses.\n");
    }

    transactionArea.setText(sb.toString());
    transactionArea.setCaretPosition(0);
}

    private void initializeGUI() {
        setTitle("Personal Finance Tracker Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(1000, 700));

        getContentPane().setBackground(DARK_BG);
        setLayout(new BorderLayout(0, 0));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        // Set enhanced look and feel
        setupLookAndFeel();

    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(DARK_BG);
        headerPanel.setBorder(new EmptyBorder(60, 40, 40, 40));

        JLabel titleLabel = new JLabel("Finance Tracker Pro", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 36));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));

        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 25));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(new EmptyBorder(0, 40, 0, 40));

        mainPanel.add(createStatsPanel(), BorderLayout.NORTH);


        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 25, 0));
        contentPanel.setBackground(DARK_BG);

        contentPanel.add(createInputPanel());
        contentPanel.add(createTransactionPanel());

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(DARK_BG);
        statsPanel.setBorder(new EmptyBorder(0, 0, 25, 0));

        // Enhanced Balance Card with trend indicator
        JPanel balanceCard = createStatsCard("Current Balance", "KSh 0.00", ACCENT_PURPLE, "Available funds");
        balanceLabel = getCardValueLabel(balanceCard);

        // Enhanced Income Card
        JPanel incomeCard = createStatsCard("Total Income", "KSh 0.00", ACCENT_GREEN, "Money earned");
        incomeLabel = getCardValueLabel(incomeCard);

        // Enhanced Expense Card
        JPanel expenseCard = createStatsCard("Total Expenses", "KSh 0.00", ACCENT_RED, "Money spent");
        expenseLabel = getCardValueLabel(expenseCard);


        statsPanel.add(balanceCard);
        statsPanel.add(incomeCard);
        statsPanel.add(expenseCard);

        return statsPanel;
    }

    private JPanel createStatsCard(String title, String value, Color accentColor, String subtitle) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 18, 18);

                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 15, 20, 15));
        card.setPreferredSize(new Dimension(200, 120));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("SF Pro Display", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);

        JLabel subtitleLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SF Pro Display", Font.PLAIN, 11));
        subtitleLabel.setForeground(TEXT_SECONDARY);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(valueLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(subtitleLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.setColor(ACCENT_PURPLE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 23, 23);

                g2.dispose();
            }
        };

        inputPanel.setLayout(new BorderLayout());
        inputPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(48, 20, 10, 20));

        JLabel titleLabel = new JLabel("Add New Transaction");
        titleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);

        titlePanel.add(titleLabel);


        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 0, 12, 0);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Amount (KSh):", TEXT_SECONDARY), gbc);

        gbc.gridy = 1;
        amountField = createTextField("0.00", "Enter amount...");
        formPanel.add(amountField, gbc);


        gbc.gridy = 2;
        formPanel.add(createLabel("Description:", TEXT_SECONDARY), gbc);

        gbc.gridy = 3;
        descriptionField = createTextField("Enter description...", "What was this for?");
        formPanel.add(descriptionField, gbc);

        gbc.gridy = 4;
        formPanel.add(createLabel("Category:", TEXT_SECONDARY), gbc);

        gbc.gridy = 5;
        String[] categories = {"🍕 Food & Dining", "🚗 Transportation", "🏠 Housing & Rent",
                "💊 Healthcare", "🎬 Entertainment",
                "👕 Shopping", "💼 Business", "🎯 Other"};
        categoryCombo = createComboBox(categories);
        formPanel.add(categoryCombo, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(25, 0, 0, 0);
        JPanel buttonPanel = createButtonPanel();
        formPanel.add(buttonPanel, gbc);

        inputPanel.add(titlePanel, BorderLayout.NORTH);
        inputPanel.add(formPanel, BorderLayout.CENTER);

        return inputPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        buttonPanel.setOpaque(false);

        addIncomeBtn = createButton(" Add Income", ACCENT_GREEN, "Record money earned");
        addExpenseBtn = createButton(" Add Expense", ACCENT_RED, "Record money spent");
        clearBtn = createButton("🗑️Clear Form", TEXT_SECONDARY, "Reset all fields");

        buttonPanel.add(addIncomeBtn);
        buttonPanel.add(addExpenseBtn);
        buttonPanel.add(clearBtn);

        return buttonPanel;
    }

    private JPanel createTransactionPanel() {
        JPanel transactionPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.setColor(ACCENT_PURPLE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 23, 23);

                g2.dispose();
            }
        };

        transactionPanel.setLayout(new BorderLayout());
        transactionPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(40, 20, 10, 20));

        JLabel titleLabel = new JLabel("Transaction History");
        titleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titlePanel.add(titleLabel);


        transactionArea = new JTextArea();
        transactionArea.setFont(new Font("SF Mono", Font.PLAIN, 13));
        transactionArea.setBackground(DARK_BG);
        transactionArea.setForeground(TEXT_PRIMARY);
        transactionArea.setEditable(false);
        transactionArea.setBorder(new EmptyBorder(15, 20, 15, 20));
        transactionArea.setLineWrap(true);
        transactionArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(transactionArea);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ACCENT_PURPLE;
                this.trackColor = CARD_BG;
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 15, 20, 15));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        transactionPanel.add(titlePanel, BorderLayout.NORTH);
        transactionPanel.add(contentPanel, BorderLayout.CENTER);

        return transactionPanel;
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            UIManager.put("OptionPane.background", CARD_BG);
            UIManager.put("Panel.background", CARD_BG);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JLabel getCardValueLabel(JPanel card) {
        JPanel contentPanel = (JPanel) card.getComponent(0);
        return (JLabel) contentPanel.getComponent(2);
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(DARK_BG);
        footerPanel.setBorder(new EmptyBorder(15, 40, 25, 40));

        JLabel leftLabel = new JLabel("© 2025 Finance Tracker Pro");
        leftLabel.setFont(new Font("SF Pro Display", Font.PLAIN, 12));
        leftLabel.setForeground(TEXT_SECONDARY);

        JLabel rightLabel = new JLabel("GROUP 1");
        rightLabel.setFont(new Font("SF Pro Display", Font.PLAIN, 12));
        rightLabel.setForeground(TEXT_SECONDARY);

        footerPanel.add(leftLabel, BorderLayout.WEST);
        footerPanel.add(rightLabel, BorderLayout.EAST);

        return footerPanel;
    }

    private JLabel createLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SF Pro Display", Font.BOLD, 15));
        label.setForeground(color);
        return label;
    }

    private JTextField createTextField(String text, String placeholder) {
        JTextField field = new JTextField(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                super.paintComponent(g);
                g2.dispose();
            }
        };

        field.setFont(new Font("SF Pro Display", Font.PLAIN, 15));
        field.setBackground(DARK_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_BLUE);
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 2, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        field.setOpaque(false);

        // Enhanced focus effects
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(text)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
                field.setBorder(new CompoundBorder(
                        new LineBorder(ACCENT_GREEN, 2, true),
                        new EmptyBorder(12, 16, 12, 16)
                ));
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setText(text);
                    field.setForeground(TEXT_SECONDARY);
                }
                field.setBorder(new CompoundBorder(
                        new LineBorder(BORDER_COLOR, 2, true),
                        new EmptyBorder(12, 16, 12, 16)
                ));
            }
        });

        return field;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<String>(items) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                super.paintComponent(g);
                g2.dispose();
            }
        };

        combo.setFont(new Font("SF Pro Display", Font.PLAIN, 15));
        combo.setBackground(DARK_BG);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(new CompoundBorder(
                new LineBorder(ACCENT_PURPLE, 2, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        combo.setOpaque(false);

        return combo;
    }

    private JButton createButton(String text, Color color, String tooltip) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(color);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                super.paintComponent(g);
                g2.dispose();
            }
        };

        button.setFont(new Font("SF Pro Display", Font.BOLD, 13));
        button.setForeground(color.equals(TEXT_SECONDARY) ? DARK_BG : Color.WHITE);
        button.setBorder(new EmptyBorder(14, 20, 14, 20));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        button.setOpaque(false);

        return button;
    }

    private void setupEventHandlers() {
        addIncomeBtn.addActionListener(e -> {
            addTransaction(true);
        });

        addExpenseBtn.addActionListener(e -> {
            addTransaction(false);
            showAnimation(addExpenseBtn, "Expense Recorded!");
        });

        clearBtn.addActionListener(e -> clearForm());

        amountField.addActionListener(e -> addTransaction(true));
        descriptionField.addActionListener(e -> addTransaction(true));

        amountField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                validateAmountInput();
                updateButtonStates();
            }
        });

        descriptionField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateButtonStates();
            }
        });
    }




    private void updateDisplay() {
        SwingUtilities.invokeLater(() -> {

            balanceLabel.setText("KSh " + df.format(balance));
            if (balance > 0) {
                balanceLabel.setForeground(ACCENT_GREEN);
            } else if (balance < 0) {
                balanceLabel.setForeground(ACCENT_RED);
            } else {
                balanceLabel.setForeground(ACCENT_BLUE);
            }

            incomeLabel.setText("KSh " + df.format(totalIncome));
            expenseLabel.setText("KSh " + df.format(totalExpenses));


            updateTransactionDisplay();
        });
    }

    private void updateStatsDisplay() {
        SwingUtilities.invokeLater(() -> {
            balanceLabel.setText("KSh " + df.format(balance));
            incomeLabel.setText("KSh " + df.format(totalIncome));
            expenseLabel.setText("KSh " + df.format(totalExpenses));

            if (balance > 0) {
                balanceLabel.setForeground(ACCENT_GREEN);
            } else if (balance < 0) {
                balanceLabel.setForeground(ACCENT_RED);
            } else {
                balanceLabel.setForeground(ACCENT_BLUE);
            }
        });
    }





    private void clearForm() {
        amountField.setText("0.00");
        descriptionField.setText("Enter description...");
        categoryCombo.setSelectedIndex(0);
        amountField.requestFocus();
        updateButtonStates();
    }

    private void validateAmountInput() {
        String text = amountField.getText();
        if (!text.matches("\\d*\\.?\\d*")) {
            text = text.replaceAll("[^\\d.]", "");
            amountField.setText(text);
        }
    }

    private void updateButtonStates() {
        boolean hasAmount = !amountField.getText().trim().isEmpty() &&
                !amountField.getText().equals("0.00");
        boolean hasDescription = !descriptionField.getText().trim().isEmpty() &&
                !descriptionField.getText().equals("Enter description...");

        boolean canAdd = hasAmount && hasDescription;
        addIncomeBtn.setEnabled(canAdd);
        addExpenseBtn.setEnabled(canAdd);
    }


    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showTemporaryMessage(String message) {
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("SF Pro Display", Font.BOLD, 14));
        messageLabel.setForeground(ACCENT_GREEN);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setOpaque(true);
        messageLabel.setBackground(CARD_BG);
        messageLabel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JWindow popup = new JWindow();
        popup.add(messageLabel);
        popup.pack();
        popup.setLocationRelativeTo(this);
        popup.setVisible(true);

        Timer timer = new Timer(2000, e -> popup.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private void showAnimation(JButton button, String message) {
        Color originalColor = button.getForeground();
        Timer flashTimer = new Timer(100, null);
        final int[] flashCount = {0};

        flashTimer.addActionListener(e -> {
            if (flashCount[0] % 2 == 0) {
                button.setForeground(ACCENT_GOLD);
            } else {
                button.setForeground(originalColor);
            }
            flashCount[0]++;

            if (flashCount[0] >= 6) {
                flashTimer.stop();
                button.setForeground(originalColor);
            }
        });

        flashTimer.start();
        showTemporaryMessage(message);
    }

    private void addSampleData() {
        transactions.add(new Transaction(50000, "Salary", "💼 Other", true, new Date()));
        transactions.add(new Transaction(5000, "Lunch", "🍕 Food", false, new Date()));
        transactions.add(new Transaction(2000, "Matatu", "🚗 Transport", false, new Date()));

        totalIncome = 50000;
        totalExpenses = 7000;
        balance = 43000;

        updateStatsDisplay();
        updateTransactionDisplay();
    }

    private void startAnimations() {
        Timer updateTimer = new Timer(60000, e -> {
            updateDisplay();
        });
        updateTimer.start();
    }



    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new FinanceTracker().setVisible(true);
        });
    }
    
    


private void loadDataFromDatabase() {
    try {
        // Load transactions from database
        transactions = new ArrayList<>(DatabaseConnection.getAllTransactions());
        
        // Calculate totals from database
        totalIncome = DatabaseConnection.getTotalIncome();
        totalExpenses = DatabaseConnection.getTotalExpenses();
        balance = totalIncome - totalExpenses;
        
        updateStatsDisplay();
        updateTransactionDisplay();
        
    } catch (Exception e) {
        System.err.println("Error loading data from database: " + e.getMessage());
        // Fall back to empty state
        transactions = new ArrayList<>();
        totalIncome = 0.0;
        totalExpenses = 0.0;
        balance = 0.0;
        updateStatsDisplay();
        updateTransactionDisplay();
    }
}



private void refreshData() {
    loadDataFromDatabase();
}


}