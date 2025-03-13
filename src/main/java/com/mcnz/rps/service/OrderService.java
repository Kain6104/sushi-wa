package com.mcnz.rps.service;

import com.mcnz.rps.model.Order;
import com.mcnz.rps.model.User;
import com.mcnz.rps.repository.OrderRepository;
import com.mcnz.rps.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mcnz.rps.repository.OrderRepository;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.transaction.annotation.Transactional;

import com.mcnz.rps.repository.*;
import com.mcnz.rps.model.MenuItems;
import com.mcnz.rps.model.OrderDetail;


import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private MenuItemsRepository menuItemsRepository;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public List<Order> searchOrders(String query) {
        Order order = orderRepository.findByOrderCode(query);
        return order != null ? List.of(order) : new ArrayList<>();
    }

    public Order searchOrderByOrderCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode);
    }

    /**
     * Xử lý thanh toán đơn hàng và trừ điểm thưởng nếu có.
     * @param order Đối tượng đơn hàng cần thanh toán.
     * @param user Người dùng thực hiện thanh toán.
     * @param points Số điểm thưởng người dùng muốn sử dụng cho đơn hàng.
     * @return Trả về true nếu thanh toán thành công.
     */
    public boolean processCheckout(Order order, User user, int points) {
        // Kiểm tra và trừ điểm thưởng
        if (points > 0) {
            int pointsToSubtract = Math.min(points, user.getPoints());
            user.setPoints(user.getPoints() - pointsToSubtract);
        }
        
        // Lưu đơn hàng và cập nhật thông tin người dùng
        orderRepository.save(order);
        userRepository.save(user);
        return true;
    }

    /**
     * Xác nhận đơn hàng đã giao và tích điểm cho người dùng.
     * @param orderId ID của đơn hàng cần xác nhận.
     */
    public void confirmOrder(Long orderId) {
        // Tìm đơn hàng theo ID
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order != null && "PENDING".equals(order.getStatus())) {
            // Cập nhật trạng thái đơn hàng thành DELIVERED
            order.setStatus("DELIVERED");

            // Tính số điểm thưởng dựa trên tổng số tiền thanh toán
            int earnedPoints = (int) (order.getTotalAmount() / 10000); // 1 điểm = 10,000 VND
            User user = order.getUser();

            // Cộng điểm thưởng cho khách hàng
            if (user != null) {
                user.setPoints(user.getPoints() + earnedPoints);
                userRepository.save(user); // Lưu thông tin người dùng với điểm đã cập nhật
            }

            // Lưu lại trạng thái đơn hàng
            orderRepository.save(order);
        }
    }

    @Transactional
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByUser_CustomerId(customerId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    // Convert Date to LocalDateTime (utility function)
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public List<Order> getOrdersForDay(Date startOfDay, Date endOfDay) {
        LocalDateTime start = convertToLocalDateTime(startOfDay);
        LocalDateTime end = convertToLocalDateTime(endOfDay);
        return orderRepository.findByOrderDateBetween(start, end);
    }

    // Lấy tổng doanh thu trong ngày
    public double getTotalRevenueForDay(Date startOfDay, Date endOfDay) {
        List<Order> orders = getOrdersForDay(startOfDay, endOfDay);
        return orders.stream().mapToDouble(Order::getTotalAmount).sum();
    }

    // Lấy tổng số đơn hàng trong ngày
    public int getTotalSalesForDay(Date startOfDay, Date endOfDay) {
        List<Order> orders = getOrdersForDay(startOfDay, endOfDay);
        return orders.size();
    }
   
 
   

    public int getTotalSalesForDay(LocalDate startDate, LocalDate endDate) {
        // Chuyển đổi LocalDate sang LocalDateTime với giờ 00:00:00 cho startDate và 23:59:59 cho endDate
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return orderRepository.countOrdersByDateRange(startDateTime, endDateTime);
    }

	public Order getOrderByCode(String orderCode) {
	    return orderRepository.findByOrderCode(orderCode);  // Gọi phương thức từ repository
	}
	
	
	
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    // Phương thức tính tổng chi tiêu của người dùng
   

    // Định nghĩa phương thức findAllOrders()
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUser_CustomerId(userId); // Corrected method name
    }

public void confirmOrderAsDelivered(Long orderId) {
    // Lấy thông tin đơn hàng từ cơ sở dữ liệu
    Order order = orderRepository.findById(orderId).orElseThrow(() ->
        new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId)
    );

    // Kiểm tra trạng thái đơn hàng, chỉ cho phép khi trạng thái là CONFIRMED
    if (!"CONFIRMED".equalsIgnoreCase(order.getStatus())) {
        throw new IllegalStateException("Chỉ có thể giao hàng khi đơn hàng đã được xác nhận.");
    }

    // Kiểm tra tổng số tiền hợp lệ
    if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
        throw new IllegalArgumentException("Tổng số tiền không hợp lệ.");
    }

    // Lấy thông tin người dùng
    User user = order.getUser();
    if (user == null) {
        throw new IllegalStateException("Không tìm thấy thông tin người dùng liên quan đến đơn hàng.");
    }

    // Cập nhật trạng thái đơn hàng thành DELIVERED
    order.setStatus("DELIVERED");

    // Tính điểm thưởng (1 điểm = 10,000 VND)
    int earnedPoints = (int) (order.getTotalAmount() / 10000);
    user.setPoints(user.getPoints() + earnedPoints);

    // Lưu thông tin vào cơ sở dữ liệu
    userRepository.save(user);
    orderRepository.save(order);

    System.out.println("Cộng " + earnedPoints + " điểm cho người dùng: " + user.getUsername());
}
    @Transactional
    public Order createOrder(User user, List<OrderDetail> orderDetails, String address, String phone) {
        if (user == null) {
            throw new IllegalArgumentException("Người dùng không tồn tại");
        }
        if (orderDetails == null || orderDetails.isEmpty()) {
            throw new IllegalArgumentException("Danh sách chi tiết đơn hàng không được trống");
        }

        // Tạo đối tượng Order mới
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPhone(phone);
        order.setOrderDate(java.time.LocalDateTime.now());
        order.setStatus("Pending");

        // Tính tổng giá và liên kết chi tiết đơn hàng
        double totalPrice = 0;
        for (OrderDetail detail : orderDetails) {
            if (detail.getPrice() <= 0 || detail.getQuantity() <= 0) {
                throw new IllegalArgumentException("Chi tiết đơn hàng không hợp lệ: giá hoặc số lượng không hợp lệ");
            }
            detail.setOrder(order); // Gắn order vào từng detail
            totalPrice += detail.getPrice() * detail.getQuantity();
        }

        order.setOrderDetails(orderDetails); // Gắn danh sách OrderDetail vào Order
        order.setTotalPrice(totalPrice);

        // Lưu Order (cả OrderDetail sẽ được lưu nhờ CascadeType.ALL)
        try {
            orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu đơn hàng: " + e.getMessage(), e);
        }

        return order;
    }


    public MenuItems getMenuItemById(int itemId) {
        return menuItemsRepository.findById(itemId).orElse(null);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
 // Lưu thông tin OrderDetail
    @Transactional
    public void saveOrderDetail(OrderDetail orderDetail) {
        orderDetailRepository.save(orderDetail);
    } 
  
    @Transactional
    public void cancelOrder(Long orderId) {
        // Lấy thông tin đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra nếu trạng thái đơn hàng không phải CANCELED
        if ("CANCELED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Đơn hàng đã bị hủy trước đó.");
        }

        // Lấy thông tin người dùng liên quan đến đơn hàng
        User user = order.getUser();
        if (user == null) {
            throw new IllegalStateException("Không tìm thấy người dùng liên quan đến đơn hàng.");
        }

        // Trừ điểm đã cộng trước đó nếu trạng thái trước đó là DELIVERED
        if ("DELIVERED".equalsIgnoreCase(order.getStatus())) {
            int pointsToDeduct = (int) (order.getTotalAmount() / 10000); // 1 điểm = 10,000 VND
            user.setPoints(Math.max(user.getPoints() - pointsToDeduct, 0)); // Trừ điểm, không để âm
        }

        // Cập nhật trạng thái đơn hàng
        order.setStatus("CANCELED");

        // Lưu thông tin người dùng và đơn hàng
        userRepository.save(user);
        orderRepository.save(order);

        System.out.println("Đơn hàng đã được hủy: " + orderId);
    }


    public double getTotalRevenueForDay(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Tính doanh thu, bỏ qua đơn hàng bị hủy
        List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);
        return orders.stream()
                     .filter(order -> !"CANCELED".equalsIgnoreCase(order.getStatus())) // Bỏ qua đơn hàng bị hủy
                     .mapToDouble(Order::getTotalAmount)
                     .sum();
    }

    @Transactional
    public void cancelOrder(String orderCode) {
        // Lấy thông tin đơn hàng
        Order order = orderRepository.findByOrderCode(orderCode);
        if (order == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + orderCode);
        }

        // Kiểm tra trạng thái đơn hàng
        if (!"CANCELED".equalsIgnoreCase(order.getStatus())) {
            // Nếu đơn hàng đã giao, cần xử lý trừ điểm khách hàng
            if ("DELIVERED".equalsIgnoreCase(order.getStatus())) {
                User user = order.getUser();
                if (user != null) {
                    int pointsToDeduct = (int) (order.getTotalAmount() / 10000); // 1 điểm = 10,000 VND
                    user.setPoints(Math.max(user.getPoints() - pointsToDeduct, 0)); // Trừ điểm đã cộng
                    userRepository.save(user);
                }
            }

            // Cập nhật trạng thái đơn hàng
            order.setStatus("CANCELED");
            orderRepository.save(order);
        } else {
            throw new IllegalStateException("Đơn hàng đã bị hủy trước đó.");
        }
    }
    public double getTotalSpending(Long userId) {
        List<Order> orders = orderRepository.findByUser_CustomerId(userId);

        // Tính tổng chi tiêu, bỏ qua đơn hàng bị hủy
        return orders.stream()
                     .filter(order -> !"CANCELED".equalsIgnoreCase(order.getStatus())) // Bỏ qua đơn hàng bị hủy
                     .mapToDouble(Order::getTotalAmount)
                     .sum();
    }
    
    public double getTotalRevenueForDay(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);
        return orders.stream()
                     .filter(order -> "DELIVERED".equalsIgnoreCase(order.getStatus())) 
                     .mapToDouble(Order::getTotalAmount)
                     .sum();
    }

    public int getTotalSalesForDay(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);
        return (int) orders.stream()
                           .filter(order -> !"CANCELED".equalsIgnoreCase(order.getStatus())) // Loại bỏ đơn hàng bị hủy
                           .count();
    }
    public List<Object[]> getMenuItemSales(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Lấy danh sách OrderDetail trong khoảng thời gian
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderDateBetween(startDateTime, endDateTime);

        // Định dạng doanh thu với dấu chấm ngăn cách hàng nghìn
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // Đặt dấu chấm làm dấu ngăn cách
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);

        // Loại bỏ OrderDetail của các đơn hàng bị hủy và nhóm theo tên món ăn
        Map<String, double[]> groupedData = orderDetails.stream()
                .filter(detail -> !"CANCELED".equalsIgnoreCase(detail.getOrder().getStatus())) // Loại bỏ đơn hàng bị hủy
                .collect(Collectors.groupingBy(
                        detail -> detail.getMenuItems().getName(), // Nhóm theo tên món ăn
                        Collectors.reducing(
                                new double[]{0, 0}, // Khởi tạo mảng {tổng số lượng, tổng doanh thu}
                                detail -> new double[]{detail.getQuantity(), detail.getPrice() * detail.getQuantity()},
                                (a, b) -> new double[]{a[0] + b[0], a[1] + b[1]} // Cộng dồn số lượng và doanh thu
                        )
                ));

        // Chuyển đổi dữ liệu sang danh sách và định dạng doanh thu
        List<Object[]> result = groupedData.entrySet().stream()
                .map(entry -> new Object[]{
                        entry.getKey(),                     // Tên món ăn
                        (int) entry.getValue()[0],          // Tổng số lượng
                        entry.getValue()[1],                // Tổng doanh thu (giữ nguyên kiểu số cho tính toán thêm)
                        decimalFormat.format(entry.getValue()[1]) // Tổng doanh thu đã định dạng (hiển thị)
                })
                .collect(Collectors.toList());

        return result;
    }
    public List<Object[]> getCanceledMenuItemSales(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderDateBetween(startDateTime, endDateTime);

        return orderDetails.stream()
                .filter(detail -> "CANCELED".equalsIgnoreCase(detail.getOrder().getStatus())) // Chỉ lấy đơn hàng bị hủy
                .collect(Collectors.groupingBy(
                        detail -> detail.getMenuItems().getName(),
                        Collectors.mapping(
                                detail -> new double[]{detail.getQuantity(), detail.getPrice() * detail.getQuantity()},
                                Collectors.reducing(
                                        new double[]{0, 0},
                                        (a, b) -> new double[]{a[0] + b[0], a[1] + b[1]} // Tổng số lượng và doanh thu
                                )
                        )
                ))
                .entrySet()
                .stream()
                .map(entry -> new Object[]{
                        entry.getKey(),            // Tên món ăn
                        (int) entry.getValue()[0], // Số lượng
                        entry.getValue()[1]        // Doanh thu (double, chưa định dạng)
                })
                .collect(Collectors.toList());
    }

    public Map<String, Double> getRevenueByPaymentMethod(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Object[]> results = orderRepository.findRevenueByPaymentMethod(startDateTime, endDateTime);
        Map<String, Double> revenueMap = new LinkedHashMap<>();
        for (Object[] row : results) {
            String paymentMethod = (String) row[0];
            Double revenue = (Double) row[1];
            revenueMap.put(paymentMethod, revenue);
        }
        return revenueMap;
    }
    public List<Map<String, Object>> getTop5MenuItemsByUserId(Long userId) {
        List<Object[]> results = orderDetailRepository.findTop5MenuItemsByUserId(userId);
        List<Map<String, Object>> topItems = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", row[0]);           // Tên món ăn
            item.put("totalQuantity", row[1]);  // Tổng số lượng đã mua
            item.put("token", row[2]);          // Token sản phẩm (thêm mới)
            topItems.add(item);
        }
        return topItems;
    }

   
    public List<Map<String, Object>> getPointsHistoryByUserId(Long userId) {
        // Lấy các đơn hàng có trạng thái DELIVERED
        List<Object[]> results = orderRepository.findPointsHistoryByUserIdAndStatus(userId, "DELIVERED");
        List<Map<String, Object>> pointsHistory = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Object[] row : results) {
            Map<String, Object> history = new HashMap<>();
            history.put("orderCode", row[0]); // Mã đơn hàng
            history.put("date", ((LocalDateTime) row[1]).format(formatter)); // Ngày đặt hàng
            history.put("totalAmount", row[2]); // Tổng tiền thanh toán

            // Kiểm tra kiểu của pointsUsed trước khi ép kiểu
            Object pointsUsedObj = row[3];
            int pointsUsed = 0;

            if (pointsUsedObj instanceof Double) {
                pointsUsed = ((Double) pointsUsedObj).intValue();  // Nếu là Double, chuyển thành int
            } else if (pointsUsedObj instanceof Integer) {
                pointsUsed = (Integer) pointsUsedObj;  // Nếu là Integer, lấy trực tiếp
            }

            history.put("pointsUsed", pointsUsed); // Điểm đã sử dụng

            // Kiểm tra kiểu của rewardPoints trước khi ép kiểu
            Object rewardPointsObj = row[4];
            Double rewardPoints = 0.0;

            if (rewardPointsObj instanceof Double) {
                rewardPoints = (Double) rewardPointsObj; // Nếu là Double, gán trực tiếp
            } else if (rewardPointsObj instanceof Integer) {
                rewardPoints = ((Integer) rewardPointsObj).doubleValue(); // Nếu là Integer, chuyển thành Double
            }

            history.put("rewardPoints", rewardPoints); // Điểm được tích lũy

            pointsHistory.add(history);
        }

        return pointsHistory;
    }



    public void updateOrder(Order order) {
        orderRepository.save(order); // Lưu thông tin cập nhật vào database
    }
    public String canCancelOrder(Order order, Long userId, boolean isAdmin) {
        if (order == null) {
            return "Đơn hàng không tồn tại.";
        }

        // Admin có quyền hủy mọi trạng thái đơn hàng
        if (isAdmin) {
            return null; // Admin luôn có thể hủy
        }

        // Kiểm tra quyền sở hữu đơn hàng cho người dùng
        if (!order.getUser().getCustomerId().equals(userId)) {
            return "Bạn không có quyền hủy đơn hàng này.";
        }

        // Kiểm tra trạng thái đơn hàng
        if (!"PENDING".equals(order.getStatus())) {
            if ("DELIVERING".equals(order.getStatus())) {
                return "Đơn hàng đang được giao không thể hủy.";
            }
            if ("DELIVERED".equals(order.getStatus())) {
                return "Đơn hàng đã hoàn tất không thể hủy.";
            }
            return "Đơn hàng không ở trạng thái có thể hủy.";
        }

        return null; // Có thể hủy
    }
    //doanh thu dự kiến
    public double getProjectedRevenue(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Double projectedRevenue = orderRepository.findProjectedRevenue(startDateTime, endDateTime);
        return projectedRevenue != null ? projectedRevenue : 0.0; // Trả về 0 nếu không có kết quả
    }
    //Tổng điểm
    public double getTotalPointsUsed(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Double totalPointsUsed = orderRepository.findTotalPointsUsed(startDateTime, endDateTime);
        return totalPointsUsed != null ? totalPointsUsed : 0.0;
    }
    //Tổng giá trị voucher
    public double getTotalVoucherDiscountUsed(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Double totalVoucherDiscount = orderRepository.findTotalVoucherDiscountUsed(startDateTime, endDateTime);
        return totalVoucherDiscount != null ? totalVoucherDiscount : 0.0;
    }
    public List<Order> findOrdersByStatus(String status) {
        return orderRepository.findByStatus(status); // Giả sử bạn có phương thức này trong repository
    }


    public int getTotalCanceledOrders(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return orderRepository.countCanceledOrders(startDateTime, endDateTime);
    }
    public List<Object[]> getRevenueByDayAndHour(LocalDateTime start, LocalDateTime end) {
        List<Object[]> result = orderRepository.getRevenueByDayAndHour(start, end);
        return result != null ? result : new ArrayList<>();
    }

    public Map<String, Double> getTotalDiscountPerVoucher(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Order> deliveredOrders = orderRepository.findDeliveredOrdersWithinTimeRange(startDateTime, endDateTime);
        Map<String, Double> discountMap = new HashMap<>();

        for (Order order : deliveredOrders) {
            String voucherCode = order.getVoucherCode();
            Double voucherDiscount = order.getVoucherDiscount(); // Lấy đúng từ voucher_discount

            if (voucherCode != null && voucherDiscount != null) { // Kiểm tra null trước
                discountMap.put(voucherCode, discountMap.getOrDefault(voucherCode, 0.0) + voucherDiscount);
            }
        }

        return discountMap;
    }
    public double getTotalDiscountForVoucher(String voucherCode) {
        List<Order> deliveredOrders = orderRepository.findByVoucherCode(voucherCode);

        return deliveredOrders.stream()
                              .mapToDouble(order -> order.getVoucherDiscount() != null ? order.getVoucherDiscount() : 0.0)
                              .sum();
    }
    public List<Order> getOrdersByVoucherCode(String voucherCode) {
        return orderRepository.findByVoucherCode(voucherCode);
    }
    public Map<String, Double> getTotalPointsUsedPerUser(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Order> deliveredOrders = orderRepository.findDeliveredOrdersWithinTimeRange(startDateTime, endDateTime);
        Map<String, Double> pointsUsedMap = new HashMap<>();

        for (Order order : deliveredOrders) {
            if (order.getStatus().equals("DELIVERED") && order.getPointsUsed() > 0) { // Kiểm tra trạng thái & điểm sử dụng
                String username = order.getUser().getUsername(); // Lấy tên người dùng
                Double pointsUsed = (double) order.getPointsUsed(); // Chuyển đổi int -> Double

                pointsUsedMap.put(username, pointsUsedMap.getOrDefault(username, 0.0) + pointsUsed);
            }
        }
        return pointsUsedMap;
    }
}
