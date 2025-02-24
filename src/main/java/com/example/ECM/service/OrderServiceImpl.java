package com.example.ECM.service;

import com.example.ECM.model.Cart;
import com.example.ECM.model.Order;
import com.example.ECM.model.User;
import com.example.ECM.repository.CartRepository;
import com.example.ECM.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = Logger.getLogger(OrderServiceImpl.class.getName());

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public Order createOrder(Long userId) {
        logger.info("Bắt đầu tạo đơn hàng cho userId: " + userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.severe("Không tìm thấy giỏ hàng cho userId: " + userId);
                    return new RuntimeException("Không tìm thấy giỏ hàng");
                });

        logger.info("Giỏ hàng tìm thấy: " + cart);
        logger.info("Số lượng sản phẩm trong giỏ: " + cart.getCartItems().size());

        if (cart.getCartItems().isEmpty()) {
            logger.warning("Giỏ hàng trống, không thể đặt hàng!");
            throw new RuntimeException("Giỏ hàng trống, không thể đặt hàng!");
        }

        Order newOrder = new Order();
        newOrder.setUser(cart.getUser());

        // Kiểm tra xem cart.getUser() có null không
        if (cart.getUser() == null) {
            logger.severe("Lỗi: User trong giỏ hàng là null!");
            throw new RuntimeException("Không thể tạo đơn hàng vì thiếu thông tin người dùng.");
        }

        double totalPrice = cart.getCartItems().stream()
                .mapToDouble(item -> {
                    if (item.getProduct() == null) {
                        logger.severe("Sản phẩm trong giỏ hàng bị null!");
                        throw new RuntimeException("Sản phẩm trong giỏ hàng không hợp lệ.");
                    }
                    return item.getQuantity() * item.getProduct().getPrice();
                })
                .sum();

        newOrder.setTotalPrice(BigDecimal.valueOf(totalPrice));
        newOrder.setStatus("PENDING");

        logger.info("Tổng giá trị đơn hàng: " + totalPrice);
        logger.info("Lưu đơn hàng vào cơ sở dữ liệu...");

        Order savedOrder = orderRepository.save(newOrder);

        logger.info("Đơn hàng đã được tạo thành công: " + savedOrder);

        // Xóa giỏ hàng sau khi đặt hàng
        cart.clearItems();
        cartRepository.save(cart);
        logger.info("Đã xóa sản phẩm trong giỏ hàng sau khi đặt hàng.");

        return savedOrder;
    }

    @Override
    public Order getOrderById(Long id) {
        logger.info("Tìm đơn hàng với ID: " + id);
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    logger.severe("Không tìm thấy đơn hàng với ID: " + id);
                    return new RuntimeException("Order not found");
                });
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        logger.info("Lấy danh sách đơn hàng của userId: " + userId);
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<Order> getAllOrders() {
        logger.info("Lấy tất cả đơn hàng");
        return orderRepository.findAll();
    }

    @Override
    public Order updateOrder(Long id, Order updatedOrder) {
        logger.info("Cập nhật đơn hàng ID: " + id);
        return orderRepository.findById(id).map(order -> {
            order.setStatus(updatedOrder.getStatus());
            logger.info("Trạng thái đơn hàng mới: " + updatedOrder.getStatus());
            return orderRepository.save(order);
        }).orElseThrow(() -> {
            logger.severe("Không tìm thấy đơn hàng để cập nhật: " + id);
            return new RuntimeException("Order not found");
        });
    }

    public Order updateOrderStatus(Long orderId, String status) {
        logger.info("Cập nhật trạng thái đơn hàng ID: " + orderId + " thành " + status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.severe("Không tìm thấy đơn hàng với ID: " + orderId);
                    return new RuntimeException("Không tìm thấy đơn hàng");
                });
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long id) {
        logger.info("Xóa đơn hàng với ID: " + id);
        if (!orderRepository.existsById(id)) {
            logger.severe("Không tìm thấy đơn hàng để xóa: " + id);
            throw new RuntimeException("Order not found");
        }
        orderRepository.deleteById(id);
        logger.info("Đã xóa đơn hàng với ID: " + id);
    }
}
