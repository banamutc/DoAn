package WebProject.WebProject.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import WebProject.WebProject.entity.Order;
import WebProject.WebProject.repository.OrderRepository;
import WebProject.WebProject.service.OrderService;
@Service
public class OrderServiceImpl implements OrderService{

	private static final String EDIT_STATUS = "Successful Delivery";
	@Autowired
	OrderRepository orderRepository;
	@Override
	public void saveOrder(Order order) {
		orderRepository.save(order);
	}
	@Override
	public List<Order> getAllOrderByUser_Id(String id) {
		return orderRepository.findAllByUser_id(id);
	}
	@Override
	public Order findById(int id) {
		return orderRepository.findById(id);
	}
	@Override
	public List<Order> findAll() {
		return orderRepository.findAll();
	}
	@Override
	public List<Order> findTop5RecentOrder() {
		return orderRepository.findTop5RecentOrder();
	}
	@Override
	public List<String> findTop5RecentCustomer() {
		return orderRepository.findTop5RecentCustomer();
	}
	@Override
	public Page<Order> findAll(Pageable pageable) {
		return orderRepository.findAll(pageable);
	}
	@Override
	public void deleteById(int id) {
		orderRepository.deleteById(id);
	}

	@Override
	public void editOrderById(int id) {
		orderRepository.updateOrderStatusById(id, EDIT_STATUS);
	}

	@Override
	public List<Order> findAllByPaymentMethod(String paymentMethod) {
		return orderRepository.findAllByPaymentMethod(paymentMethod);
	}
	@Override
	public List<Order> findTop5OrderByPaymentMethod(String paymentMethod) {
		return orderRepository.findTop5OrderByPaymentMethod(paymentMethod);
	}
	
	
}
