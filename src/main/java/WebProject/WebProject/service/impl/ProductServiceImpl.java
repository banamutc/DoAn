package WebProject.WebProject.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import WebProject.WebProject.entity.Product;
import WebProject.WebProject.repository.ProductRepository;
import WebProject.WebProject.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {
 private final int DELETE_STATUS = 2;
	@Autowired
	ProductRepository productRepository;

	@Override
	public List<Product> getAllProduct() {
		// TODO Auto-generated method stub
		return productRepository.findAllProduct();
	}

	@Override
	public Product saveProduct(Product product) {
		// TODO Auto-generated method stub
		return productRepository.save(product);
	}

	@Override
	public Product getProductById(int id) {
		// TODO Auto-generated method stub
		return productRepository.findById(id);
	}

	@Override
	public List<Product> getAllProductInCartByUserId(String id) {
		return productRepository.findAllProductInCartByUserId(Integer.parseInt(id));
	}

	@Override
	public Product updateProduct(Product product) {
		// TODO Auto-generated method stub
		return productRepository.save(product);
	}

	@Override
	public void deleteProductImageById(int id) {
		productRepository.deleteProductImageById(id);
	}

	@Override
	public void deleteProductById(int id) {
		// TODO Auto-generated method stub
		productRepository.updateProductActiveById(id, DELETE_STATUS);
	}

	@Override
	public List<Product> getAll(Pageable pageable) {
		return productRepository.getListProduct(pageable.getPageSize() * pageable.getPageNumber());
	}

	@Override
	public List<Product> findByProductNameContaining(String name) {
		// TODO Auto-generated method stub
		return productRepository.findByProductNameContaining(name);
	}

	@Override
	public List<Product> findTop12ProductBestSellers() {
		// TODO Auto-generated method stub
		return productRepository.findTop12ProductBestSellers();
	}

	@Override
	public List<Product> findTop12ProductNewArrivals() {
		// TODO Auto-generated method stub
		return productRepository.findTop12ProductNewArrivals();
	}

	@Override
	public Page<Product> findAll(Pageable pageable) {
		return productRepository.findAll(pageable);
	}

	@Override
	public Page<Product> findByProductNameAndCategoryIdContaining(String name, int category_id, Pageable pageable) {
		return productRepository.findByProductNameAndCategoryIdContaining(name, category_id, pageable);
	}

	@Override
	public Page<Product> findByProductNameContaining(String name, Pageable pageable) {
		return productRepository.findByProductNameContaining(name, pageable);
	}

	@Override
	public List<Product> findTop4ProductByCategory_id(int id) {
		return productRepository.findTop4ProductByCategory_id(id);
	}
	
}
