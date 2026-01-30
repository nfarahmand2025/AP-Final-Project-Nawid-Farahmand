package com.mall.ui;

import com.mall.model.SaleRecord;
import com.mall.service.MallManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SalesHistoryPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private MallManager manager;
    private JLabel totalRevenueLabel;

    public SalesHistoryPanel(MainFrame parent, MallManager manager) {
        this.manager = manager;

        // Layout with padding
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // --- 1. Top Section: Header & Revenue ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel header = new JLabel("Global Sales History");
        header.setFont(new Font("SansSerif", Font.BOLD, 22));

        totalRevenueLabel = new JLabel("Total Revenue: $0.00");
        totalRevenueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        totalRevenueLabel.setForeground(new Color(46, 125, 50));

        topPanel.add(header, BorderLayout.WEST);
        topPanel.add(totalRevenueLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Center Section: The Table ---
        String[] columns = { "Transaction ID", "Customer", "Product", "Qty", "Price Paid", "Date" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Bottom Section: Navigation & Actions ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);

        // Left side: Admin Actions
        ModernButton deleteBtn = new ModernButton("Delete Selected Record");
        deleteBtn.setForeground(new Color(255, 71, 77));
        deleteBtn.addActionListener(e -> handleDelete());

        // Right side: Back Navigation
        ModernButton backBtn = new ModernButton("← Back to Dashboard");
        backBtn.setPreferredSize(new Dimension(180, 40));
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        backBtn.addActionListener(e -> parent.showView("CATALOG_ADMIN"));

        footerPanel.add(deleteBtn, BorderLayout.WEST);
        footerPanel.add(backBtn, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);
    }

    public void refresh() {
        model.setRowCount(0);
        List<SaleRecord> sales = manager.getSaleService().getAllSales();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (SaleRecord s : sales) {
            model.addRow(new Object[] {
                    s.getTransactionId(),
                    s.getCustomerUsername(),
                    s.getProductName(),
                    s.getQuantity(),
                    "$" + String.format("%.2f", s.getAmountPaid()),
                    s.getDate().format(formatter)
            });
        }

        // Update the Revenue label at the top
        String total = String.format("%.2f", manager.getSaleService().getTotalRevenue());
        totalRevenueLabel.setText("Total Revenue: $" + total);
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.");
            return;
        }

        String tid = (String) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete all records for Transaction: " + tid + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            manager.getSaleService().deleteRecord(tid);
            manager.saveData();
            refresh();
        }
    }
}