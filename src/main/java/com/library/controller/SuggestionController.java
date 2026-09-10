package com.library.controller;

import com.library.dao.PurchaseSuggestionDAO;
import com.library.model.PurchaseSuggestion;

import java.util.List;

/**
 * Controller: Quản lý đề xuất mua sách
 */
public class SuggestionController {

    private final PurchaseSuggestionDAO dao = new PurchaseSuggestionDAO();

    public SuggestionController() {
        dao.createTableIfNotExists();
    }

    public List<PurchaseSuggestion> getAll() {
        return dao.findAll();
    }

    public String saveSuggestion(PurchaseSuggestion s) {
        if (s.getBookTitle() == null || s.getBookTitle().isBlank()) {
            return "Tên sách không được để trống!";
        }
        if (s.getSuggestedBy() == null || s.getSuggestedBy().isBlank()) {
            return "Tên người đề xuất không được để trống!";
        }
        if (s.getQuantity() <= 0) {
            return "Số lượng phải > 0!";
        }
        return dao.save(s) ? null : "Lỗi lưu đề xuất!";
    }

    public String approve(int id, String note, int reviewedBy) {
        return dao.updateStatus(id, "APPROVED", note, reviewedBy)
                ? null : "Lỗi duyệt đề xuất!";
    }

    public String reject(int id, String note, int reviewedBy) {
        return dao.updateStatus(id, "REJECTED", note, reviewedBy)
                ? null : "Lỗi từ chối đề xuất!";
    }

    public String delete(int id) {
        return dao.delete(id) ? null : "Không thể xóa đề xuất đã duyệt/từ chối!";
    }

    public int countPending() {
        return dao.countPending();
    }
}
