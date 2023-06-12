package WebProject.WebProject.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import WebProject.WebProject.entity.OrderItem;
import WebProject.WebProject.repository.OrderItemRepository;
import WebProject.WebProject.service.OrderItemService;

@Service
public class OrderItemServiceImpl implements OrderItemService {

	@Autowired
    OrderItemRepository order_ItemRepository;
	@Override
	public void saveOrderItem(OrderItem order_Item) {
		// TODO Auto-generated method stub
		order_ItemRepository.save(order_Item);
	}
	@Override
	public List<OrderItem> getAllByOrder_Id(int id) {
		// TODO Auto-generated method stub
		return order_ItemRepository.findAllByOrder_id(id);
	}
	@Override
	public void deleteById(int id) {
		order_ItemRepository.deleteById(id);
	}
}
