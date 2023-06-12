package WebProject.WebProject.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import WebProject.WebProject.entity.Product;

import javax.transaction.Transactional;

/**
 * @author HOAN HAO
 *
 */
@Repository
@Transactional
public interface ProductRepository extends JpaRepository<Product,Long>{
	
	@Query(value="select * from product p where p.product_name like %?1%",nativeQuery = true)
	List<Product> findByProductNameContaining(String name);
	
	@Query(value="Select * From product p where p.is_active = 1 ORDER BY p.quantity ASC LIMIT 12;",nativeQuery = true)
	List<Product> findTop12ProductBestSellers();
	
	@Query(value="Select * From product p where p.is_active = 1 ORDER BY p.quantity  DESC LIMIT 12;;",nativeQuery = true)
	List<Product> findTop12ProductNewArrivals();
	
	Page<Product> findAllByCategory_id(int id, Pageable pageable);

	@Query(value = "select * from product where product.is_active = 1", nativeQuery = true)
	List<Product> findAllProduct();

	@Query(value = "select * from product where product.is_active = 1", nativeQuery = true)
	Page<Product> findAllPageProduct(Pageable pageable);


	@Query(value = "select * from product where product.is_active = 1 and product.id =:id", nativeQuery = true)
	Product findById(int id);
	
	@Query(value="select * from `fashionstore`.product where `fashionstore`.product.product_name like %?1% and `fashionstore`.product.category_id= ?2",nativeQuery = true)
	Page<Product> findByProductNameAndCategoryIdContaining(String name, int category_id, Pageable pageable);
	
	@Query(value="select * from `fashionstore`.product where `fashionstore`.product.product_name like %?1%",nativeQuery = true)
	Page<Product> findByProductNameContaining(String name, Pageable pageable);


	@Query(value="select * from product p where p.category_id = ?1 ORDER BY p.sold DESC LIMIT 4",nativeQuery = true)
	List<Product> findTop4ProductByCategory_id(int id);

	@Query(value = "select * from product p join cart c on p.id=c.product_id join user u on c.user_id = u.id where u.id =: id",nativeQuery = true)
	List<Product> findAllProductInCartByUserId(int id);

	@Modifying
	@Query(value = "delete from product_image pi where pi.product_id =:id", nativeQuery = true)
	void deleteProductImageById(int id);

	@Modifying
	@Query(value = "update product p set p.is_active =:status where p.id =:id", nativeQuery = true)
	void updateProductActiveById(int id, int status);

	@Modifying
	@Query(value = "SELECT * FROM product where product.is_active = 1 limit 9 OFFSET :offset",nativeQuery = true)
	List<Product> getListProduct(int offset);

	@Query(value = "select count(product.id) from product where product.is_active = 1", nativeQuery = true)
	int getTotalProduct();
}
