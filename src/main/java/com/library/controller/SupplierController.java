package com.library.controller;

import com.library.dao.SupplierDAO;
import com.library.model.Supplier;

import java.util.List;

/**
 * Controller: Quản lý nhà cung cấp
 */
public class SupplierController {

    private final SupplierDAO supplierDAO = new SupplierDAO();

    public List<Supplier> getAllSuppliers() {
        return supplierDAO.findAll();
    }

    public List<Supplier> getActiveSuppliers() {
        return supplierDAO.findActive();
    }

    public String saveSupplier(Supplier sup) {
        if (sup.getName() == null || sup.getName().isBlank()) {
            return "Tên nhà cung cấp không được để trống!";
        }
        boolean success;
        if (sup.getId() > 0) {
            success = supplierDAO.update(sup);
        } else {
            success = supplierDAO.save(sup);
        }
        return success ? null : "Lỗi lưu nhà cung cấp!";
    }

    public String toggleActive(int id, boolean active) {
        return supplierDAO.toggleActive(id, active) ? null : "Lỗi cập nhật trạng thái!";
    }
}
