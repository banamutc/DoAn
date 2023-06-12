package WebProject.WebProject.controller;

import WebProject.WebProject.entity.*;
import WebProject.WebProject.repository.ProductImageRepository;
import WebProject.WebProject.repository.ProductRepository;
import WebProject.WebProject.repository.UserRepository;
import WebProject.WebProject.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
public class AdminController {

    @Autowired
    OrderService orderService;

    @Autowired
    UserService userService;

    @Autowired
    ProductService productService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    MailService mailService;

    @Autowired
    HttpSession session;

    @Autowired
    ProductImageService productImageService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductImageRepository productImageRepository;

    @GetMapping("/signin-admin")
    public String SignInAdminView(Model model) {
        String err_sign_admin = (String) session.getAttribute("err_sign_admin");
        model.addAttribute("err_sign_admin", err_sign_admin);
        session.setAttribute("err_sign_admin", null);
        return "signin-admin";
    }

    @PostMapping("/signin-admin")
    public String SignInAdminHandel(@ModelAttribute("login-name") String login_name,
                                    @ModelAttribute("pass") String pass, Model model) throws Exception {
        User admin = userService.findByIdAndRole(login_name, "admin");
        if (admin == null) {
            session.setAttribute("err_sign_admin", "UserName or Password is not correct!");
            return "redirect:/signin-admin";
        } else {
            String decodedValue = new String(Base64.getDecoder().decode(admin.getPassword()));
            if (!decodedValue.equals(pass)) {
                session.setAttribute("err_sign_admin", "UserName or Password is not correct!");
                return "redirect:/signin-admin";
            } else {
                session.setAttribute("admin", admin);
                return "redirect:/dashboard";
            }
        }

    }

    @GetMapping("/logout-admin")
    public String LogOutAdmin(Model model) {
        session.setAttribute("admin", null);
        return "redirect:/signin-admin";
    }

    @GetMapping("/dashboard")
    public String dashboardView(Model model) {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            List<Order> listOrder = orderService.findAll();
            List<Product> listProduct = productService.getAllProduct();
            List<User> listUser = userService.getAllUser();
            List<Category> listCategory = categoryService.findAll();
            List<Order> recentOrders = orderService.findTop5RecentOrder();
            List<String> recentUser = orderService.findTop5RecentCustomer();
            List<User> recentCustomer = new ArrayList<>();
            for (String y : recentUser) {
                recentCustomer.add(userService.findByIdAndRole(y, "user"));
            }
            model.addAttribute("TotalOrder", listOrder.size());
            model.addAttribute("TotalProduct", listProduct.size());
            model.addAttribute("TotalUser", listUser.size());
            model.addAttribute("TotalCategory", listCategory.size());
            model.addAttribute("recentOrders", recentOrders);
            model.addAttribute("recentCustomer", recentCustomer);
            return "dashboard";
        }
    }
    @GetMapping("/dashboard-myprofile")
    public String dashboardMyProfileAdmin(Model model, HttpServletRequest request) {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            String referer = request.getHeader("Referer");
            String messageChangeProfile = (String) session.getAttribute("messageChangeProfile");
            model.addAttribute("messageChangeProfile", messageChangeProfile);
            session.setAttribute("messageChangeProfile", null);
            String ChangePassSuccess = (String) session.getAttribute("ChangePassSuccess");
            model.addAttribute("ChangePassSuccess", ChangePassSuccess);
            session.setAttribute("ChangePassSuccess", null);
            model.addAttribute("admin", admin);
            return "dashboard-my-profile";
        }
    }
    @PostMapping("/dashboard-myprofile/changepassword")
    public String changePassword(Model model, @ModelAttribute("current_password") String current_password,
                                 @ModelAttribute("new_password") String new_password,
                                 @ModelAttribute("confirm_password") String confirm_password, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        User admin = (User) session.getAttribute("admin");
        String decodedValue = new String(Base64.getDecoder().decode(admin.getPassword()));
        if (!decodedValue.equals(current_password)) {
            session.setAttribute("error_change_pass", "Current Password not correct!");
            return "redirect:/dashboard-my-profile";
        } else {
            if (!new_password.equals(confirm_password)) {
                session.setAttribute("error_change_pass", "Confirm New Password not valid!");
                return "redirect:/dashboard-my-profile";
            } else {
                String encodedValue = Base64.getEncoder().encodeToString(new_password.getBytes());
                admin.setPassword(encodedValue);
                userService.saveUser(admin);
                session.setAttribute("admin", admin);
            }
        }
        session.setAttribute("ChangePassSuccess", "ChangePassSuccess");
        return "redirect:" + referer;
    }

    @PostMapping("/dashboard-myprofile/changeProfile")
    public String changeProfile(Model model, @ModelAttribute("avatar") MultipartFile avatar,
                                @ModelAttribute("fullname") String fullName, @ModelAttribute("phone") String phone,
                                @ModelAttribute("email") String email) throws IOException {
        User admin = (User) session.getAttribute("admin");
        if (admin != null) {
            if (!avatar.isEmpty()) {
                String url = cloudinaryService.uploadFile(avatar);
                admin.setAvatar(url);
            }
            admin.setUserName(fullName);
            admin.setEmail(email);
            admin.setPhoneNumber(phone);
            userService.saveUser(admin);
            session.setAttribute("admin", admin);
            session.setAttribute("messageChangeProfile", "Change Success.");
            return "redirect:/dashboard-my-profile";
        } else {
            return "redirect:/signin-admin";
        }
    }
    @GetMapping("/dashboard-myusers")
    public String dashboardMyUserPageView( @RequestParam(value = "page", required = false, defaultValue = "1") int page, Model model) {
        if(page == 0) {
            page = 1;
        }
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            Pageable pageable = PageRequest.of(page-1, 9);
            List<User> pageUser = userService.getAll(pageable);
            int totalPage;
            if(userRepository.getTotalUser() % 9 == 0) {
                totalPage = userRepository.getTotalUser() / 9;
            }
            else {
                totalPage = userRepository.getTotalUser() / 9 + 1;
            }
            model.addAttribute("totalPage",totalPage);
            model.addAttribute("pageUser", pageUser);
            model.addAttribute("id",page);
            return "dashboard-myusers";
        }
    }
    @GetMapping("/dashboard-myusers/delete")
    public String deleteUser(@RequestParam(value = "user", required = false) String id, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        userService.deleteUserById(id);
        return "redirect:" + referer;
    }
    @PostMapping("/dashboard-myusers/search")
    public String dashboardSearchUserPage(@ModelAttribute("search-input") String search_input
            ,@RequestParam(value = "page", required = false, defaultValue = "1") int page, Model model) {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            if (page == 0) {
                page = 1;
            }
            Pageable pageable = PageRequest.of(0, 3);
            Page<User> pageUser = userService.findByUserNameContaining(search_input, pageable);
            model.addAttribute("pageUser", pageUser);
            model.addAttribute("search_dashboard", "search_dashboard");
            model.addAttribute("search_input", search_input);
            session.setAttribute("search_input_dashboard", search_input);
            return "dashboard-myusers";
        }
    }
    @GetMapping("/dashboard-myproducts")
    public String dashboardMyProductPageView( @RequestParam(value = "page", required = false, defaultValue = "1") int page, Model model) {
        if(page == 0) {
            page = 1;
        }
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            List<Category> listCategories = categoryService.findAll();

            Pageable pageable = PageRequest.of(page-1, 9);
            List<Product> pageProduct = productService.getAll(pageable);
            int totalPage;
            if(productRepository.getTotalProduct() % 9 == 0) {
                totalPage = productRepository.getTotalProduct() / 9;
            }
            else {
                totalPage = productRepository.getTotalProduct() / 9 + 1;
            }
            model.addAttribute("totalPage",totalPage);
            model.addAttribute("pageProduct", pageProduct);
            model.addAttribute("id",page);
            model.addAttribute("listCategories", listCategories);
            return "dashboard-myproducts";
        }
    }

    @PostMapping("/dashboard-myproduct/search")
    public String dashboardSearchProductPage(@ModelAttribute("search-input") String search_input,@ModelAttribute("category-selected") int categorySelected
            ,@RequestParam(value = "page", required = false, defaultValue = "1") int page, Model model) {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            if (page == 0) {
                page = 1;
            }
            Page<Product> pageProduct = null;
            Pageable pageable = PageRequest.of(0, 3);
            if (categorySelected > 0) {
                pageProduct = productService.findByProductNameAndCategoryIdContaining(search_input, categorySelected,
                        pageable);
            } else {
                pageProduct = productService.findByProductNameContaining(search_input, pageable);
            }
            List<Category> listCategories = categoryService.findAll();
            String nameCategory = null;
            if (categorySelected == 0) {
                nameCategory = null;
            } else {
                for (Category y : listCategories) {
                    if (y.getId() == categorySelected) {
                        nameCategory = y.getCategoryName();
                    }
                }
            }
            System.out.println(nameCategory);
            model.addAttribute("pageProduct", pageProduct);
            model.addAttribute("listCategories", listCategories);
            model.addAttribute("search_dashboard", "search_dashboard");
            model.addAttribute("search_input", search_input);
            model.addAttribute("nameCategory", nameCategory);
            session.setAttribute("search_input_dashboard", search_input);
            session.setAttribute("category_selected", categorySelected);
            return "dashboard-myproducts";
        }
    }
    @GetMapping("/dashboard-myproduct/search/{page}")
    public String dashboardMyproductSearchPage(@PathVariable int page, Model model) {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            String search_input = (String) session.getAttribute("search_input_dashboard");
            int categorySelected = (int) session.getAttribute("category_selected");
            Page<Product> pageProduct = null;
            Pageable pageable = PageRequest.of(page, 3);
            if (categorySelected > 0) {
                pageProduct = productService.findByProductNameAndCategoryIdContaining(search_input, categorySelected,
                        pageable);
            } else {
                pageProduct = productService.findByProductNameContaining(search_input, pageable);
            }
            List<Category> listCategories = categoryService.findAll();
            model.addAttribute("pageProduct", pageProduct);
            model.addAttribute("listCategories", listCategories);
            model.addAttribute("search_dashboard", "search_dashboard");
            model.addAttribute("search_input", search_input);
            model.addAttribute("category_selected", categorySelected);
            session.setAttribute("search_input_dashboard", search_input);
            return "dashboard-myproducts";
        }
    }

    @GetMapping("dashboard-addproduct")
    public String dashboardAddProductView(Model model) {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            String addProduct = (String) session.getAttribute("addProduct");
            model.addAttribute("addProduct", addProduct);
            session.setAttribute("addProduct", null);
            List<Category> listCategories = categoryService.findAll();
            model.addAttribute("listCategories", listCategories);
            return "dashboard-addproduct";
        }
    }

    @PostMapping("dashboard-addproduct")
    public String dashboardAddProductHandel(Model model, @ModelAttribute("product_name") String productName,
                                            @ModelAttribute("price") String price, @ModelAttribute("availability") String availability,
                                            @ModelAttribute("category") int category, @ModelAttribute("description") String description,
                                            @RequestParam("listImage") MultipartFile[] listImage) throws Exception {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
           if(listImage[0].getOriginalFilename() != null && !listImage[0].getOriginalFilename().isEmpty() ) {
               long millis = System.currentTimeMillis();
               Date createdAt = new java.sql.Date(millis);
               Category cate = categoryService.getCategoryById(category);
               Product product = new Product();
               product.setProductName(productName);
               product.setPrice(Integer.parseInt(price));
               product.setQuantity(Integer.parseInt(availability));
               product.setSold(0);
               product.setCreated_At(createdAt);
               product.setIsActive(1);
               product.setIsSelling(1);
               product.setCategory(cate);
               product.setDescription(description);
               productService.saveProduct(product);
               List<Product> list = productService.getAllProduct();
               product = list.get(list.size() - 1);
               for (MultipartFile y : listImage) {
                   String urlImg = cloudinaryService.uploadFile(y);
                   ProductImage img = new ProductImage();
                   img.setProduct(product);
                   img.setUrlImage(urlImg);
                   productImageService.save(img);
               }
               model.addAttribute("addProduct","AddSuccessProduct");
               return "redirect:/dashboard-myproducts";
           }
           else {
               return "redirect:/dashboard-addproduct";
           }
        }
    }
    @GetMapping("dashboard-myproducts/edit/{id}")
    public String dashboardEditProductView(Model model, @PathVariable int id) {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            List<Category> listCategories = categoryService.findAll();
            Product product = productService.getProductById(id);
            model.addAttribute("product", product);
            model.addAttribute("listCategories", listCategories);
            String editProduct = (String) session.getAttribute("editProduct");
            model.addAttribute("editProduct", editProduct);
            session.setAttribute("editProduct", null);
            return "dashboard-myproducts-edit";
        }

    }
    @PostMapping("dashboard-myproducts/edit")
    public String editProduct(Model model, @ModelAttribute("productId") int id, @ModelAttribute("productName") String productName,
                              @ModelAttribute("price") String price, @ModelAttribute("availability") String availability,
                              @ModelAttribute("category") int category, @ModelAttribute("description") String description,
                              @RequestParam("listImage") MultipartFile[] listImage) throws Exception {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            if(listImage != null ) {
                Category cate = categoryService.getCategoryById(category);
                Product product = productService.getProductById(id);
                product.setProductName(productName);
                product.setPrice(Integer.parseInt(price));
                product.setQuantity(Integer.parseInt(availability));
                product.setCategory(cate);
                product.setDescription(description);
                productService.saveProduct(product);
                for (MultipartFile y : listImage) {
                    if (!y.isEmpty()) {
                        String urlImg = cloudinaryService.uploadFile(y);
                        ProductImage img = new ProductImage();
                        img.setProduct(product);
                        img.setUrlImage(urlImg);
                        productImageService.save(img);
                    }
                }
                session.setAttribute("editProduct", "editProductSuccess");
                return "redirect:/dashboard-myproducts/edit/" + id;
            }
            else {
                return "redirect:/dashboard-myproducts/edit/" + id;

            }
        }
    }

    @GetMapping("/dashboard-myproducts/delete-product/{id}")
    public String deleteProduct(@PathVariable int id, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
//        productService.deleteProductImageById(id);
        productService.deleteProductById(id);
        return "redirect:"+referer;
    }
    @GetMapping("/dashboard-myproducts/delete-image/{id}")
    public String DeleteImage(@PathVariable int id, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        productImageService.deleteById(id);
        return "redirect:"+referer;
    }
    @GetMapping("/dashboard-orders")
    public String dashboardOrderView( @RequestParam(value = "page", required = false, defaultValue = "1") int page, Model model) {
        if(page == 0) {
            page = 1;
        }
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            List<Order> listOrders = orderService.findAll();
            Pageable pageable = PageRequest.of(0, 5);
            Page<Order> pageOrder = orderService.findAll(pageable);
            model.addAttribute("pageOrder", pageOrder);
            model.addAttribute("id",page);
            model.addAttribute("listOrders", listOrders);
            return "dashboard-orders";
        }
    }

    @GetMapping("/dashboard-invoice")
    public String Invoice(Model model) {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            Order order = (Order) session.getAttribute("order");
            String invoiceView = (String) session.getAttribute("invoiceView");
            session.setAttribute("invoiceView", null);
            List<OrderItem> listOrderItem = orderItemService.getAllByOrder_Id(order.getId());
            model.addAttribute("invoiceView", invoiceView);
            model.addAttribute("listOrderItem", listOrderItem);
            model.addAttribute("order", order);
            return "dashboard-invoice";
        }
    }

    @GetMapping("/dashboard-invoice/{id}")
    public String invoiceView(@PathVariable int id, Model model, HttpServletRequest request) {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            String referer = request.getHeader("Referer");
            model.addAttribute("referer", referer);
            Order order = orderService.findById(id);
            session.setAttribute("order", order);
            session.setAttribute("invoiceView", "view");
            return "redirect:/dashboard-invoice";
        }
    }
    @GetMapping("/dashboard-orders/edit")
    public String deleteUser(@RequestParam(value = "order", required = false) int id, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        orderService.editOrderById(id);
        return "redirect:" + referer;
    }
    @GetMapping("dashboard-my-profile")
//    @RequestMapping(value = "dashboard-my-profile", method = RequestMethod.GET)
    public String dashboardMyProfile(Model model,HttpServletRequest request) {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            String error_change_pass = (String) session.getAttribute("error_change_pass");
            String ChangePassSuccess = (String) session.getAttribute("ChangePassSuccess");
            String messageChangeProfile = (String) session.getAttribute("messageChangeProfile");
            model.addAttribute("messageChangeProfile", messageChangeProfile);
            model.addAttribute("error_change_pass", error_change_pass);
            model.addAttribute("ChangePassSuccess", ChangePassSuccess);
            session.setAttribute("error_change_pass", null);
            session.setAttribute("ChangePassSuccess", null);
            session.setAttribute("messageChangeProfile", null);
            model.addAttribute("admin", admin);
            return "dashboard-my-profile";
        }
    }

    @PostMapping("/dashboard-my-profile/changepassword")
    public String dashboardChangePassword(Model model, @ModelAttribute("current_password") String currentPassword,
                                          @ModelAttribute("new_password") String new_password,
                                          @ModelAttribute("confirm_password") String confirmPassword, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            String decodedValue = new String(Base64.getDecoder().decode(admin.getPassword()));
            if (!decodedValue.equals(currentPassword)) {
                session.setAttribute("error_change_pass", "Current Password not correct!");
                return "redirect:/dashboard-my-profile";
            } else {

                if (!new_password.equals(confirmPassword)) {
                    session.setAttribute("error_change_pass", "Confirm New Password not valid!");
                    return "redirect:/dashboard-my-profile";
                } else {
                    String encodedValue = Base64.getEncoder().encodeToString(new_password.getBytes());
                    admin.setPassword(encodedValue);
                    userService.saveUser(admin);
                    session.setAttribute("admin", admin);
                }
            }
            session.setAttribute("ChangePassSuccess", "ChangePassSuccess");
            return "redirect:" + referer;
        }
    }

//    @PostMapping("/dashboard-my-profile/changeProfile")
//    public String changeProfile(Model model, @ModelAttribute("avatar") MultipartFile avatar,
//                                @ModelAttribute("fullname") String fullname, @ModelAttribute("phone") String phone,
//                                @ModelAttribute("email") String email) throws IOException {
//        User admin = (User) session.getAttribute("admin");
//        if (admin == null) {
//            return "redirect:/signin-admin";
//        } else {
//            if (!avatar.isEmpty()) {
//                String url = cloudinaryService.uploadFile(avatar);
//                admin.setAvatar(url);
//            }
//            admin.setUserName(fullname);
//            admin.setEmail(email);
//            admin.setPhoneNumber(phone);
//            userService.saveUser(admin);
//            session.setAttribute("admin", admin);
//            session.setAttribute("messageChangeProfile", "Change Success.");
//            return "redirect:/dashboard-my-profile";
//        }
//    }
    @GetMapping("dashboard-wallet")
    public String dashboardWalletView(Model model) {
        User admin = (User) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/signin-admin";
        } else {
            List<Order> listOrder = orderService.findAll();
            List<Order> listPaymentWithMomo = orderService.findAllByPaymentMethod("Payment with momo");
            List<Order> listPaymentOnDelivery = orderService.findAllByPaymentMethod("Payment on delivery");
            int TotalMomo = 0;
            int TotalDelivery = 0;
            for (Order y : listPaymentWithMomo) {
                TotalMomo = TotalMomo + y.getTotal();
            }
            for (Order y : listPaymentOnDelivery) {
                TotalDelivery = TotalDelivery + y.getTotal();
            }
            List<Order> listRecentMomo = orderService.findTop5OrderByPaymentMethod("Payment with momo");
            List<Order> listRecentDelivery = orderService.findTop5OrderByPaymentMethod("Payment on delivery");

            model.addAttribute("TotalMomo", TotalMomo);
            model.addAttribute("TotalDelivery", TotalDelivery);
            model.addAttribute("TotalOrder", listOrder.size());
            model.addAttribute("listRecentDelivery", listRecentDelivery);
            model.addAttribute("listRecentMomo", listRecentMomo);
            return "dashboard-wallet";
        }
    }

}
