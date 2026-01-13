package dev.viresh.SpringEcom.service;

import dev.viresh.SpringEcom.Model.Order;
import dev.viresh.SpringEcom.Model.OrderItem;
import dev.viresh.SpringEcom.Model.Product;
import dev.viresh.SpringEcom.Model.dto.OrderItemRequest;
import dev.viresh.SpringEcom.Model.dto.OrderItemResponse;
import dev.viresh.SpringEcom.Model.dto.OrderRequest;
import dev.viresh.SpringEcom.Model.dto.OrderResponse;
import dev.viresh.SpringEcom.repo.OrderRepo;
import dev.viresh.SpringEcom.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    public OrderResponse placeOrder(OrderRequest request) {
        Order order = new Order();
        String orderId = "ORD" + UUID.randomUUID().toString().substring(0,8);
        order.setOrderId(orderId);
        order.setCustomerName(request.customerName());
        order.setEmail(request.email());
        order.setStatus("PLACED");
        order.setOrderDate(LocalDate.now());

        List<OrderItem> orderItems = new ArrayList<>();
        for(OrderItemRequest itemReq : request.items()){
            Product product = productRepo.findById(itemReq.productId())
                    .orElseThrow(()->new RuntimeException("Product Not Found"));
            product.setStockQuantity(product.getStockQuantity() - itemReq.quantity());
            productRepo.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())))
                    .order(order)
                    .build();

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        Order savedOrder = orderRepo.save(order);


        List<OrderItemResponse> orderItemResponses = new ArrayList<>();
        for(OrderItem item : savedOrder.getItems()){
            OrderItemResponse itemResponse = new OrderItemResponse(
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getTotalPrice()
                    );
            orderItemResponses.add(itemResponse);

        }

        OrderResponse orderResponse = new OrderResponse(savedOrder.getOrderId(),
                savedOrder.getCustomerName(),
                savedOrder.getEmail(),
                savedOrder.getStatus(),
                savedOrder.getOrderDate(),
                orderItemResponses);

        return orderResponse;
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepo.findAll();
        List<OrderResponse> orderResponses = new ArrayList<>();
        for(Order order:orders){
            List<OrderItemResponse> itemResponses = new ArrayList<>();

            for(OrderItem item : order.getItems()){
                OrderItemResponse itemResponse = new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getTotalPrice()
                );
                itemResponses.add(itemResponse);
            }

            OrderResponse orderResponse = new OrderResponse(
                order.getOrderId(),
                order.getCustomerName(),
                order.getEmail(),
                order.getStatus(),
                order.getOrderDate(),
                itemResponses
            );

            orderResponses.add(orderResponse);
        }
        return orderResponses;
    }
}
