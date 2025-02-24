package com.example.ECM.service;

import com.example.ECM.model.*;
import com.example.ECM.repository.CartRepository;
import com.example.ECM.repository.OrderItemRepository;
import com.example.ECM.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = Logger.getLogger(OrderServiceImpl.class.getName());

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public Order createOrder(Long userId) {
        logger.info("Bắt đầu tạo đơn hàng cho userId: " + userId);

        // Tìm giỏ hàng của user
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

        // Tạo đơn hàng mới
        Order newOrder = new Order();
        newOrder.setUser(cart.getUser());
        newOrder.setStatus("PENDING");

        // Tạo danh sách OrderItem từ giỏ hàng
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(newOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(BigDecimal.valueOf(cartItem.getProduct().getPrice()).multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            orderItems.add(orderItem);
            totalPrice = totalPrice.add(orderItem.getPrice());
        }

        newOrder.setTotalPrice(totalPrice);
        newOrder.setOrderItems(orderItems);

        logger.info("Tổng giá trị đơn hàng: " + totalPrice);
        logger.info("Lưu đơn hàng vào cơ sở dữ liệu...");

        // Lưu đơn hàng (Hibernate sẽ tự động lưu OrderItem nhờ cascade = CascadeType.ALL)
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
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    logger.severe("Không tìm thấy đơn hàng với ID: " + id);
                    return new RuntimeException("Order not found");
                });

        // Load danh sách sản phẩm trong đơn hàng
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(id);
        order.setOrderItems(orderItems); // Đảm bảo danh sách sản phẩm được đưa vào đơn hàng

        return order;
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
