package WebProject.WebProject.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import WebProject.WebProject.entity.Cart;
import WebProject.WebProject.repository.CartRepository;
import WebProject.WebProject.service.CartService;

@Service
public class CartServiceImpl implements CartService{

	@Autowired
	CartRepository cartRepository;

	@Override
	public void deleteById(int id) {
		cartRepository.deleteById(id);
	}
	@Override
	public List<Cart> getAllCartByUserId(String id) {
		return cartRepository.findAllByUserId(id);
	}
	@Override
	public void saveCart(Cart cart) {
		cartRepository.save(cart);
	}
}
