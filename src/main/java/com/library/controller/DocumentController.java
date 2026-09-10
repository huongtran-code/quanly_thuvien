package com.library.controller;

import com.library.dao.CategoryDAO;
import com.library.dao.DocumentDAO;
import com.library.model.Category;
import com.library.model.Document;

import java.util.List;

/**
 * Controller: Quản lý tài liệu & danh mục
 */
public class DocumentController {

    private final DocumentDAO documentDAO = new DocumentDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // ── Tài liệu ──

    public List<Document> getAllDocuments() {
        return documentDAO.findAll();
    }

    public List<Document> searchDocuments(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return documentDAO.findAll();
        }
        return documentDAO.findByKeyword(keyword.trim());
    }

    public List<Document> filterByCategory(int categoryId) {
        if (categoryId <= 0) {
            return documentDAO.findAll();
        }
        return documentDAO.findByCategoryId(categoryId);
    }

    public Document getDocumentById(int id) {
        return documentDAO.findById(id);
    }

    public Document findByIsbn(String isbn) {
        return documentDAO.findByIsbn(isbn);
    }

    public String saveDocument(Document doc) {
        // Validate
        if (doc.getTitle() == null || doc.getTitle().isBlank()) {
            return "Tên tài liệu không được để trống!";
        }
        if (doc.getUnitPrice() < 0) {
            return "Đơn giá không hợp lệ!";
        }
        if (doc.getStockQuantity() < 0) {
            return "Số lượng tồn không hợp lệ!";
        }

        boolean success;
        if (doc.getId() > 0) {
            success = documentDAO.update(doc);
        } else {
            success = documentDAO.save(doc);
        }
        return success ? null : "Lỗi lưu tài liệu!";
    }

    public String deleteDocument(int id) {
        return documentDAO.delete(id) ? null : "Không thể xóa tài liệu! Có thể đang được sử dụng trong đơn mua.";
    }

    // ── Danh mục ──

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public String saveCategory(Category cat) {
        if (cat.getName() == null || cat.getName().isBlank()) {
            return "Tên danh mục không được để trống!";
        }
        boolean success;
        if (cat.getId() > 0) {
            success = categoryDAO.update(cat);
        } else {
            success = categoryDAO.save(cat);
        }
        return success ? null : "Lỗi lưu danh mục!";
    }

    public String deleteCategory(int id) {
        return categoryDAO.delete(id) ? null : "Không thể xóa danh mục! Có thể đang được sử dụng.";
    }

    // ── Tồn kho thấp ──

    public List<Document> getLowStockDocuments(int threshold) {
        return documentDAO.findLowStock(threshold);
    }

    public List<Document> getLowStockDocuments() {
        return getLowStockDocuments(5); // default threshold = 5
    }

    // ── Tìm kiếm nâng cao ──

    public List<Document> advancedSearch(String title, String author, String isbn,
                                          String publisher, Integer yearFrom, Integer yearTo,
                                          Double priceFrom, Double priceTo,
                                          Integer stockFrom, Integer stockTo,
                                          Integer categoryId) {
        return documentDAO.advancedSearch(title, author, isbn, publisher,
                yearFrom, yearTo, priceFrom, priceTo, stockFrom, stockTo, categoryId);
    }
}
