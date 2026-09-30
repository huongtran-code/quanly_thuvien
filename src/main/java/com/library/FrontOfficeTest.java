package com.library;

import com.library.controller.*;
import com.library.model.*;
import com.library.util.AppConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test class để kiểm tra các tính năng front-office
 * Chạy sau khi đã import schema.sql với dữ liệu mẫu
 */
public class FrontOfficeTest {
    
    private static final CustomerController customerController = new CustomerController();
    private static final BorrowController borrowController = new BorrowController();
    private static final SaleController saleController = new SaleController();
    private static final FineController fineController = new FineController();
    private static final ReportController reportController = new ReportController();
    
    public static void main(String[] args) {
        System.out.println("=== FRONT-OFFICE FEATURES TEST ===\n");
        
        testCustomerFeatures();
        testBorrowFeatures();
        testSaleFeatures();
        testFineFeatures();
        testReportFeatures();
        
        System.out.println("\n=== TEST COMPLETED ===");
    }
    
    private static void testCustomerFeatures() {
        System.out.println("1. TESTING CUSTOMER FEATURES");
        System.out.println("   " + "=".repeat(50));
        
        // Test get all customers
        var customers = customerController.getAllCustomers();
        System.out.println("   ✓ Total customers: " + customers.size());
        
        // Test get by code
        Customer customer = customerController.getCustomerByCode("KH000001");
        if (customer != null) {
            System.out.println("   ✓ Found customer: " + customer.getFullName());
            System.out.println("   ✓ Status: " + customer.getStatus());
            System.out.println("   ✓ Can borrow: " + customer.canBorrow());
        }
        
        // Test search
        var searchResults = customerController.searchCustomers("Nguyen");
        System.out.println("   ✓ Search 'Nguyen': " + searchResults.size() + " results");
        
        // Test create customer
        Customer newCustomer = new Customer();
        newCustomer.setFullName("Test Customer");
        newCustomer.setEmail("test@example.com");
        newCustomer.setPhone("0999999999");
        newCustomer.setAddress("Test Address");
        newCustomer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        newCustomer.setIdCard("999999999");
        newCustomer.setStatus(AppConstants.CUSTOMER_STATUS_ACTIVE);
        
        if (customerController.createCustomer(newCustomer)) {
            System.out.println("   ✓ Created test customer: " + newCustomer.getCustomerCode());
        }
        
        // Test get by status
        var activeCustomers = customerController.getCustomersByStatus(AppConstants.CUSTOMER_STATUS_ACTIVE);
        System.out.println("   ✓ Active customers: " + activeCustomers.size());
        
        System.out.println();
    }
    
    private static void testBorrowFeatures() {
        System.out.println("2. TESTING BORROW FEATURES");
        System.out.println("   " + "=".repeat(50));
        
        // Test get all borrows
        var borrows = borrowController.getAllBorrows();
        System.out.println("   ✓ Total borrows: " + borrows.size());
        
        // Test get by status
        var borrowed = borrowController.getBorrowsByStatus(AppConstants.BORROW_STATUS_BORROWED);
        var returned = borrowController.getBorrowsByStatus(AppConstants.BORROW_STATUS_RETURNED);
        var overdue = borrowController.getOverdueBorrows();
        
        System.out.println("   ✓ Currently borrowed: " + borrowed.size());
        System.out.println("   ✓ Returned: " + returned.size());
        System.out.println("   ✓ Overdue: " + overdue.size());
        
        // Test get by customer
        var customerBorrows = borrowController.getBorrowsByCustomer(1);
        System.out.println("   ✓ Customer #1 borrows: " + customerBorrows.size());
        
        // Test search
        var searchResults = borrowController.searchBorrows("BR2023");
        System.out.println("   ✓ Search 'BR2023': " + searchResults.size() + " results");
        
        // Test borrow details
        if (!borrows.isEmpty()) {
            Borrow borrow = borrows.get(0);
            var items = borrowController.getBorrowItems(borrow.getId());
            System.out.println("   ✓ Borrow #" + borrow.getId() + " has " + items.size() + " items");
            System.out.println("   ✓ Is overdue: " + borrow.isOverdue());
            if (borrow.isOverdue()) {
                System.out.println("   ✓ Late days: " + borrow.calculateLateDays());
                System.out.println("   ✓ Late fee: " + borrow.calculateLateFee(5000) + " VND");
            }
        }
        
        System.out.println();
    }
    
    private static void testSaleFeatures() {
        System.out.println("3. TESTING SALE FEATURES");
        System.out.println("   " + "=".repeat(50));
        
        // Test get all sales
        var sales = saleController.getAllSales();
        System.out.println("   ✓ Total sales: " + sales.size());
        
        // Test get by customer
        var customerSales = saleController.getSalesByCustomer(1);
        System.out.println("   ✓ Customer #1 sales: " + customerSales.size());
        
        // Test get by date range
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
        var yearSales = saleController.getSalesByDateRange(startDate, endDate);
        System.out.println("   ✓ Sales in 2024: " + yearSales.size());
        
        // Test revenue
        var revenue = saleController.getTotalRevenue(startDate, endDate);
        System.out.println("   ✓ Total revenue 2024: " + revenue);
        
        // Test search
        var searchResults = saleController.searchSales("SL2024");
        System.out.println("   ✓ Search 'SL2024': " + searchResults.size() + " results");
        
        // Test sale details
        if (!sales.isEmpty()) {
            Sale sale = sales.get(0);
            var items = saleController.getSaleItems(sale.getId());
            System.out.println("   ✓ Sale #" + sale.getId() + " has " + items.size() + " items");
            System.out.println("   ✓ Total amount: " + sale.getTotalAmount() + " VND");
            System.out.println("   ✓ Discount: " + sale.getDiscountAmount() + " VND");
            System.out.println("   ✓ Final amount: " + sale.getFinalAmount() + " VND");
        }
        
        System.out.println();
    }
    
    private static void testFineFeatures() {
        System.out.println("4. TESTING FINE FEATURES");
        System.out.println("   " + "=".repeat(50));
        
        // Test get all fines
        var fines = fineController.getAllFines();
        System.out.println("   ✓ Total fines: " + fines.size());
        
        // Test get by status
        var unpaid = fineController.getFinesByStatus(AppConstants.FINE_STATUS_UNPAID);
        var paid = fineController.getFinesByStatus(AppConstants.FINE_STATUS_PAID);
        var waived = fineController.getFinesByStatus(AppConstants.FINE_STATUS_WAIVED);
        
        System.out.println("   ✓ Unpaid fines: " + unpaid.size());
        System.out.println("   ✓ Paid fines: " + paid.size());
        System.out.println("   ✓ Waived fines: " + waived.size());
        
        // Test get by customer
        var customerFines = fineController.getFinesByCustomer(1);
        System.out.println("   ✓ Customer #1 fines: " + customerFines.size());
        
        // Test unpaid fines by customer
        var customerUnpaid = fineController.getUnpaidFinesByCustomer(1);
        System.out.println("   ✓ Customer #1 unpaid fines: " + customerUnpaid.size());
        
        // Test has unpaid fines
        boolean hasUnpaid = fineController.hasUnpaidFines(1);
        System.out.println("   ✓ Customer #1 has unpaid fines: " + hasUnpaid);
        
        // Test fine details
        if (!fines.isEmpty()) {
            Fine fine = fines.get(0);
            System.out.println("   ✓ Fine #" + fine.getId() + " amount: " + fine.getAmount() + " VND");
            System.out.println("   ✓ Fine type: " + fine.getFineType());
            System.out.println("   ✓ Status: " + fine.getStatus());
            System.out.println("   ✓ Is paid: " + fine.isPaid());
            System.out.println("   ✓ Is unpaid: " + fine.isUnpaid());
        }
        
        System.out.println();
    }
    
    private static void testReportFeatures() {
        System.out.println("5. TESTING REPORT FEATURES");
        System.out.println("   " + "=".repeat(50));
        
        int currentYear = reportController.getCurrentYear();
        
        // Test back-office reports
        var monthlySpending = reportController.getMonthlySpending(currentYear);
        System.out.println("   ✓ Monthly spending data points: " + monthlySpending.size());
        
        var spendingBySupplier = reportController.getSpendingBySupplier(currentYear);
        System.out.println("   ✓ Spending by supplier: " + spendingBySupplier.size() + " suppliers");
        
        var spendingByCategory = reportController.getSpendingByCategory(currentYear);
        System.out.println("   ✓ Spending by category: " + spendingByCategory.size() + " categories");
        
        var orderStatus = reportController.getOrderCountByStatus();
        System.out.println("   ✓ Order status counts: " + orderStatus.size() + " statuses");
        
        var budgets = reportController.getBudgetSummary();
        System.out.println("   ✓ Budget summary: " + budgets.size() + " budgets");
        
        // Test front-office reports
        var customerStatus = reportController.getCustomerCountByStatus();
        System.out.println("   ✓ Customer by status: " + customerStatus);
        
        var borrowStatus = reportController.getBorrowCountByStatus();
        System.out.println("   ✓ Borrow by status: " + borrowStatus);
        
        var salesRevenue = reportController.getMonthlySalesRevenue(currentYear);
        System.out.println("   ✓ Monthly sales revenue: " + salesRevenue.size() + " months");
        
        double totalRevenue = reportController.getTotalSalesRevenue(currentYear);
        System.out.println("   ✓ Total sales revenue " + currentYear + ": " + totalRevenue + " VND");
        
        var fineStatus = reportController.getFineCountByStatus();
        System.out.println("   ✓ Fine by status: " + fineStatus);
        
        double unpaidFines = reportController.getTotalUnpaidFines();
        System.out.println("   ✓ Total unpaid fines: " + unpaidFines + " VND");
        
        double paidFines = reportController.getTotalPaidFines(currentYear);
        System.out.println("   ✓ Total paid fines " + currentYear + ": " + paidFines + " VND");
        
        System.out.println();
    }
}
