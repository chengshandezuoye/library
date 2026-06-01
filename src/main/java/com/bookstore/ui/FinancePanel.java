package com.bookstore.ui;

import com.bookstore.dao.FinanceDao;
import com.bookstore.util.UiStyle;
import com.bookstore.util.UiUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;

public class FinancePanel extends JPanel {
    private final FinanceDao financeDao = new FinanceDao();
    private final JTextField startDateField = new JTextField(LocalDate.now().minusMonths(1).toString());
    private final JTextField endDateField = new JTextField(LocalDate.now().toString());
    private final JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"全部", "收入", "支出"});
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "类型", "金额", "关联类型", "关联ID", "备注", "操作员", "创建时间"}, 0);
    private final JTable table = new JTable(tableModel);

    public FinancePanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(1, 7, 8, 8));
        JButton searchButton = new JButton("查询");
        searchButton.addActionListener(event -> refresh());
        topPanel.add(new JLabel("开始日期"));
        topPanel.add(startDateField);
        topPanel.add(new JLabel("结束日期"));
        topPanel.add(endDateField);
        topPanel.add(typeComboBox);
        topPanel.add(searchButton);
        topPanel.add(new JLabel("格式：yyyy-MM-dd"));

        UiStyle.tuneTable(table);
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        try {
            tableModel.setRowCount(0);
            String type = switch (String.valueOf(typeComboBox.getSelectedItem())) {
                case "收入" -> "income";
                case "支出" -> "expense";
                default -> "";
            };
            for (Object[] row : financeDao.search(
                    LocalDate.parse(startDateField.getText().trim()),
                    LocalDate.parse(endDateField.getText().trim()),
                    type
            )) {
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }
}
