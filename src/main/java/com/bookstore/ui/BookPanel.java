package com.bookstore.ui;

import com.bookstore.dao.BookDao;
import com.bookstore.model.Book;
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
import java.math.BigDecimal;
import java.util.List;

public class BookPanel extends JPanel {
    private final BookDao bookDao = new BookDao();
    private final JTextField keywordField = new JTextField();
    private final JTextField idField = new JTextField();
    private final JTextField isbnField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField authorField = new JTextField();
    private final JTextField publisherField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField stockField = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "ISBN", "书名", "作者", "出版社", "零售价", "库存"}, 0);
    private final JTable table = new JTable(tableModel);

    public BookPanel() {
        setLayout(new BorderLayout(10, 10));
        idField.setEditable(false);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        JButton searchButton = new JButton("查询");
        JButton clearButton = new JButton("清空表单");
        searchButton.addActionListener(event -> refresh());
        clearButton.addActionListener(event -> clearForm());
        JPanel searchButtons = new JPanel(new GridLayout(1, 2, 8, 8));
        searchButtons.add(searchButton);
        searchButtons.add(clearButton);
        searchPanel.add(new JLabel("关键词（编号/ISBN/书名/作者/出版社）"), BorderLayout.WEST);
        searchPanel.add(keywordField, BorderLayout.CENTER);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        JPanel formPanel = new JPanel(new GridLayout(4, 4, 8, 8));
        formPanel.add(new JLabel("ID"));
        formPanel.add(idField);
        formPanel.add(new JLabel("ISBN"));
        formPanel.add(isbnField);
        formPanel.add(new JLabel("书名"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("作者"));
        formPanel.add(authorField);
        formPanel.add(new JLabel("出版社"));
        formPanel.add(publisherField);
        formPanel.add(new JLabel("零售价"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("库存"));
        formPanel.add(stockField);

        JButton addButton = new JButton("新增图书");
        JButton updateButton = new JButton("修改图书");
        addButton.addActionListener(event -> addBook());
        updateButton.addActionListener(event -> updateBook());
        formPanel.add(addButton);
        formPanel.add(updateButton);

        UiStyle.tuneTable(table);
        table.getSelectionModel().addListSelectionListener(event -> fillSelectedRow());

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        try {
            tableModel.setRowCount(0);
            List<Book> books = bookDao.search(keywordField.getText().trim());
            for (Book book : books) {
                tableModel.addRow(new Object[]{
                        book.getId(), book.getIsbn(), book.getTitle(), book.getAuthor(),
                        book.getPublisher(), book.getRetailPrice(), book.getStockQty()
                });
            }
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void addBook() {
        try {
            bookDao.create(
                    isbnField.getText().trim(),
                    titleField.getText().trim(),
                    authorField.getText().trim(),
                    publisherField.getText().trim(),
                    new BigDecimal(priceField.getText().trim()),
                    Integer.parseInt(stockField.getText().trim())
            );
            UiUtil.info(this, "新增成功");
            clearForm();
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void updateBook() {
        try {
            if (idField.getText().isBlank()) {
                UiUtil.warn(this, "请先选择要修改的图书");
                return;
            }
            bookDao.update(
                    Long.parseLong(idField.getText()),
                    isbnField.getText().trim(),
                    titleField.getText().trim(),
                    authorField.getText().trim(),
                    publisherField.getText().trim(),
                    new BigDecimal(priceField.getText().trim()),
                    Integer.parseInt(stockField.getText().trim())
            );
            UiUtil.info(this, "修改成功");
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void fillSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        isbnField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        titleField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        authorField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        publisherField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        priceField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        stockField.setText(String.valueOf(tableModel.getValueAt(row, 6)));
    }

    private void clearForm() {
        idField.setText("");
        isbnField.setText("");
        titleField.setText("");
        authorField.setText("");
        publisherField.setText("");
        priceField.setText("");
        stockField.setText("");
        table.clearSelection();
    }
}
