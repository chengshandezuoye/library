package com.bookstore.ui;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.PurchaseDao;
import com.bookstore.model.Book;
import com.bookstore.model.User;
import com.bookstore.util.UiStyle;
import com.bookstore.util.UiUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
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

        JButton loadBookButton = new JButton("按ID载入图书");
        loadBookButton.addActionListener(event -> loadBook());

        JPanel loadPanel = new JPanel(new BorderLayout(8, 8));
        loadPanel.setBorder(BorderFactory.createTitledBorder("载入已有图书"));
        loadPanel.add(new JLabel("已有图书ID（新书可空）"), BorderLayout.WEST);
        loadPanel.add(bookIdField, BorderLayout.CENTER);
        loadPanel.add(loadBookButton, BorderLayout.EAST);

        JPanel bookInfoPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        bookInfoPanel.setBorder(BorderFactory.createTitledBorder("图书信息"));
        bookInfoPanel.add(new JLabel("ISBN"));
        bookInfoPanel.add(isbnField);
        bookInfoPanel.add(new JLabel("书名"));
        bookInfoPanel.add(titleField);
        bookInfoPanel.add(new JLabel("作者"));
        bookInfoPanel.add(authorField);
        bookInfoPanel.add(new JLabel("出版社"));
        bookInfoPanel.add(publisherField);

        JPanel purchaseInfoPanel = new JPanel(new GridLayout(1, 6, 8, 8));
        purchaseInfoPanel.setBorder(BorderFactory.createTitledBorder("进货信息"));
        purchaseInfoPanel.add(new JLabel("进货价"));
        purchaseInfoPanel.add(purchasePriceField);
        purchaseInfoPanel.add(new JLabel("数量"));
        purchaseInfoPanel.add(quantityField);
        purchaseInfoPanel.add(new JLabel("入库零售价"));
        purchaseInfoPanel.add(retailPriceField);

        JButton createButton = new JButton("创建进货单");
        JButton payButton = new JButton("付款");
        JButton returnButton = new JButton("退货");
        JButton stockButton = new JButton("入库");
        createButton.addActionListener(event -> createPurchase());
        payButton.addActionListener(event -> pay());
        returnButton.addActionListener(event -> returnOrder());
        stockButton.addActionListener(event -> stockIn());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setBorder(BorderFactory.createTitledBorder("进货单操作"));
        actionPanel.add(createButton);
        actionPanel.add(payButton);
        actionPanel.add(returnButton);
        actionPanel.add(stockButton);

        UiStyle.tuneTable(table);
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.add(loadPanel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(bookInfoPanel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(purchaseInfoPanel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(actionPanel);

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
        String status = selectedOrderStatus();
        if (!"paid".equals(status)) {
            UiUtil.warn(this, "只有已付款进货单可以入库，当前状态：" + status);
            return;
        }
        if (retailPriceField.getText().trim().isBlank()) {
            UiUtil.warn(this, "请输入入库零售价");
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

    private String selectedOrderStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return "";
        }
        return String.valueOf(tableModel.getValueAt(row, 1));
    }
}
