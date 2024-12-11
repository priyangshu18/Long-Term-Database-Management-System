import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

public class UpdateScreen extends JFrame {

    private JComboBox<String> fetchTypeComboBox;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private JTextField heatNumberField;
    private JComboBox<String> steelGradeComboBox;
    private JTextField castSequenceNoField;
    private JComboBox<String> shiftComboBox;
    private JComboBox<Integer> strandNumberComboBox;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public UpdateScreen() {
        setTitle("UpdateScreen");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // North Panel for Filters
        JPanel northPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        fetchTypeComboBox = new JComboBox<>(new String[]{"Weekly Wise Data", "Monthly Wise Data", "Yearly Wise Data", "Date Wise Data", "Heat Number", "Steel Grade", "Cast Sequence No", "Shift", "Strand Number"});
        startDateChooser = new JDateChooser();
        endDateChooser = new JDateChooser();
        heatNumberField = new JTextField();
        steelGradeComboBox = new JComboBox<>(new String[]{"A1011", "A1012", "A1013"});
        castSequenceNoField = new JTextField();
        shiftComboBox = new JComboBox<>(new String[]{"Morning", "Evening", "Night"});
        strandNumberComboBox = new JComboBox<>(new Integer[]{1, 2, 3});

        northPanel.add(new JLabel("Fetch Type"));
        northPanel.add(fetchTypeComboBox);
        northPanel.add(new JLabel("Start Date"));
        northPanel.add(startDateChooser);
        northPanel.add(new JLabel("End Date"));
        northPanel.add(endDateChooser);
        northPanel.add(new JLabel("Heat Number"));
        northPanel.add(heatNumberField);
        northPanel.add(new JLabel("Steel Grade"));
        northPanel.add(steelGradeComboBox);
        northPanel.add(new JLabel("Cast Sequence No"));
        northPanel.add(castSequenceNoField);
        northPanel.add(new JLabel("Shift"));
        northPanel.add(shiftComboBox);
        northPanel.add(new JLabel("Strand Number"));
        northPanel.add(strandNumberComboBox);

        add(northPanel, BorderLayout.NORTH);

        // Center Panel for Table
        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        // Fetch Data Button
        JButton fetchDataButton = new JButton("Fetch Data");
        add(fetchDataButton, BorderLayout.SOUTH);

        fetchDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchData();
            }
        });

        // Set the button renderer and editor for the "Slab Number" column
        if (dataTable.getColumnCount() > 0) {
            setButtonColumn(dataTable.getColumnModel().getColumn(0));
        }

    }

    private void fetchData() {
        String fetchType = (String) fetchTypeComboBox.getSelectedItem();
        String startDate = ((JTextField) startDateChooser.getDateEditor().getUiComponent()).getText();
        String endDate = ((JTextField) endDateChooser.getDateEditor().getUiComponent()).getText();
        String heatNumber = heatNumberField.getText();
        String steelGrade = (String) steelGradeComboBox.getSelectedItem();
        String castSequenceNo = castSequenceNoField.getText();
        String shift = (String) shiftComboBox.getSelectedItem();
        Integer strandNumber = (Integer) strandNumberComboBox.getSelectedItem();

        // Construct SQL Query based on inputs
        String query = constructQuery(fetchType, startDate, endDate, heatNumber, steelGrade, castSequenceNo, shift, strandNumber);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/metals", "root", "kiit");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Vector<String> columnNames = new Vector<>();

            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }

            Vector<Vector<Object>> data = new Vector<>();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }

            // Set the table data
            tableModel.setDataVector(data, columnNames);

            // Add listener for clicking on "Slab Number" column
            if (dataTable.getColumnModel().getColumnCount() > 0) {
                dataTable.getColumnModel().getColumn(0).setCellRenderer(new ButtonRenderer());
                dataTable.getColumnModel().getColumn(0).setCellEditor(new ButtonEditor(new JCheckBox(), conn));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String constructQuery(String fetchType, String startDate, String endDate, String heatNumber, String steelGrade, String castSequenceNo, String shift, Integer strandNumber) {
        StringBuilder query = new StringBuilder("SELECT * FROM metalmaster WHERE 1=1");

        if (fetchType != null) {
            switch (fetchType) {
                case "Weekly Wise Data":
                    query.append(" AND Date >= DATE_SUB(CURDATE(), INTERVAL 1 WEEK)");
                    break;
                case "Monthly Wise Data":
                    query.append(" AND Date >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)");
                    break;
                case "Yearly Wise Data":
                    query.append(" AND Date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)");
                    break;
                case "Date Wise Data":
                    if (!startDate.isEmpty() && !endDate.isEmpty()) {
                        query.append(" AND Date BETWEEN '").append(startDate).append("' AND '").append(endDate).append("'");
                    }
                    break;
                case "Heat Number":
                    if (!heatNumber.isEmpty()) {
                        query.append(" AND HeatNumber = '").append(heatNumber).append("'");
                    }
                    break;
                case "Steel Grade":
                    if (!steelGrade.isEmpty()) {
                        query.append(" AND SteelGrade = '").append(steelGrade).append("'");
                    }
                    break;
                case "Cast Sequence No":
                    if (!castSequenceNo.isEmpty()) {
                        query.append(" AND CastSequenceNo = '").append(castSequenceNo).append("'");
                    }
                    break;
                case "Shift":
                    if (!shift.isEmpty()) {
                        query.append(" AND Shift = '").append(shift).append("'");
                    }
                    break;
                case "Strand Number":
                    if (strandNumber != null) {
                        query.append(" AND StrandNumber = '").append(strandNumber).append("'");
                    }
                    break;
            }
        }

        return query.toString();
    }

    // Method to set the button renderer and editor for the "Slab Number" column
    private void setButtonColumn(TableColumn column) {
        column.setCellRenderer(new ButtonRenderer());
        column.setCellEditor(new ButtonEditor(new JCheckBox(), null));
    }

    // Button renderer class
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value != null) {
                setText(value.toString());
            } else {
                setText("");
            }
            return this;
        }
    }

    // Button editor class
    static class ButtonEditor extends DefaultCellEditor {
        private String slabNumber;
        private JButton button;
        private Connection conn;

        public ButtonEditor(JCheckBox checkBox, Connection conn) {
            super(checkBox);
            this.conn = conn;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    // Retrieve metal details for the clicked Slab Number
                    updatefront(slabNumber);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            slabNumber = (value != null) ? value.toString() : "";
            button.setText(slabNumber);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return slabNumber;
        }
        private void updatefront(String slabNumber) {
            String dbUrl = "jdbc:mysql://localhost:3306/metals";
            String user = "root";
            String pass = "kiit";

            // Declare text fields outside the try-catch block to access them later
            JTextField heatField = new JTextField();
            JTextField steelField = new JTextField();
            JTextField strandField = new JTextField();
            JTextField castField = new JTextField();
            JTextField shiftField = new JTextField();
            ArrayList<JLabel> ElementName = new ArrayList<>();
            ArrayList<JTextField> ElementQuantity = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(dbUrl, user, pass)) {
                // Fetch data from metalmaster table
                String masterQuery = "SELECT * FROM metalmaster WHERE slabNumber = ?";
                PreparedStatement masterPstmt = conn.prepareStatement(masterQuery);
                masterPstmt.setString(1, slabNumber);
                ResultSet masterRs = masterPstmt.executeQuery();

                // Fetch data from metaldetail table
                String detailQuery = "SELECT * FROM metaldetails WHERE slabNumber = ?";
                PreparedStatement detailPstmt = conn.prepareStatement(detailQuery);
                detailPstmt.setString(1, slabNumber);
                ResultSet detailRs = detailPstmt.executeQuery();

                // Create a new JFrame to display element details
                JFrame updateFrame = new JFrame("Update Details for Slab Number: " + slabNumber);
                updateFrame.setSize(400, 300);
                updateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                updateFrame.setLocationRelativeTo(null);
                updateFrame.setLayout(new GridLayout(0, 2));

                // Add attributes and their current values to the JFrame
                while (masterRs.next()) {
                    heatField.setText(masterRs.getString("HeatNumber"));
                    steelField.setText(masterRs.getString("SteelGrade"));
                    strandField.setText(masterRs.getString("StrandNumber"));
                    castField.setText(masterRs.getString("CastSequenceNo"));
                    shiftField.setText(masterRs.getString("Shift"));
                }

                // Add elementName and elementQuantity fields from metaldetail table
                while (detailRs.next()) {
                    JLabel nameLabel = new JLabel(detailRs.getString("ElementName"));
                    JTextField quantityField = new JTextField(detailRs.getString("ElementQuantity"));
                    ElementName.add(nameLabel);
                    ElementQuantity.add(quantityField);
                }

                // Add text fields to the JFrame
                updateFrame.add(new JLabel("Heat Number:"));
                updateFrame.add(heatField);
                updateFrame.add(new JLabel("Steel Grade:"));
                updateFrame.add(steelField);
                updateFrame.add(new JLabel("Strand Number:"));
                updateFrame.add(strandField);
                updateFrame.add(new JLabel("Cast Sequence Number:"));
                updateFrame.add(castField);
                updateFrame.add(new JLabel("Shift:"));
                updateFrame.add(shiftField);

                // Add elementName and elementQuantity fields to the JFrame
                for (int i = 0; i < ElementName.size(); i++) {
                    updateFrame.add(ElementName.get(i));
                    updateFrame.add(ElementQuantity.get(i));
                }

                // Add the Update Data button
                JButton updateButton = new JButton("Update Data");
                updateButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Extract values from text fields and update the data
                        updateMetalDetails(slabNumber, heatField.getText(), steelField.getText(), strandField.getText(), castField.getText(), shiftField.getText(), ElementName, ElementQuantity);
                        // Close the update frame
                        updateFrame.dispose();
                    }
                });
                updateFrame.add(updateButton);

                // Make the JFrame visible
                updateFrame.setVisible(true);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showInputDialog(this, "Error fetching element details.");
            }
        }

        private void updateMetalDetails(String SlabNumber, String HeatNumber, String SteelGrade, String StrandNumber, String CastSequenceNo, String Shift, ArrayList<JLabel> ElementName, ArrayList<JTextField> ElementQuantity) {
            String dbUrl = "jdbc:mysql://localhost:3306/metals";
            String user = "root";
            String pass = "kiit";

            try (Connection conn = DriverManager.getConnection(dbUrl, user, pass)) {
                // Get the current date and time
                java.util.Date currentDate = new java.util.Date();

                // Check if any of the attributes that make up the slabNumber are updated
                String checkQuery = "SELECT * FROM metalmaster WHERE SlabNumber = ? AND (HeatNumber != ? OR SteelGrade != ? OR StrandNumber != ? OR CastSequenceNo != ?)";
                PreparedStatement checkPstmt = conn.prepareStatement(checkQuery);
                checkPstmt.setString(1, SlabNumber);
                checkPstmt.setString(2, HeatNumber);
                checkPstmt.setString(3, SteelGrade);
                checkPstmt.setString(4, StrandNumber);
                checkPstmt.setString(5, CastSequenceNo);
                ResultSet checkRs = checkPstmt.executeQuery();

                // Generate the new slab number
                String newSlabNumber = generateNewSlabNumber(HeatNumber, SteelGrade, StrandNumber, CastSequenceNo);

                if (checkRs.next()) {
                    // Update newSlabNumber and newDate columns
                    String updateNewColumnsQuery = "UPDATE metalmaster SET NewSlabNumber = ?, NewDate = ? WHERE SlabNumber = ?";
                    PreparedStatement updateNewColumnsPstmt = conn.prepareStatement(updateNewColumnsQuery);
                    updateNewColumnsPstmt.setString(1, newSlabNumber);
                    updateNewColumnsPstmt.setTimestamp(2, new java.sql.Timestamp(currentDate.getTime()));
                    updateNewColumnsPstmt.setString(3, SlabNumber);
                    updateNewColumnsPstmt.executeUpdate();
                }

                // Update metalmaster table
                String masterUpdateQuery = "UPDATE metalmaster SET HeatNumber = ?, SteelGrade = ?, StrandNumber = ?, CastSequenceNo = ?, shift = ? WHERE SlabNumber = ?";
                PreparedStatement masterPstmt = conn.prepareStatement(masterUpdateQuery);
                masterPstmt.setString(1, HeatNumber);
                masterPstmt.setString(2, SteelGrade);
                masterPstmt.setString(3, StrandNumber);
                masterPstmt.setString(4, CastSequenceNo);
                masterPstmt.setString(5, Shift);
                masterPstmt.setString(6, slabNumber);
                masterPstmt.executeUpdate();

                // Update metaldetail table
                String detailUpdateQuery = "UPDATE metaldetails SET ElementQuantity = ? WHERE SlabNumber = ? AND ElementName = ?";
                for (int i = 0; i < ElementName.size(); i++) {
                    String elementName = ElementName.get(i).getText();
                    String elementQuantity = ElementQuantity.get(i).getText();
                    PreparedStatement detailPstmt = conn.prepareStatement(detailUpdateQuery);
                    detailPstmt.setString(1, elementQuantity); // Corrected variable
                    detailPstmt.setString(2, SlabNumber);
                    detailPstmt.setString(3, elementName); // Corrected variable
                    detailPstmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(null, "Data updated successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error updating data.");
            }
        }
        // Method to generate a new slab number based on the given attributes
        private String generateNewSlabNumber(String heatNumber, String steelGrade, String strandNumber, String castSequenceNo) {
            // Assuming the new slab number is a combination of these attributes
            return heatNumber + steelGrade + strandNumber + castSequenceNo;
        }
    }

        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                UpdateScreen updateScreen = new UpdateScreen();
                updateScreen.setVisible(true);
            });
        }
}

