package WebProject.WebProject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import WebProject.WebProject.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem,Integer>{

	List<OrderItem> findAllByOrder_id(int id);

	void deleteById(int id);
	
}
