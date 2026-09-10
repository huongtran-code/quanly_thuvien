package com.library.controller;

import com.library.dao.BudgetDAO;
import com.library.dao.DocumentDAO;
import com.library.dao.PurchaseOrderDAO;
import com.library.dao.StockTransactionDAO;
import com.library.dao.SupplierDAO;
import com.library.model.Budget;
import com.library.model.Document;
import com.library.model.PurchaseOrder;
import com.library.model.PurchaseOrderItem;
import com.library.model.StockTransaction;
import com.library.model.Supplier;

import java.util.List;

/**
 * Controller: Xử lý nhập/xuất kho qua mã vạch (dùng chung cho
 * ScannerServer và giao diện desktop).
 */
public class StockController {

    /** Kết quả xử lý một lần quét */
    public record ScanResult(Status status, String message, Document document, int newStock) {
        public enum Status { SUCCESS, NOT_FOUND, OUT_OF_STOCK, INVALID }
        public boolean isSuccess() { return status == Status.SUCCESS; }
    }

    private final DocumentDAO documentDAO = new DocumentDAO();
    private final StockTransactionDAO transactionDAO = new StockTransactionDAO();
    private final PurchaseOrderDAO orderDAO = new PurchaseOrderDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();

    /**
     * Xử lý một lần quét mã vạch.
     *
     * @param barcode mã vạch (ISBN)
     * @param isImport true = nhập kho, false = xuất kho
     * @param qty      số lượng (> 0)
     * @param userId   người thực hiện (nullable)
     */
    public ScanResult processScan(String barcode, boolean isImport, int qty, Integer userId) {
        if (barcode == null || barcode.isBlank()) {
            return new ScanResult(ScanResult.Status.INVALID, "Mã vạch trống!", null, 0);
        }
        if (qty <= 0 || qty > 100000) {
            return new ScanResult(ScanResult.Status.INVALID, "Số lượng không hợp lệ!", null, 0);
        }

        Document doc = documentDAO.findByIsbn(barcode.trim());
        if (doc == null) {
            return new ScanResult(ScanResult.Status.NOT_FOUND,
                    "Không tìm thấy tài liệu có ISBN: " + barcode.trim(), null, 0);
        }

        int delta = isImport ? qty : -qty;
        boolean updated = documentDAO.updateStock(doc.getId(), delta);
        if (!updated) {
            // updateStock có guard stock + delta >= 0 nên thất bại khi xuất quá tồn
            return new ScanResult(ScanResult.Status.OUT_OF_STOCK,
                    "Tồn kho không đủ! \"" + doc.getTitle() + "\" chỉ còn " + doc.getStockQuantity(),
                    doc, doc.getStockQuantity());
        }

        // Ghi lịch sử giao dịch
        StockTransaction tx = new StockTransaction(doc.getId(), delta,
                isImport ? StockTransaction.TYPE_IMPORT : StockTransaction.TYPE_EXPORT,
                "SCANNER", userId);
        transactionDAO.save(tx);

        // Đọc lại tồn kho thực tế từ DB (tránh dùng giá trị cũ)
        int newStock = documentDAO.getStockQuantity(doc.getId());
        if (newStock < 0) newStock = doc.getStockQuantity() + delta; // fallback
        doc.setStockQuantity(newStock);

        // ── Tự động tạo đơn mua khi XUẤT KHO ──
        String orderInfo = "";
        if (!isImport) {
            orderInfo = createAutoOrder(doc, qty, userId);
        }

        String action = isImport ? "Nhập" : "Xuất";
        String message = action + " " + qty + " × \"" + doc.getTitle() + "\" — tồn mới: " + newStock;
        if (!orderInfo.isEmpty()) {
            message += "\n" + orderInfo;
        }
        return new ScanResult(ScanResult.Status.SUCCESS, message, doc, newStock);
    }

    /**
     * Tự động tạo đơn mua DRAFT khi xuất kho.
     * Dùng NCC active đầu tiên và ngân sách active đầu tiên.
     *
     * @return thông báo kết quả (rỗng nếu không tạo được)
     */
    private String createAutoOrder(Document doc, int qty, Integer userId) {
        try {
            // Lấy NCC active đầu tiên
            List<Supplier> suppliers = supplierDAO.findActive();
            if (suppliers.isEmpty()) return "";

            // Lấy ngân sách active đầu tiên
            List<Budget> budgets = budgetDAO.findActive();
            if (budgets.isEmpty()) return "";

            Supplier supplier = suppliers.get(0);
            Budget budget = budgets.get(0);

            // Tạo đơn mua
            PurchaseOrder order = new PurchaseOrder();
            order.setSupplierId(supplier.getId());
            order.setBudgetId(budget.getId());
            order.setCreatedBy(userId != null ? userId : 0);
            order.setNotes("Tạo tự động khi xuất kho — ISBN: " + (doc.getIsbn() != null ? doc.getIsbn() : "N/A"));

            // Thêm item
            PurchaseOrderItem item = new PurchaseOrderItem(doc.getId(), qty, doc.getUnitPrice());
            order.getItems().add(item);
            order.calculateTotal();

            // Lưu vào DB
            boolean saved = orderDAO.save(order);
            if (saved) {
                return "📋 Đã tạo đơn mua " + order.getOrderCode() + " (Nháp)";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Lịch sử giao dịch gần nhất
     */
    public List<StockTransaction> getRecentTransactions(int limit) {
        return transactionDAO.findRecent(limit);
    }

    /**
     * Lịch sử xuất kho gần nhất (cho màn hình khách hàng)
     */
    public List<StockTransaction> getRecentExports(int limit) {
        return transactionDAO.findExportRecent(limit);
    }
}

