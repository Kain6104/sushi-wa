package com.mcnz.rps.repository;

import com.mcnz.rps.model.OrderDetail;
import com.mcnz.rps.model.User;
import com.mcnz.rps.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//Import cho @Query và @Param (Spring Data JPA)
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//Import cho List (Java Collections Framework)
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
//Import cho LocalDate (Java 8 Date and Time API)
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
	@Query("SELECT m.name, SUM(od.quantity), SUM(od.price * od.quantity) " +
		       "FROM OrderDetail od " +
		       "JOIN od.menuItems m " +
		       "JOIN od.order o " +
		       "WHERE o.status = 'DELIVERED' " +  // Chỉ lấy đơn hàng đã giao
		       "AND (:startDateTime IS NULL OR o.orderDate >= :startDateTime) " +
		       "AND (:endDateTime IS NULL OR o.orderDate <= :endDateTime) " +
		       "GROUP BY m.name " +
		       "ORDER BY SUM(od.quantity) DESC")
		List<Object[]> findMenuItemSales(@Param("startDateTime") LocalDateTime startDateTime, 
		                                 @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT SUM(od.price * od.quantity) " +
           "FROM OrderDetail od " +
           "JOIN od.order o " +
           "WHERE o.orderDate BETWEEN :startDateTime AND :endDateTime")
    double findTotalRevenueBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("SELECT SUM(od.quantity) " +
           "FROM OrderDetail od " +
           "JOIN od.order o " +
           "WHERE o.orderDate BETWEEN :startDateTime AND :endDateTime")
    int findTotalSalesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    @Modifying
    @Transactional
    @Query("DELETE FROM OrderDetail od WHERE od.order IN (SELECT o FROM Order o WHERE o.user = :user)")
    void deleteByUser(@Param("user") User user);
    void deleteByOrder(Order order);
    void deleteByOrderIn(List<Order> orders);

    @Query("SELECT od FROM OrderDetail od WHERE od.order.orderDate BETWEEN :startDateTime AND :endDateTime")
    List<OrderDetail> findByOrderDateBetween(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);
 // Lọc tip 5 món ăn
    @Query("SELECT d.menuItems.name, SUM(d.quantity) as totalQuantity, d.menuItems.token " +
    	       "FROM OrderDetail d " +
    	       "WHERE d.order.user.id = :userId " +
    	       "AND d.order.status = 'DELIVERED' " +  // Chỉ lấy đơn hàng đã giao
    	       "GROUP BY d.menuItems.name, d.menuItems.token " +
    	       "ORDER BY totalQuantity DESC")
    	List<Object[]> findTop5MenuItemsByUserId(@Param("userId") Long userId);
@Query("SELECT m.token, SUM(od.quantity) " +
	       "FROM OrderDetail od " +
	       "JOIN od.menuItems m " +
	       "JOIN od.order o " +
	       "WHERE o.status = 'DELIVERED' " +  // Chỉ lấy đơn hàng đã giao
	       "GROUP BY m.token")
	List<Object[]> findAllSoldQuantities();


}

