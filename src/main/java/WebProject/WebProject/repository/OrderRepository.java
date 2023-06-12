package WebProject.WebProject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import WebProject.WebProject.entity.Order;
import WebProject.WebProject.entity.Product;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface OrderRepository extends JpaRepository<Order,Integer>{
	List<Order> findAllByUser_id(String user_id);

	Order findById(int id);
	
	@Query(value="Select * From `order` o ORDER BY o.id ASC  LIMIT 5;",nativeQuery = true)
	List<Order> findTop5RecentOrder();
	
	@Query(value="Select o.user_id From `order` o ORDER BY o.id ASC  LIMIT 5;",nativeQuery = true)
	List<String> findTop5RecentCustomer();
	
	Page<Order> findAll(Pageable pageable);

	void deleteById(int id);
	
	
	@Query(value="select * from `order` o where o.payment_method = ?1",nativeQuery = true)
	List<Order> findAllByPaymentMethod(String payment_Method);
	
	@Query(value="Select * From `order` o where o.payment_method = ?1 ORDER BY o.id DESC LIMIT 5;",nativeQuery = true)
	List<Order> findTop5OrderByPaymentMethod(String payment_method);

	@Modifying
	@Query(value = "update `order` p set p.status =:status where p.id =:id", nativeQuery = true)
	void updateOrderStatusById(int id, String status);

	@Modifying
	@Query(value = "SELECT * FROM `order` where `order`.status = 'Delivering' limit 9 OFFSET :offset",nativeQuery = true)
	List<Order> getListOrder(int offset);

	@Query(value = "select count(`order`.id) from `order` where `order`.status = 'Delivering'", nativeQuery = true)
	int getTotalOrder();
	
}
