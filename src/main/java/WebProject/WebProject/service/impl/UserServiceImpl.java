package WebProject.WebProject.service.impl;

import java.util.List;

import WebProject.WebProject.entity.Product;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import WebProject.WebProject.entity.User;
import WebProject.WebProject.repository.UserRepository;
import WebProject.WebProject.service.UserService;

@Service
public class UserServiceImpl implements UserService{

//	@Autowired
//	SessionFactory factory;
	private final int DELETE_STATUS = 2;
//	private UserRepository userRepository;
//	public UserServiceImpl(UserRepository userRepository) {
//		super();
//		this.userRepository=userRepository;
//	}
	@Autowired
	UserRepository userRepository;
	@Override
	public List<User> getAllUser() {
		// TODO Auto-generated method stub
		return userRepository.findAll();
	}

	@Override
	public User saveUser(User user) {
		// TODO Auto-generated method stub
		return userRepository.save(user);
	}

	@Override
	public User updateUser(User user) {
		// TODO Auto-generated method stub
		return userRepository.save(user);
	}

	@Override
	public void deleteUserById(String id) {
		// TODO Auto-generated method stub
		userRepository.updateUserActiveById(id, DELETE_STATUS);
	}
	@Override
	public User getUserByEmail(String email) {
		// TODO Auto-generated method stub
		return userRepository.findByEmail(email);
	}
	@Override
	public User findByIdAndRole(String id, String role) {
		return userRepository.findByIdAndRole(id, role);
	}

	@Override
	public List<User> getAll(Pageable pageable) {
		return userRepository.getListUser(pageable.getPageSize() * pageable.getPageNumber());
	}

	@Override
	public Page<User> findAll(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	@Override
	public List<User> findByUserNameContaining(String name) {
		return userRepository.findByUserNameContaining(name);
	}

	@Override
	public Page<User> findByUserNameContaining(String name, Pageable pageable) {
		return userRepository.findByUserNameContaining(name, pageable);
	}


}
