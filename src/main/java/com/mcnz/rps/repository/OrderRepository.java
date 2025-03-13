package com.mcnz.rps.repository;

import com.mcnz.rps.model.Order;
import com.mcnz.rps.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find order by unique order code
    Order findByOrderCode(String orderCode);

    // Find all orders by status
    List<Order> findByStatus(String status);

    // Find orders by user (using the customerId field from User)
    List<Order> findByUser_CustomerId(Long customerId);

    // Query orders by user explicitly using JPQL
    @Query("SELECT o FROM Order o WHERE o.user.customerId = :customerId")
    List<Order> findOrdersByCustomerId(@Param("customerId") Long customerId);

    // Find orders between two dates (using LocalDateTime)
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Query to calculate total revenue for a given day
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :startOfDay AND :endOfDay")
    double sumTotalRevenueForDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // Query to count total sales for a given day
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startOfDay AND :endOfDay")
    int countTotalSalesForDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

   
    // Calculate total revenue for a date range
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Double sumRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Count total orders for a date range
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    int countOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    // Remove unnecessary or redundant methods
    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :status WHERE o.orderId = :orderId")
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") String status);
    
    @Modifying
    @Transactional
    void deleteByUser(User user);
    List<Order> findByUser(User user); 

    @Query("SELECT o.paymentMethod, SUM(o.totalAmount) " +
    	       "FROM Order o " +
    	       "WHERE o.status = 'DELIVERED' " +
    	       "AND o.orderDate BETWEEN :startDateTime AND :endDateTime " +
    	       "GROUP BY o.paymentMethod")
    	List<Object[]> findRevenueByPaymentMethod(
    	    @Param("startDateTime") LocalDateTime startDateTime,
    	    @Param("endDateTime") LocalDateTime endDateTime);

	@Query("SELECT o.orderCode, o.orderDate, o.totalAmount, o.pointsUsed, " +
		       "FLOOR(o.totalAmount / 10000) AS rewardPoints " +
		       "FROM Order o " +
		       "WHERE o.user.id = :userId AND o.status = :status " +
		       "ORDER BY o.orderDate DESC")
		List<Object[]> findPointsHistoryByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

	   // Đếm số lượt sử dụng của từng voucher
    @Query("SELECT o.voucherCode, COUNT(o), o.user.id, o.id " +
           "FROM Order o " +
           "WHERE o.voucherCode IS NOT NULL " +
           "GROUP BY o.voucherCode, o.user.id, o.id")
    List<Object[]> findVoucherUsage();
    boolean existsByOrderCode(String orderCode);
    
    //Doanh thu dự kiến
    @Query("SELECT SUM(o.totalAmount) " +
    	       "FROM Order o " +
    	       "WHERE o.status NOT IN ('CANCELED', 'DELIVERED') " +
    	       "AND o.orderDate BETWEEN :startDateTime AND :endDateTime")
    	Double findProjectedRevenue(
    	    @Param("startDateTime") LocalDateTime startDateTime,
    	    @Param("endDateTime") LocalDateTime endDateTime);

    //điểm
    @Query("SELECT SUM(o.pointsUsed) " +
    	       "FROM Order o " +
    	       "WHERE o.orderDate BETWEEN :startDateTime AND :endDateTime " +
    	       "AND o.status = 'DELIVERED'")
    	Double findTotalPointsUsed(
    	    @Param("startDateTime") LocalDateTime startDateTime,
    	    @Param("endDateTime") LocalDateTime endDateTime);
    //giá trị voucher
    @Query("SELECT SUM(o.voucherDiscount) " +
    	       "FROM Order o " +
    	       "WHERE o.orderDate BETWEEN :startDateTime AND :endDateTime " +
    	       "AND o.voucherCode IS NOT NULL " +
    	       "AND o.status = 'DELIVERED'")
    	Double findTotalVoucherDiscountUsed(
    	    @Param("startDateTime") LocalDateTime startDateTime,
    	    @Param("endDateTime") LocalDateTime endDateTime);
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'CANCELED' AND o.orderDate BETWEEN :startDateTime AND :endDateTime")
    int countCanceledOrders(@Param("startDateTime") LocalDateTime startDateTime, 
                            @Param("endDateTime") LocalDateTime endDateTime);
    @Query("SELECT DATE(o.orderDate) AS orderDay, HOUR(o.orderDate) AS orderHour, SUM(o.totalAmount) " +
    	       "FROM Order o " +
    	       "WHERE o.orderDate BETWEEN :startDateTime AND :endDateTime " +
    	       "AND o.status = 'DELIVERED' " +  // Thêm dấu cách trước GROUP BY
    	       "GROUP BY orderDay, orderHour ORDER BY orderDay, orderHour")
    	List<Object[]> getRevenueByDayAndHour(@Param("startDateTime") LocalDateTime startDateTime,
    	                                      @Param("endDateTime") LocalDateTime endDateTime);
    	@Query("SELECT o FROM Order o WHERE o.status = 'DELIVERED' AND o.orderDate BETWEEN :startDate AND :endDate")
    	List<Order> findDeliveredOrdersWithinTimeRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    	@Query("SELECT o FROM Order o WHERE o.status = 'DELIVERED'")
        List<Order> findDeliveredOrders();
    	@Query("SELECT o FROM Order o WHERE o.voucherCode = :voucherCode")
    	List<Order> findByVoucherCode(@Param("voucherCode") String voucherCode);
    	@Query("SELECT o.user.id, SUM(o.pointsUsed) " +
    		       "FROM Order o " +
    		       "WHERE o.pointsUsed > 0  " +
    		       "GROUP BY o.user.id")
    		List<Object[]> findUsersWithPointsUsed();
    		@Query("SELECT o.user.id, SUM(o.pointsUsed) " +
    			       "FROM Order o " +
    			       "WHERE o.pointsUsed > 0 AND o.status = 'DELIVERED'" +
    			       "AND o.orderDate BETWEEN :startDateTime AND :endDateTime " +
    			       "GROUP BY o.user.id")
    			List<Object[]> findUsersWithPointsUsedByDateRange(
    			    @Param("startDateTime") LocalDateTime startDateTime,
    			    @Param("endDateTime") LocalDateTime endDateTime);

}
