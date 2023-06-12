package WebProject.WebProject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import WebProject.WebProject.entity.Cart;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart,Integer>{

//	@Query(value="DELETE FROM `cart` e WHERE e.id= ?1",nativeQuery = true)
//	void deleteById(int id);

	@Query(value = "select c.id,c.count, c.product_id, c.user_id from cart c join user u on c.user_id = u.id join product p on c.product_id=p.id where u.id=:id and p.is_active=1",nativeQuery = true)
	List<Cart> findAllByUserId(String id);
	
}
