package com.bookstore.ui;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.SaleDao;
import com.bookstore.model.Book;
import com.bookstore.model.User;
import com.bookstore.util.UiStyle;
import com.bookstore.util.UiUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class SalePanel extends JPanel {
    private final User currentUser;
    private final BookDao bookDao = new BookDao();
    private final SaleDao saleDao = new SaleDao();
    private final JTextField keywordField = new JTextField();
    private final JTextField quantityField = new JTextField("1");
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "ISBN", "书名", "作者", "出版社", "零售价", "库存"}, 0);
    private final JTable table = new JTable(tableModel);

    public SalePanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        JButton searchButton = new JButton("查询图书");
        JButton sellButton = new JButton("销售选中图书");
        searchButton.addActionListener(event -> refresh());
        sellButton.addActionListener(event -> sell());
        topPanel.add(new JLabel("关键词"));
        topPanel.add(keywordField);
        topPanel.add(new JLabel("购买数量"));
        topPanel.add(quantityField);
        topPanel.add(searchButton);
        topPanel.add(sellButton);
        topPanel.add(new JLabel(""));
        topPanel.add(new JLabel(""));

        UiStyle.tuneTable(table);
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        try {
            tableModel.setRowCount(0);
            for (Book book : bookDao.search(keywordField.getText().trim())) {
                tableModel.addRow(new Object[]{
                        book.getId(), book.getIsbn(), book.getTitle(), book.getAuthor(),
                        book.getPublisher(), book.getRetailPrice(), book.getStockQty()
                });
            }
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void sell() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.warn(this, "请先选择要销售的图书");
            return;
        }
        try {
            long bookId = Long.parseLong(String.valueOf(tableModel.getValueAt(row, 0)));
            int quantity = Integer.parseInt(quantityField.getText().trim());
            saleDao.sell(bookId, quantity, currentUser.getId());
            UiUtil.info(this, "销售成功，库存和财务收入已更新");
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }
}
