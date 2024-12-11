import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class ReadScreen extends JFrame {

	private static final long serialVersionUID = 1L;
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

    public ReadScreen() {
        setTitle("ReadScreen");
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
    class ButtonEditor extends DefaultCellEditor {
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
                    fetchMetalDetails(slabNumber);
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
    }

    private void fetchMetalDetails(String slabNumber) {
        String query = "SELECT ElementName, ElementQuantity FROM metaldetails WHERE SlabNumber = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/metals", "root", "kiit");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, slabNumber);
            ResultSet rs = stmt.executeQuery();

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

            // Display metal details in a new JFrame
            JFrame detailsFrame = new JFrame("Metal Details for Slab Number: " + slabNumber);
            DefaultTableModel detailsTableModel = new DefaultTableModel(data, columnNames);
            JTable detailsTable = new JTable(detailsTableModel);
            detailsFrame.add(new JScrollPane(detailsTable));
            detailsFrame.setSize(400, 300);
            detailsFrame.setVisible(true);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching metal details", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ReadScreen readScreen = new ReadScreen();
            readScreen.setVisible(true);
        });
    }
}
