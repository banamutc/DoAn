package WebProject.WebProject.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.filters.AddDefaultCharsetFilter;
import org.apache.coyote.http11.Http11AprProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import WebProject.WebProject.entity.Cart;
import WebProject.WebProject.entity.Product;
import WebProject.WebProject.entity.ProductImage;
import WebProject.WebProject.entity.User;
import WebProject.WebProject.service.CartService;
import WebProject.WebProject.service.ProductService;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @Autowired
    ProductService productService;

    @Autowired
    HttpSession session;

    @GetMapping("/cart")
    public String CartView(Model model) throws Exception {
        User user = (User) session.getAttribute("acc");
        if (user == null) {
            session.setAttribute("NoSignIn", "Please login before performing the action");
            return "redirect:/home";
        } else {
            List<Cart> listCart = cartService.getAllCartByUserId(user.getId());
            if (listCart != null) {
                int Total = 0;
                for (Cart cart : listCart) {
                    Product product = productService.getProductById(cart.getId());
                    int totalInventory = cart.getProduct().getQuantity() - cart.getCount();
                    if(totalInventory < 0) {
                        session.setAttribute("QuantityIsEmpty","Not enough product quantity!");
                        String b = session.getAttribute("QuantityIsEmpty").toString();
                        model.addAttribute("TotalInventory", b);
                    }
                    else {
                        @SuppressWarnings("unchecked")
                        List<Cart> listCarts = (List<Cart>) session.getAttribute("listCart");
                        model.addAttribute("listCart", listCarts);
                        model.addAttribute("TotalInventory", totalInventory);
                        session.setAttribute("TotalInventory", totalInventory);
                    }
                }
                for (Cart y : listCart) {
                    Total = Total + y.getCount() * y.getProduct().getPrice();
                }
                model.addAttribute("Total", Total);
                model.addAttribute("listCart", listCart);
                session.setAttribute("listCart", listCart);
                session.setAttribute("Total", Total);
                return "shopping-cart";
            }
            else {
                return "redirect:/home";
            }
        }
    }

    @GetMapping("/deleteCart/{id}")
    public String GetDeleteCart(@PathVariable int id, Model model, HttpServletRequest request) throws Exception {
        String referer = request.getHeader("Referer");
        User user = (User) session.getAttribute("acc");
        if (user == null) {
            return "redirect:" + referer;
        } else {
            System.out.println(id);
            cartService.deleteById(id);
            session.setAttribute("CartDelete", id);
            List<Cart> listCart = cartService.getAllCartByUserId(user.getId());
            session.setAttribute("countCart", listCart.size());
            return "redirect:/cart";
        }
    }

    @PostMapping("/updateCart")
    public String UpdateCart(HttpServletRequest request, Model model) throws Exception {
        @SuppressWarnings("unchecked")
        List<Cart> listCart = (List<Cart>) session.getAttribute("listCart");
        int i = 0;
        for (Cart o : listCart) {
            String a = request.getParameter("count" + i);
            int count = Integer.parseInt(a);
            System.out.println(count);
            o.setCount(count);
            i++;
        }
        for (Cart o : listCart) {
            cartService.saveCart(o);
        }
        return "redirect:/cart";
    }

    @GetMapping("/addToCart/{id}")
    public String AddToCart(@PathVariable int id, Model model, HttpServletRequest request) throws Exception {
        String referer = request.getHeader("Referer");
        User user = (User) session.getAttribute("acc");
        if (user == null) {
            session.setAttribute("AddToCartErr", "Please login before performing the action");
            return "redirect:" + referer;
        } else {
            List<Cart> listCart = cartService.getAllCartByUserId(user.getId());
            Product product = productService.getProductById(id);
            int cart = 0;
            for (Cart y : listCart) {
                if (y.getProduct().getId() == id) {
                    cart++;
                }
            }
            if (cart > 0) {
                for (Cart y : listCart) {
                    if (y.getProduct().getId() == id) {
                        y.setCount(y.getCount() + 1);
                        cartService.saveCart(y);
                    }
                }
            } else {
                Cart newCart = new Cart();
                newCart.setCount(1);
                newCart.setProduct(product);
                newCart.setUser(user);
                cartService.saveCart(newCart);
            }
            listCart = cartService.getAllCartByUserId(user.getId());
            session.setAttribute("countCart", listCart.size());
            return "redirect:" + referer;
        }
    }

    @PostMapping("/addToCart")
    public String AddToCartPost(@ModelAttribute("product_id") int productId, @ModelAttribute("count") String a,
                                Model model, HttpServletRequest request) throws Exception {
        int count = Integer.parseInt(a);
        String referer = request.getHeader("Referer");
        User user = (User) session.getAttribute("acc");
        if (user == null) {
            session.setAttribute("AddToCartErr", "Please login before performing the action");
            return "redirect:" + referer;
        } else {
            List<Cart> listCart = cartService.getAllCartByUserId(user.getId());
            Product product = productService.getProductById(productId);
            int cart = 0;
            for (Cart y : listCart) {
                if (y.getProduct().getId() == productId) {
                    y.setCount(y.getCount() + count);
                    cartService.saveCart(y);
                    cart++;
                }
            }
            if (cart == 0) {
                Cart newCart = new Cart();
                newCart.setCount(count);
                newCart.setProduct(product);
                newCart.setUser(user);
                cartService.saveCart(newCart);
            }
            listCart = cartService.getAllCartByUserId(user.getId());
            session.setAttribute("countCart", listCart.size());
            return "redirect:" + referer;
        }

    }

}