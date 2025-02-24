package com.example.ECM.controller;

import com.example.ECM.model.Order;
import com.example.ECM.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/orders")  // Endpoint chính của Order
public class OrderController {

    private static final Logger logger = Logger.getLogger(OrderController.class.getName());

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 📌 API để đặt hàng từ giỏ hàng
    @PostMapping("/checkout/{userId}") // Đổi từ @RequestParam sang @PathVariable
    public ResponseEntity<Order> checkout(@PathVariable Long userId) {
        Order newOrder = orderService.createOrder(userId);
        return ResponseEntity.ok(newOrder);
    }

    // 📌 API lấy đơn hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        logger.info("📢 [GET ORDER] Lấy đơn hàng ID: " + id);
        try {
            Order order = orderService.getOrderById(id);
            logger.info("✅ Đơn hàng tìm thấy: " + order);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Không tìm thấy đơn hàng ID: " + id, e);
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 📌 API lấy danh sách đơn hàng của người dùng
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        logger.info("📢 [GET USER ORDERS] Lấy đơn hàng của userId: " + userId);
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            logger.info("✅ Số đơn hàng tìm thấy: " + orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Lỗi khi lấy đơn hàng của userId: " + userId, e);
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 📌 API lấy danh sách tất cả đơn hàng (admin)
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        logger.info("📢 [GET ALL ORDERS] Lấy tất cả đơn hàng");
        try {
            List<Order> orders = orderService.getAllOrders();
            logger.info("✅ Tổng số đơn hàng: " + orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Lỗi khi lấy tất cả đơn hàng", e);
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 📌 API cập nhật trạng thái đơn hàng
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Order updatedOrder) {
        logger.info("📢 [UPDATE ORDER] Cập nhật đơn hàng ID: " + id + " với trạng thái mới: " + updatedOrder.getStatus());
        try {
            Order order = orderService.updateOrder(id, updatedOrder);
            logger.info("✅ Đơn hàng đã cập nhật: " + order);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Lỗi khi cập nhật đơn hàng ID: " + id, e);
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 📌 API xóa đơn hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        logger.info("📢 [DELETE ORDER] Xóa đơn hàng ID: " + id);
        try {
            orderService.deleteOrder(id);
            logger.info("✅ Đã xóa đơn hàng ID: " + id);
            return ResponseEntity.ok("Order deleted successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Lỗi khi xóa đơn hàng ID: " + id, e);
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}
