package WebProject.WebProject.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import WebProject.WebProject.entity.Product;

public interface ProductService {
	List<Product> getAllProduct();
	
	Product saveProduct(Product product);

	Product getProductById(int id);

	List<Product> getAllProductInCartByUserId(String id);
	Product updateProduct(Product product);

	void deleteProductImageById(int id);
	void deleteProductById(int id);

	List<Product> getAll(Pageable pageable);
	
	List<Product> findByProductNameContaining(String name);
	
	List<Product> findTop12ProductBestSellers();
	
	List<Product> findTop12ProductNewArrivals();

	Page<Product> findAll(Pageable pageable);

	Page<Product> findByProductNameContaining(String name, Pageable pageable);

	Page<Product> findByProductNameAndCategoryIdContaining(String name, int category_id, Pageable pageable);

	List<Product> findTop4ProductByCategory_id(int name);
}
