package com.bookstore.ui;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.PurchaseDao;
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
import java.math.BigDecimal;

public class PurchasePanel extends JPanel {
    private final User currentUser;
    private final BookDao bookDao = new BookDao();
    private final PurchaseDao purchaseDao = new PurchaseDao();
    private final JTextField bookIdField = new JTextField();
    private final JTextField isbnField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField authorField = new JTextField();
    private final JTextField publisherField = new JTextField();
    private final JTextField purchasePriceField = new JTextField();
    private final JTextField quantityField = new JTextField();
    private final JTextField retailPriceField = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"进货单ID", "状态", "ISBN", "书名", "作者", "出版社", "进价", "数量", "总额", "创建时间"}, 0);
    private final JTable table = new JTable(tableModel);

    public PurchasePanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(5, 4, 8, 8));
        formPanel.add(new JLabel("已有图书ID（新书可空）"));
        formPanel.add(bookIdField);
        JButton loadBookButton = new JButton("按ID载入图书");
        loadBookButton.addActionListener(event -> loadBook());
        formPanel.add(loadBookButton);
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel("ISBN"));
        formPanel.add(isbnField);
        formPanel.add(new JLabel("书名"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("作者"));
        formPanel.add(authorField);
        formPanel.add(new JLabel("出版社"));
        formPanel.add(publisherField);
        formPanel.add(new JLabel("进货价"));
        formPanel.add(purchasePriceField);
        formPanel.add(new JLabel("数量"));
        formPanel.add(quantityField);
        formPanel.add(new JLabel("入库零售价"));
        formPanel.add(retailPriceField);

        JButton createButton = new JButton("创建进货单");
        JButton payButton = new JButton("付款");
        JButton returnButton = new JButton("退货");
        JButton stockButton = new JButton("入库");
        createButton.addActionListener(event -> createPurchase());
        payButton.addActionListener(event -> pay());
        returnButton.addActionListener(event -> returnOrder());
        stockButton.addActionListener(event -> stockIn());
        formPanel.add(createButton);
        formPanel.add(payButton);
        formPanel.add(returnButton);
        formPanel.add(stockButton);

        UiStyle.tuneTable(table);
        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        try {
            tableModel.setRowCount(0);
            for (Object[] row : purchaseDao.findAll()) {
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void loadBook() {
        try {
            Book book = bookDao.findById(Long.parseLong(bookIdField.getText().trim()));
            if (book == null) {
                UiUtil.warn(this, "未找到该图书");
                return;
            }
            isbnField.setText(book.getIsbn());
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            publisherField.setText(book.getPublisher());
            retailPriceField.setText(String.valueOf(book.getRetailPrice()));
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void createPurchase() {
        try {
            Long bookId = bookIdField.getText().isBlank() ? null : Long.parseLong(bookIdField.getText().trim());
            purchaseDao.create(
                    bookId,
                    isbnField.getText().trim(),
                    titleField.getText().trim(),
                    authorField.getText().trim(),
                    publisherField.getText().trim(),
                    new BigDecimal(purchasePriceField.getText().trim()),
                    Integer.parseInt(quantityField.getText().trim()),
                    currentUser.getId()
            );
            UiUtil.info(this, "进货单创建成功，当前状态为未付款");
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void pay() {
        Long id = selectedOrderId();
        if (id == null) {
            return;
        }
        try {
            purchaseDao.pay(id, currentUser.getId());
            UiUtil.info(this, "付款成功，已生成财务支出记录");
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void returnOrder() {
        Long id = selectedOrderId();
        if (id == null) {
            return;
        }
        try {
            purchaseDao.returnOrder(id);
            UiUtil.info(this, "退货成功");
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private void stockIn() {
        Long id = selectedOrderId();
        if (id == null) {
            return;
        }
        try {
            purchaseDao.stockIn(id, new BigDecimal(retailPriceField.getText().trim()));
            UiUtil.info(this, "入库成功，库存已更新");
            refresh();
        } catch (Exception e) {
            UiUtil.error(this, e);
        }
    }

    private Long selectedOrderId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.warn(this, "请先选择进货单");
            return null;
        }
        return Long.parseLong(String.valueOf(tableModel.getValueAt(row, 0)));
    }
}
