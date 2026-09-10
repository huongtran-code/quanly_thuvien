package com.library.controller;

import com.library.dao.BudgetDAO;
import com.library.dao.DocumentDAO;
import com.library.dao.PurchaseOrderDAO;
import com.library.model.Budget;
import com.library.model.PurchaseOrder;
import com.library.model.PurchaseOrderItem;

import java.util.List;

/**
 * Controller: Quản lý đơn mua hàng
 * Xử lý workflow: DRAFT → APPROVED → RECEIVED / CANCELLED
 */
public class PurchaseOrderController {

    private final PurchaseOrderDAO orderDAO = new PurchaseOrderDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();

    public List<PurchaseOrder> getAllOrders() {
        return orderDAO.findAll();
    }

    public PurchaseOrder getOrderById(int id) {
        return orderDAO.findById(id);
    }

    /**
     * Tạo đơn mua mới (trạng thái DRAFT)
     */
    public String createOrder(PurchaseOrder order) {
        if (order.getSupplierId() <= 0) {
            return "Vui lòng chọn nhà cung cấp!";
        }
        if (order.getBudgetId() <= 0) {
            return "Vui lòng chọn ngân sách!";
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return "Đơn mua phải có ít nhất 1 tài liệu!";
        }
        // Tính tổng
        order.calculateTotal();

        // Kiểm tra ngân sách đủ không
        Budget budget = budgetDAO.findById(order.getBudgetId());
        if (budget == null || !budget.isActive()) {
            return "Ngân sách không hợp lệ hoặc đã đóng!";
        }
        if (budget.getRemainingAmount() < order.getTotalAmount()) {
            return "Ngân sách không đủ! Còn lại: " + String.format("%,.0f", budget.getRemainingAmount()) + " ₫";
        }

        order.setStatus("DRAFT");
        return orderDAO.save(order) ? null : "Lỗi tạo đơn mua!";
    }

    /**
     * Duyệt đơn mua → trừ ngân sách
     */
    public String approveOrder(int orderId) {
        PurchaseOrder order = orderDAO.findById(orderId);
        if (order == null) return "Đơn mua không tồn tại!";
        if (!"DRAFT".equals(order.getStatus())) return "Chỉ duyệt được đơn ở trạng thái Nháp!";

        // Kiểm tra lại ngân sách
        Budget budget = budgetDAO.findById(order.getBudgetId());
        if (budget == null || !budget.isActive()) {
            return "Ngân sách không hợp lệ!";
        }
        if (budget.getRemainingAmount() < order.getTotalAmount()) {
            return "Ngân sách không đủ!";
        }

        // Trừ ngân sách
        budgetDAO.updateSpent(order.getBudgetId(), order.getTotalAmount());
        // Cập nhật trạng thái
        orderDAO.updateStatus(orderId, "APPROVED");
        return null;
    }

    /**
     * Nhận hàng → cộng tồn kho
     */
    public String receiveOrder(int orderId) {
        PurchaseOrder order = orderDAO.findById(orderId);
        if (order == null) return "Đơn mua không tồn tại!";
        if (!"APPROVED".equals(order.getStatus())) return "Chỉ nhận hàng được đơn đã duyệt!";

        // Cộng tồn kho cho từng tài liệu
        for (PurchaseOrderItem item : order.getItems()) {
            documentDAO.updateStock(item.getDocumentId(), item.getQuantity());
        }
        orderDAO.updateStatus(orderId, "RECEIVED");
        return null;
    }

    /**
     * Hủy đơn → hoàn ngân sách nếu đã duyệt
     */
    public String cancelOrder(int orderId) {
        PurchaseOrder order = orderDAO.findById(orderId);
        if (order == null) return "Đơn mua không tồn tại!";
        if ("RECEIVED".equals(order.getStatus())) return "Không thể hủy đơn đã nhận hàng!";
        if ("CANCELLED".equals(order.getStatus())) return "Đơn đã bị hủy rồi!";

        // Nếu đã duyệt → hoàn ngân sách
        if ("APPROVED".equals(order.getStatus())) {
            budgetDAO.updateSpent(order.getBudgetId(), -order.getTotalAmount());
        }
        orderDAO.updateStatus(orderId, "CANCELLED");
        return null;
    }
}
