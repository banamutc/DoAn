package WebProject.WebProject.repository;

import java.util.List;

import WebProject.WebProject.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import WebProject.WebProject.entity.Order;
import WebProject.WebProject.entity.User;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long>{
	User findByEmail(String email);
	User findById(String id);

	@Query(value="select * from user u where u.id =:id and u.role =:role and u.is_active = 1 ",nativeQuery = true)
	User findByIdAndRole(String id, String role);
	
	void deleteById(String id);

	@Modifying
	@Query(value = "SELECT * FROM user where user.is_active = 1 limit 9 OFFSET :offset",nativeQuery = true)
	List<User> getListUser(int offset);
	@Query(value = "select count(user.id) from user where user.is_active = 1", nativeQuery = true)
	int getTotalUser();

	@Modifying
	@Query(value = "update user u set u.is_active =:status where u.id =:id ", nativeQuery = true)
	void updateUserActiveById(String id, int status);

	@Query(value="select * from `fashionstore`.user where `fashionstore`.user.user_name like %?1%",nativeQuery = true)
	Page<User> findByUserNameContaining(String name, Pageable pageable);

	@Query(value="select * from user p where p.user_name like %?1%",nativeQuery = true)
	List<User> findByUserNameContaining(String name);
}
