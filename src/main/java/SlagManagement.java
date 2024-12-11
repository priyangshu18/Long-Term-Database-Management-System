import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class SlagManagement extends JFrame {
	private static final long serialVersionUID = 1L;
	private JComboBox<String> operationComboBox;
    private JButton submitButton;
    private Connection connection;

    public SlagManagement() {
        setTitle("SLAG HMI");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        ImageIcon originalImageIcon = new ImageIcon("C:\\Users\\Windows\\Downloads\\VSP.jpg");
        Image originalImage = originalImageIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(800, 500, Image.SCALE_SMOOTH);
        ImageIcon backgroundImage = new ImageIcon(scaledImage);
        JLabel backgroundLabel = new JLabel(backgroundImage) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };

        JLabel chooseLabel = new JLabel("Please choose:");
        chooseLabel.setFont(new Font("Arial", Font.BOLD, 20));
        chooseLabel.setForeground(Color.black);
        chooseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        String[] operations = {"CREATE", "READ", "UPDATE", "DELETE"};
        operationComboBox = new JComboBox<>(operations);

        JLabel textLabel = new JLabel("SLAG HMI");
        textLabel.setForeground(Color.BLACK);
        textLabel.setFont(new Font("VERDANA", Font.BOLD, 30));
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        textLabel.setVerticalAlignment(SwingConstants.TOP);
        backgroundLabel.setLayout(new BorderLayout());
        backgroundLabel.add(textLabel, BorderLayout.NORTH);

        submitButton = new JButton("GO ");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedOperation = (String) operationComboBox.getSelectedItem();
                if (selectedOperation.equals("CREATE")) {
                    openCreateFrame();
                }else if  (selectedOperation.equals("READ")) {
                   new ReadScreen().setVisible(true);;
                }else if (selectedOperation.equals("UPDATE")){
                	new UpdateScreen().setVisible(true);;
                }else if (selectedOperation.equals("DELETE")) {
                	new DeleteScreen().setVisible(true);;
                }
            }
        });
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(chooseLabel);
        mainPanel.add(operationComboBox);
        mainPanel.add(submitButton);

        add(mainPanel, BorderLayout.NORTH);
        add(backgroundLabel, BorderLayout.CENTER);

        setVisible(true);

        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            // Replace with your database connection details
            String url = "jdbc:mysql://localhost:3306/metals";
            String user = "root";
            String password = "kiit";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCreateFrame() {
        JFrame createFrame = new JFrame("Create Metal Record");
        createFrame.setSize(600, 400);
        createFrame.setLocationRelativeTo(null);
        createFrame.setLayout(new GridLayout(0, 2, 10, 10));

        JLabel heatNumberLabel = new JLabel("Heat Number:");
        JTextField heatNumberField = new JTextField();
        JLabel steelGradeLabel = new JLabel("Steel Grade:");
        String[] steelGradeOptions = {"A1011", "A1012", "A1013"};
        JComboBox<String> steelGradeComboBox = new JComboBox<>(steelGradeOptions);

        // Create other JLabels and JTextFields
        JLabel castSequenceNoLabel = new JLabel("Cast Sequence No:");
        JTextField castSequenceNoField = new JTextField();
        JLabel shiftLabel = new JLabel("Shift:");
        JComboBox<String> shiftComboBox = new JComboBox<>(new String[]{"Morning", "Afternoon", "Night"});
        JLabel strandNumberLabel = new JLabel("Strand Number:");
        String[] strandNumberOptions = {"1", "2", "3", "4"};
        JComboBox<String> strandNumberComboBox = new JComboBox<>(strandNumberOptions);

        // Add all components to the frame
        createFrame.add(heatNumberLabel);
        createFrame.add(heatNumberField);
        createFrame.add(steelGradeLabel);
        createFrame.add(steelGradeComboBox);
        createFrame.add(castSequenceNoLabel);
        createFrame.add(castSequenceNoField);
        createFrame.add(shiftLabel);
        createFrame.add(shiftComboBox);
        createFrame.add(strandNumberLabel);
        createFrame.add(strandNumberComboBox);

        steelGradeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSteelGrade = (String) steelGradeComboBox.getSelectedItem();
                try {
                    List<String> elementNames = readElementNamesFromFile("C:\\Users\\Windows\\eclipse-workspace\\Metallurgy\\Metallurgy\\src\\main\\resources\\" + selectedSteelGrade.toLowerCase() + "_elements.txt");
                    if (elementNames.isEmpty()) {
                        JOptionPane.showMessageDialog(createFrame, "No element names found for selected steel grade", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // Clear previous element-related components
                    createFrame.getContentPane().removeAll();
                    // Add back non-element-related components
                    createFrame.add(heatNumberLabel);
                    createFrame.add(heatNumberField);
                    createFrame.add(steelGradeLabel);
                    createFrame.add(steelGradeComboBox);
                    createFrame.add(castSequenceNoLabel);
                    createFrame.add(castSequenceNoField);
                    createFrame.add(shiftLabel);
                    createFrame.add(shiftComboBox);
                    createFrame.add(strandNumberLabel);
                    createFrame.add(strandNumberComboBox);

                    // Add new element-related components
                    for (String elementName : elementNames) {
                        JLabel elementLabel = new JLabel("Quantity for " + elementName + ":");
                        JTextField elementField = new JTextField();
                        createFrame.add(elementLabel);
                        createFrame.add(elementField);
                    }

                    // Add SUBMIT button
                    JButton createSubmitButton = new JButton("SUBMIT");
                    createSubmitButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            int result = JOptionPane.showConfirmDialog(createFrame, "Are you sure you want to submit?", "Confirm Submission", JOptionPane.YES_NO_OPTION);
                            if (result == JOptionPane.YES_OPTION) {
                                String heatNumber = heatNumberField.getText().trim();
                                String steelGrade = (String) steelGradeComboBox.getSelectedItem();
                                String castSequenceNo = castSequenceNoField.getText().trim();
                                String shift = (String) shiftComboBox.getSelectedItem();
                                String strandNumber = (String) strandNumberComboBox.getSelectedItem();
                                List<String> elementQuantities = new ArrayList<>();
                                for (Component component : createFrame.getContentPane().getComponents()) {
                                    if (component instanceof JTextField && component != heatNumberField && component != castSequenceNoField) {
                                        elementQuantities.add(((JTextField) component).getText().trim());
                                    }
                                }
                                boolean isSuccess = insertInDatabase(heatNumber, steelGrade, castSequenceNo, shift, strandNumber, elementNames, elementQuantities);
                                if (isSuccess) {
                                    createFrame.dispose();
                                }
                            }
                        }
                    });
                    createFrame.add(createSubmitButton); // Add the button to the frame

              
                    createFrame.revalidate();
                    createFrame.repaint();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(createFrame, "Failed to read element names: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        createFrame.setVisible(true);
    }

    private boolean insertInDatabase(String heatNumber, String steelGrade, String castSequenceNo, String shift, String strandNumber, List<String> elementNames, List<String> elementQuantities) {
        try {
            if (connection == null || connection.isClosed()) {
                connectToDatabase(); // Reconnect if connection is closed
            }

            // Validate MetalMaster fields
            if (heatNumber.isEmpty() || steelGrade.isEmpty() || castSequenceNo.isEmpty() || shift.isEmpty() || strandNumber.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All details must be entered", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Generate SlabNumber
            String slabNumber = heatNumber + steelGrade + castSequenceNo + strandNumber;
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

            // Insert into MetalMaster table
            String metalmasterSql = "INSERT INTO MetalMaster (SlabNumber, HeatNumber, SteelGrade, CastSequenceNo, Shift, StrandNumber, date) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement metalMasterPreparedStatement = connection.prepareStatement(metalmasterSql);
            metalMasterPreparedStatement.setString(1, slabNumber);
            metalMasterPreparedStatement.setString(2, heatNumber);
            metalMasterPreparedStatement.setString(3, steelGrade);
            metalMasterPreparedStatement.setString(4, castSequenceNo);
            metalMasterPreparedStatement.setString(5, shift);
            metalMasterPreparedStatement.setString(6, strandNumber);
            metalMasterPreparedStatement.setTimestamp(7, currentTimestamp);

            int metalMasterRows = metalMasterPreparedStatement.executeUpdate();
            if (metalMasterRows > 0) {
                JOptionPane.showMessageDialog(null, "Record inserted successfully");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to insert records", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Insert into MetalDetails table
            String metaldetailsSql = "INSERT INTO MetalDetails (SlabNumber, ElementName, ElementQuantity) VALUES (?, ?, ?)";
            PreparedStatement metalDetailsPreparedStatement = connection.prepareStatement(metaldetailsSql);

            // Insert element quantities into MetalDetails table
            for (int i = 0; i < elementNames.size(); i++) {
                String elementName = elementNames.get(i);
                String quantityText = elementQuantities.get(i);
                double elementQuantity = 0; // Default to zero if left blank
                if (!quantityText.isEmpty()) {
                    try {
                        elementQuantity = Double.parseDouble(quantityText);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Invalid number format for element quantity: " + quantityText, "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }

                metalDetailsPreparedStatement.setString(1, slabNumber);
                metalDetailsPreparedStatement.setString(2, elementName);
                metalDetailsPreparedStatement.setDouble(3, elementQuantity);

                metalDetailsPreparedStatement.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to insert data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

	/*
	 * private void openReadFrame() { JFrame readFrame = new
	 * JFrame("Read Metal Records"); readFrame.setSize(800, 600);
	 * readFrame.setLocationRelativeTo(null); readFrame.setLayout(new GridLayout(0,
	 * 2, 10, 10));
	 * 
	 * JLabel heatNumberLabel = new JLabel("Heat Number:"); JTextField
	 * heatNumberField = new JTextField(); JLabel steelGradeLabel = new
	 * JLabel("Steel Grade:"); JTextField steelGradeField = new JTextField(); JLabel
	 * startDateLabel = new JLabel("Start Date (yyyy-MM-dd):"); JTextField
	 * startDateField = new JTextField(); JLabel endDateLabel = new
	 * JLabel("End Date (yyyy-MM-dd):"); JTextField endDateField = new JTextField();
	 * 
	 * JButton searchButton = new JButton("SEARCH");
	 * searchButton.addActionListener(e -> handleSearchButton(readFrame,
	 * heatNumberField, steelGradeField, startDateField, endDateField));
	 * 
	 * readFrame.add(heatNumberLabel); readFrame.add(heatNumberField);
	 * readFrame.add(steelGradeLabel); readFrame.add(steelGradeField);
	 * readFrame.add(startDateLabel); readFrame.add(startDateField);
	 * readFrame.add(endDateLabel); readFrame.add(endDateField);
	 * readFrame.add(searchButton);
	 * 
	 * readFrame.setVisible(true); }
	 */
    private void handleSearchButton(JFrame readFrame, JTextField heatNumberField, JTextField steelGradeField, JTextField startDateField, JTextField endDateField) {
        String heatNumber = heatNumberField.getText().trim();
        String steelGrade = steelGradeField.getText().trim();
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();

        try {
            List<String> results = readFromDatabase(heatNumber, steelGrade, startDate, endDate);
            displayResults(readFrame, results);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(readFrame, "Failed to read data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> readFromDatabase(String heatNumber, String steelGrade, String startDate, String endDate) throws SQLException {
        List<String> results = new ArrayList<>();
        if (connection == null || connection.isClosed()) {
            connectToDatabase();
        }

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM MetalMaster mm JOIN MetalDetails md ON mm.SlabNumber = md.SlabNumber WHERE 1=1");
        if (!heatNumber.isEmpty()) {
            queryBuilder.append(" AND mm.HeatNumber = '").append(heatNumber).append("'");
        }
        if (!steelGrade.isEmpty()) {
            queryBuilder.append(" AND mm.SteelGrade = '").append(steelGrade).append("'");
        }
        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            queryBuilder.append(" AND mm.date BETWEEN '").append(startDate).append("' AND '").append(endDate).append("'");
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(queryBuilder.toString())) {
            while (resultSet.next()) {
                String result = "SlabNumber: " + resultSet.getString("SlabNumber") +
                        ", HeatNumber: " + resultSet.getString("HeatNumber") +
                        ", SteelGrade: " + resultSet.getString("SteelGrade") +
                        ", CastSequenceNo: " + resultSet.getString("CastSequenceNo") +
                        ", Shift: " + resultSet.getString("Shift") +
                        ", StrandNumber: " + resultSet.getString("StrandNumber") +
                        ", Date: " + resultSet.getTimestamp("date") +
                        ", ElementName: " + resultSet.getString("ElementName") +
                        ", ElementQuantity: " + resultSet.getDouble("ElementQuantity");
                results.add(result);
            }
        }

        return results;
    }

    private void displayResults(JFrame readFrame, List<String> results) {
        JDialog resultsDialog = new JDialog(readFrame, "Search Results", true);
        resultsDialog.setSize(800, 600);
        resultsDialog.setLocationRelativeTo(readFrame);

        JTextArea resultsTextArea = new JTextArea();
        resultsTextArea.setEditable(false);
        for (String result : results) {
            resultsTextArea.append(result + "\n");
        }

        JScrollPane scrollPane = new JScrollPane(resultsTextArea);
        resultsDialog.add(scrollPane);
        resultsDialog.setVisible(true);
    }

    private List<String> readElementNamesFromFile(String filePath) throws IOException {
        List<String> elementNames = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            // Assuming each line in the file contains an element name
            elementNames.add(line.trim());
        }
        reader.close();
        return elementNames;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SlagManagement();
            }
        });
    }
}