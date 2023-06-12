package WebProject.WebProject.controller;

import WebProject.WebProject.entity.*;
import WebProject.WebProject.service.CartService;
import WebProject.WebProject.service.OrderService;
import WebProject.WebProject.service.OrderItemService;
import WebProject.WebProject.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import momo.MomoModel;
import momo.ResultMoMo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import utils.Constant;
import utils.Decode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
public class OrderController {
    @Autowired
    CartService cartService;
    @Autowired
    HttpSession session;

    @Autowired
    ProductService productService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    OrderService orderService;

    @GetMapping("/checkout")
    public String checkout(Model model){
        User user = (User) session.getAttribute("acc");
        //Product product = (Product) session.getAttribute("product");
        if (user == null) {
            session.setAttribute("NoSignIn", "Please login before performing the action");
            return "redirect:/home";
        } else {
            List<Cart> cart = cartService.getAllCartByUserId(user.getId());
            if (!cart.isEmpty()) {
                String a = session.getAttribute("Total").toString();
                int Total = Integer.parseInt(a);
                System.out.println(Total);
                model.addAttribute("Total", a);
                @SuppressWarnings("unchecked")
                List<Cart> list = (List<Cart>) session.getAttribute("listCart");
                for(Cart cart1 : list) {
                    Product product = productService.getProductById(cart1.getId());
                    int totalQuantity = product.getQuantity() - cart1.getCount();
                    if(totalQuantity < 0) {
                        session.setAttribute("QuantityIsEmpty","Not enough product quantity!");
                        String b = session.getAttribute("QuantityIsEmpty").toString();
                        model.addAttribute("TotalQuantity", b);
                        return "redirect:/cart";
                    }
                    else {
                        @SuppressWarnings("unchecked")
                        List<Cart> listCart = (List<Cart>) session.getAttribute("listCart");
                        model.addAttribute("listCart", listCart);
                    }
                }
                return "checkout";
            } else {
                session.setAttribute("CartIsEmpty", "CartIsEmpty");
                session.setAttribute("ProductIsEmpty","ProductIsEmpty");
                session.setAttribute("QuantityIsEmpty","QuantityIsEmpty");
                return "redirect:/cart";
            }
        }
    }

    @PostMapping("/checkout")
    public String CheckOut(@ModelAttribute("fullname") String fullname, @ModelAttribute("country") String country,
                           @ModelAttribute("address") String address, @ModelAttribute("phone") String phone,
                           @ModelAttribute("email") String email, @ModelAttribute("note") String note,
                           @RequestParam(value = "payOnDelivery", defaultValue = "false") boolean payOnDelivery,
                           @RequestParam(value = "payWithMomo", defaultValue = "false") boolean payWithMomo, Model model,
                           HttpServletResponse resp) throws Exception {

        long millis = System.currentTimeMillis();
        Date bookingDate = new java.sql.Date(millis);
        @SuppressWarnings("unchecked")
        List<Cart> listCart = (List<Cart>) session.getAttribute("listCart");
        User user = (User) session.getAttribute("acc");
        String a = session.getAttribute("Total").toString();
        int Total = Integer.parseInt(a);
        String status = "Delivering";
        String paymentMethod = null;
        if (payOnDelivery) {
            paymentMethod = "Payment on delivery";
        } else {
            paymentMethod = "Payment with momo";
        }
        Order order = new Order();
        order.setTotal(Total);
        order.setAddress(address);
        order.setBookingDate(bookingDate);
        order.setCountry(country);
        order.setEmail(email);
        order.setFullName(fullname);
        order.setNote(note);
        order.setPaymentMethod(paymentMethod);
        order.setPhone(phone);
        order.setStatus(status);
        order.setUser(user);
        if(paymentMethod == "Payment with momo") {
            session.setAttribute("order", order);
            ObjectMapper mapper = new ObjectMapper();
            int code = (int) Math.floor((Math.random() * 89999999) + 10000000);
            String orderId = Integer.toString(code);
            MomoModel momoModel = new MomoModel();
            momoModel.setPartnerCode(Constant.idMOMO);
            momoModel.setOrderId(orderId);
            momoModel.setStoreId(orderId);
            momoModel.setRequestId(orderId);
            momoModel.setRedirectUrl(Constant.redirectUrl);
            momoModel.setIpnUrl(Constant.ipnUrl);
            momoModel.setPayType(Constant.payType);
            momoModel.setOrderInfo("payment for Male Fashion");
            momoModel.setTranId("1");
            momoModel.setMessage("");
            momoModel.setExtraData("");
            momoModel.setResultCode("200");
            momoModel.setResponseTime("300000");
            momoModel.setOrderType(Constant.orderType);
            momoModel.setRequestType(Constant.requestType);
            momoModel.setAmount(String.valueOf(Total));
            String decode = "accessKey=" + Constant.accessKey + "&amount=" + momoModel.amount + "&extraData="
                    + momoModel.extraData + "&ipnUrl=" + Constant.ipnUrl + "&orderId=" + orderId + "&orderInfo="
                    + momoModel.orderInfo + "&partnerCode=" + momoModel.getPartnerCode() + "&redirectUrl="
                    + Constant.redirectUrl + "&requestId=" + momoModel.getRequestId() + "&requestType="
                    + Constant.requestType;
            String signature = Decode.encode(Constant.secretKey, decode);
            momoModel.setSignature(signature);
            String json = mapper.writeValueAsString(momoModel);
            HttpClient client = HttpClient.newHttpClient();
            ResultMoMo res = new ResultMoMo();
            try {
                HttpRequest request = HttpRequest.newBuilder().uri(new URI(Constant.url))
                        .POST(HttpRequest.BodyPublishers.ofString(json)).headers("Content-Type", "application/json")
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                res = mapper.readValue(response.body(), ResultMoMo.class);
            } catch (InterruptedException | URISyntaxException e) {
                e.printStackTrace();
            }
            if (res == null) {
                session.setAttribute("error_momo", "Payment failed");
                return "redirect:/home";
            } else {
                return "redirect:" + res.payUrl;
            }
        }
        else {
            orderService.saveOrder(order);
            List<Order> listOrder = orderService.getAllOrderByUser_Id(user.getId());
            order = listOrder.get(listOrder.size() - 1);
            for (Cart cart : listCart) {
                Product product = cart.getProduct();
                product.setQuantity(product.getQuantity() - cart.getCount());
                product.setSold(product.getSold() + cart.getCount());
                productService.saveProduct(product);
                OrderItem newOrderItem = new OrderItem();
                newOrderItem.setCount(cart.getCount());
                newOrderItem.setOrder(order);
                newOrderItem.setProduct(cart.getProduct());
                orderItemService.saveOrderItem(newOrderItem);
                cartService.deleteById(cart.getId());
            }
            listOrder = orderService.getAllOrderByUser_Id(user.getId());
            order = listOrder.get(listOrder.size() - 1);
            session.setAttribute("order", order);
            return "redirect:/invoice";
        }
    }
    @GetMapping("/paywithmomo")
    public String payWithMomoGet(@ModelAttribute("message") String message, Model model) {
        if (!message.equals("Successful.")) {
            session.setAttribute("error_momo", "Payment failed!");
            return "redirect:/home";
        } else {
            @SuppressWarnings("unchecked")
            List<Cart> listCart = (List<Cart>) session.getAttribute("listCart");
            User user = (User) session.getAttribute("acc");
            Order newOrder = (Order) session.getAttribute("newOrder");
            orderService.saveOrder(newOrder);
            List<Order> listOrder = orderService.getAllOrderByUser_Id(user.getId());
            newOrder = listOrder.get(listOrder.size() - 1);
            for (Cart y : listCart) {
                Product product = y.getProduct();
                product.setQuantity(product.getQuantity() - y.getCount());
                product.setSold(product.getSold() + y.getCount());
                productService.saveProduct(product);
                OrderItem newOrderItem = new OrderItem();
                newOrderItem.setCount(y.getCount());
                newOrderItem.setOrder(newOrder);
                newOrderItem.setProduct(y.getProduct());
                orderItemService.saveOrderItem(newOrderItem);
                cartService.deleteById(y.getId());
            }
            listOrder = orderService.getAllOrderByUser_Id(user.getId());
            newOrder = listOrder.get(listOrder.size() - 1);
            session.setAttribute("order", newOrder);
            System.out.println(newOrder);
            return "redirect:/invoice";
        }
    }
    @GetMapping("/invoice")
    public String Invoice(Model model) {
        Order order = (Order) session.getAttribute("order");
        String invoiceView = (String) session.getAttribute("invoiceView");
        session.setAttribute("invoiceView", null);
        List<OrderItem> listOrderItem = orderItemService.getAllByOrder_Id(order.getId());
        model.addAttribute("invoiceView", invoiceView);
        model.addAttribute("listOrderItem", listOrderItem);
        model.addAttribute("order", order);
        return "invoice";
    }

    @GetMapping("/invoice/{id}")
    public String invoiceView(@PathVariable int id, Model model, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        model.addAttribute("referer", referer);
        Order order = orderService.findById(id);
        session.setAttribute("order", order);
        session.setAttribute("invoiceView", "view");
        return "redirect:/invoice";
    }

    @GetMapping("/myhistory")
    public String myHistory(Model model, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        User user = (User) session.getAttribute("acc");
        if (user == null) {
            return "redirect:" + referer;
        } else {
            List<Order> listOrder = orderService.getAllOrderByUser_Id(user.getId());
            Collections.reverse(listOrder);
            model.addAttribute("listOrder", listOrder);
            System.out.println(listOrder);
            for (Order y : listOrder) {
                System.out.println(y.getOrderItem());
            }
        }
        return "myhistory";
    }

}
