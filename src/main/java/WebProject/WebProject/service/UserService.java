package WebProject.WebProject.service;

import java.util.List;

import WebProject.WebProject.entity.Product;
import WebProject.WebProject.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
	List<User> getAllUser();

	User saveUser(User user);

	User updateUser(User user);

	void deleteUserById(String id);
	
	User getUserByEmail(String email);

	User findByIdAndRole(String id, String role);

	List<User> getAll(Pageable pageable);
	Page<User> findAll(Pageable pageable);

	List<User> findByUserNameContaining(String name);
	Page<User> findByUserNameContaining(String name, Pageable pageable);
}
