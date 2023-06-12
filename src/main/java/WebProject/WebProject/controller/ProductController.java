package WebProject.WebProject.controller;

import WebProject.WebProject.entity.Cart;
import WebProject.WebProject.entity.Category;
import WebProject.WebProject.entity.Product;
import WebProject.WebProject.entity.User;
import WebProject.WebProject.repository.ProductRepository;
import WebProject.WebProject.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartService cartService;

    @Autowired
    HttpSession session;

    @Autowired
    CookieService cookie;

    @GetMapping(value = {"", "/home"})
    public String homePage(Model model) throws Exception {
        Cookie userName = cookie.read("user_name");
        Cookie remember = cookie.read("remember");
        String errorMomo = (String) session.getAttribute("error_momo");
        String NoSignIn = (String) session.getAttribute("NoSignIn");
        session.setAttribute("NoSignIn", null);
        session.setAttribute("error_momo", null);
        String a = (String) session.getAttribute("NoSignIn");
        System.out.println(a);
        System.out.println(NoSignIn);
        User acc = (User) session.getAttribute("acc");
        if (remember != null) {
            acc = userService.findByIdAndRole(userName.getValue(), "user");
            session.setAttribute("acc", acc);
            List<Cart> listCart = cartService.getAllCartByUserId(acc.getId());
            session.setAttribute("countCart", listCart.size());
        }
        if (acc != null) {
            List<Cart> listCart = cartService.getAllCartByUserId(acc.getId());
            session.setAttribute("countCart", listCart.size());
        }
        if (session.getAttribute("acc") == null)
            session.setAttribute("countCart", "0");
        model.addAttribute("error_momo", errorMomo);
        model.addAttribute("NoSignIn", NoSignIn);

        List<Product> Top12ProductBestSellers = productService.findTop12ProductBestSellers();
        List<Product> Top12ProductNewArrivals = productService.findTop12ProductNewArrivals();
        model.addAttribute("Top12ProductBestSellers", Top12ProductBestSellers);
        model.addAttribute("Top12ProductNewArrivals", Top12ProductNewArrivals);
        return "index";
    }

    @GetMapping("/shop")
    public String shopPage(Model model, @RequestParam(value = "page", required = false, defaultValue = "1") int page) throws Exception {
        if (page == 0) {
            page = 1;
        }
        List<Product> lp = productService.getAllProduct();
        int TotalPro = lp.size();
        model.addAttribute("TotalPro", TotalPro);
        Pageable pageable = PageRequest.of(page - 1, 12);
        Page<Product> pageProduct = productRepository.findAllPageProduct(pageable);
        int a = pageProduct.getTotalPages();
        model.addAttribute("listProduct", pageProduct);
        List<Category> listCategory = categoryService.findAll();
        String searchInput = (String) session.getAttribute("search_input");
        User user = (User) session.getAttribute("acc");

        if (user != null) {
            model.addAttribute("userName", user.getUserName());
        }
		model.addAttribute("listCategory", listCategory);
        model.addAttribute("id", page);
        model.addAttribute("search_input", searchInput);
        return "shop";
    }

    @GetMapping("/productDetail/{id}")
    public String productDetailId(@PathVariable int id, Model model) {
        Product product = productService.getProductById(id);
        if (product != null) {
            List<Product> relatedProduct = productService.findTop4ProductByCategory_id(product.getCategory().getId());
            model.addAttribute("relatedProduct", relatedProduct);
            model.addAttribute(product);
            return "shop-details";
        } else {
            return "redirect:/home";
        }

    }

    @PostMapping("/search")
    public String search(@ModelAttribute("search-input") String searchInput, Model model) throws Exception {
        session.setAttribute("search_input", searchInput);
        return "redirect:/search/0";
    }

    @GetMapping("/search/{id}")
    public String SearchPage(Model model,@PathVariable int id, @RequestParam(value = "page", required = false, defaultValue = "1") int page) throws Exception {
        if (page == 0) {
            page = 1;
        }
        List<Category> listCategory = categoryService.findAll();
        String searchInput = (String) session.getAttribute("search_input");
        if (searchInput != null) {
        Pageable pageable = PageRequest.of(page-1, 12);
        Page<Product> listProduct = productRepository.findByProductNameContaining(searchInput, pageable);
        List<Product> listProductAll = productRepository.findByProductNameContaining(searchInput);
        int TotalPro = listProductAll.size();
        model.addAttribute("TotalPro", TotalPro);
        model.addAttribute("search_input", searchInput);
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("listCategory", listCategory);
        model.addAttribute("pageSearch", "pageSearch");
        model.addAttribute("noPageable", "search");
        model.addAttribute("page",page);
        model.addAttribute("id", id);
        for (Product y : listProduct) {
            System.out.println(y);
        }
        return "shop";
        } else {
            model.addAttribute("TotalPro", 0);
            model.addAttribute("noPageable", "search");
            model.addAttribute("listCategory", listCategory);
            model.addAttribute("search_input", null);
            model.addAttribute("listProduct", null);
        return "shop";
        }
    }
}
