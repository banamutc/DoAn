package WebProject.WebProject.service;

import java.util.List;

import WebProject.WebProject.entity.OrderItem;

public interface OrderItemService {

	List<OrderItem> getAllByOrder_Id(int id);
	public void saveOrderItem(OrderItem order_Item);
	void deleteById(int id);
}
