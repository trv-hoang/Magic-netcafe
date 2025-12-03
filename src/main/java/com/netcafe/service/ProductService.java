package com.netcafe.service;

import com.netcafe.dao.ProductDAO;
import com.netcafe.model.Product;

import java.util.List;
import java.util.Optional;

public class ProductService {
    private final ProductDAO productDAO = new ProductDAO();

    public List<Product> getAllProducts() throws Exception {
        return productDAO.findAll();
    }

    public Optional<Product> getProductById(int id) throws Exception {
        return productDAO.findById(id);
    }

    public void createProduct(Product product) throws Exception {
        // Validation logic could go here
        if (product.getPrice() < 0) {
            throw new Exception("Price cannot be negative");
        }
        if (product.getStock() < 0) {
            throw new Exception("Stock cannot be negative");
        }
        productDAO.create(product);
    }

    public void updateProduct(Product product) throws Exception {
        if (product.getPrice() < 0) {
            throw new Exception("Price cannot be negative");
        }
        if (product.getStock() < 0) {
            throw new Exception("Stock cannot be negative");
        }
        productDAO.update(product);
    }

    public void deleteProduct(int id) throws Exception {
        productDAO.delete(id);
    }

    public void updateStock(int productId, int newStock) throws Exception {
        if (newStock < 0) {
            throw new Exception("Stock cannot be negative");
        }
        // Note: Ideally this should be transactional if part of a larger operation,
        // but for simple updates it's fine.
        // For transactional updates (like in BillingService), the Service passes the
        // Connection to the DAO directly.
        // This method is for standalone updates (e.g. from Admin Panel).
        try (java.sql.Connection conn = com.netcafe.util.DBPool.getConnection()) {
            productDAO.updateStock(conn, productId, newStock);
        }
    }
}
