package com.project.JewelryMS.service;

import com.project.JewelryMS.entity.*;
import com.project.JewelryMS.model.Dashboard.*;
import com.project.JewelryMS.model.Dashboard.Customer.*;
import com.project.JewelryMS.model.Order.ProductResponse;
import com.project.JewelryMS.repository.CategoryRepository;
import com.project.JewelryMS.repository.CustomerRepository;
import com.project.JewelryMS.repository.OrderRepositiory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class DashboardService {
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    OrderRepositiory orderRepositiory;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    CustomerService customerService;


    public List<CategoryResponse> RevenueCategory(){
        Optional<List<Category>> optionalCategoryList = Optional.ofNullable(categoryRepository.findAllCategories());
        List<CategoryResponse> categoryResponseList = new ArrayList<>();
        if(optionalCategoryList.isPresent()){
            List<Category> categoryList = optionalCategoryList.get();
            for(Category category: categoryList){
                CategoryResponse categoryResponse = new CategoryResponse();
                categoryResponse.setCategory_Name(category.getName());
                categoryResponse.setCategory_Total(CalculateCategoryTotal(category.getId()));
                categoryResponseList.add(categoryResponse);
            }
        }
//        categoryResponseList.sort(Comparator.comparing(CategoryResponse::getCategory_Total).reversed());
            return categoryResponseList;
    }

    public Float CalculateCategoryTotal(Long Category_ID){
        List<PurchaseOrder> orders = orderRepositiory.findAll();

        // Tính tổng doanh thu cho danh mục
        float totalRevenue = 0.0F;

        for(PurchaseOrder order : orders){
            Set<OrderDetail> orderDetails = order.getOrderDetails();
            for(OrderDetail orderDetail : orderDetails){
                if(Long.valueOf(orderDetail.getProductSell().getCategory().getId()).equals(Category_ID)){
                    float orderDetailTotal = orderDetail.getQuantity() * orderDetail.getProductSell().getCost();
                    totalRevenue += orderDetailTotal;
                }
            }
        }
        return totalRevenue;
    }

    public List<TopSellProductResponse> getTopSellingProducts() {
        List<PurchaseOrder> orders = orderRepositiory.findAll();

        // Sử dụng Map để lưu trữ số lượng và doanh thu bán hàng cho từng sản phẩm
        Map<Long, TopSellProductResponse> productSalesMap = new HashMap<>();

        for (PurchaseOrder order : orders) {
            Set<OrderDetail> orderDetails = order.getOrderDetails();
            for (OrderDetail orderDetail : orderDetails) {
                ProductSell product = orderDetail.getProductSell();
                Long productId = product.getProductID();
                //Return value topSellProductResponse if it finds specific key mapping in productSalesMap otherwise return null
                TopSellProductResponse topSellProductResponse = productSalesMap.getOrDefault(productId, new TopSellProductResponse());
                topSellProductResponse.setProduct_Name(product.getPName());
                topSellProductResponse.setUnitSold(topSellProductResponse.getUnitSold() != null ? topSellProductResponse.getUnitSold() + orderDetail.getQuantity() : orderDetail.getQuantity());
                topSellProductResponse.setRevenueSold(topSellProductResponse.getRevenueSold() != null ? topSellProductResponse.getRevenueSold() + (orderDetail.getQuantity() * product.getCost()) : orderDetail.getQuantity() * product.getCost());
                productSalesMap.put(productId, topSellProductResponse);
            }
        }

        // Chuyển Map sang List và sắp xếp theo số lượng bán ra hoặc doanh thu
        List<TopSellProductResponse> topSellProductResponses = new ArrayList<>(productSalesMap.values());
        topSellProductResponses.sort(Comparator.comparing(TopSellProductResponse::getUnitSold).reversed());
        return topSellProductResponses;
    }


    public List<CategoryResponse> RevenueCategory(RevenueDateRequest revenueDateRequest){
        Optional<List<Category>> optionalCategoryList = Optional.ofNullable(categoryRepository.findAllCategories());
        List<CategoryResponse> categoryResponseList = new ArrayList<>();
        if(optionalCategoryList.isPresent()){
            List<Category> categoryList = optionalCategoryList.get();
            for(Category category: categoryList){
                CategoryResponse categoryResponse = new CategoryResponse();
                categoryResponse.setCategory_Name(category.getName());
                categoryResponse.setCategory_Total(CalculateCategoryTotal(category.getId(), revenueDateRequest));
                categoryResponseList.add(categoryResponse);
            }
        }
//       categoryResponseList.sort(Comparator.comparing(CategoryResponse::getCategory_Total).reversed());
        return categoryResponseList;
    }

    public Float CalculateCategoryTotal(Long Category_ID, RevenueDateRequest revenueDateRequest){
        // Lấy start và end dates từ request và chuyển đổi thành LocalDateTime
        LocalDate startDate = revenueDateRequest.getStartTime();
        LocalDate endDate = revenueDateRequest.getEndTime();

        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999999999

        // Truy vấn các đơn hàng trong khoảng thời gian đã chỉ định
        List<PurchaseOrder> orders = orderRepositiory.findOrdersByDateRange(startDateTime, endDateTime);

        // Tính tổng doanh thu cho danh mục
        float totalRevenue = 0.0F;

        for(PurchaseOrder order : orders){
            Set<OrderDetail> orderDetails = order.getOrderDetails();
            for(OrderDetail orderDetail : orderDetails){
                if(Long.valueOf(orderDetail.getProductSell().getCategory().getId()).equals(Category_ID)){
                    float orderDetailTotal = orderDetail.getQuantity() * orderDetail.getProductSell().getCost();
                    totalRevenue += orderDetailTotal;
                }
            }
        }
        return totalRevenue;
    }

    public List<TopSellProductResponse>  getTopSellingProducts(RevenueDateRequest revenueDateRequest) {
        // Lấy start và end dates từ request và chuyển đổi thành LocalDateTime
        LocalDate startDate = revenueDateRequest.getStartTime();
        LocalDate endDate = revenueDateRequest.getEndTime();

        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999999999

        // Truy vấn các đơn hàng trong khoảng thời gian đã chỉ định
        List<PurchaseOrder> orders = orderRepositiory.findOrdersByDateRange(startDateTime, endDateTime);

        // Sử dụng Map để lưu trữ số lượng và doanh thu bán hàng cho từng sản phẩm
        Map<Long, TopSellProductResponse> productSalesMap = new HashMap<>();

        for (PurchaseOrder order : orders) {
            Set<OrderDetail> orderDetails = order.getOrderDetails();
            for (OrderDetail orderDetail : orderDetails) {
                ProductSell product = orderDetail.getProductSell();
                Long productId = product.getProductID();
                //Return value topSellProductResponse if it finds specific key mapping in productSalesMap otherwise return null
                TopSellProductResponse topSellProductResponse = productSalesMap.getOrDefault(productId, new TopSellProductResponse());
                topSellProductResponse.setProduct_Name(product.getPName());
                topSellProductResponse.setUnitSold(topSellProductResponse.getUnitSold() != null ? topSellProductResponse.getUnitSold() + orderDetail.getQuantity() : orderDetail.getQuantity());
                topSellProductResponse.setRevenueSold(topSellProductResponse.getRevenueSold() != null ? topSellProductResponse.getRevenueSold() + (orderDetail.getQuantity() * product.getCost()) : orderDetail.getQuantity() * product.getCost());
                productSalesMap.put(productId, topSellProductResponse);
            }
        }

        // Chuyển Map sang List và sắp xếp theo số lượng bán ra hoặc doanh thu
        List<TopSellProductResponse> topSellProductResponses = new ArrayList<>(productSalesMap.values());
        topSellProductResponses.sort(Comparator.comparing(TopSellProductResponse::getUnitSold).reversed());
        return topSellProductResponses;
    }

    public CustomerLoyaltyResponse getCustomerLoyaltyStatistics(RevenueDateRequest revenueDateRequest) {
        // Convert LocalDate to LocalDateTime to include the whole day
        LocalDate startDate = revenueDateRequest.getStartTime();
        LocalDate endDate = revenueDateRequest.getEndTime();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Fetch customers created within the date range
        List<Customer> customers = customerRepository.findCustomersByCreateDateRange(startDateTime, endDateTime);

        // Build the list of CustomerLoyalty objects
        List<CustomerLoyalty> customerLoyalties = new ArrayList<>();
        for (Customer customer : customers) {
            CustomerLoyalty customerLoyalty = new CustomerLoyalty();
            customerLoyalty.setEmail(customer.getEmail());
            customerLoyalty.setPhoneNumber(customer.getPhoneNumber());
            customerLoyalty.setPointAmount(customer.getPointAmount());
            customerLoyalty.setRank(customerService.getCustomerRank(customer.getPK_CustomerID()));
            customerLoyalties.add(customerLoyalty);
        }

        CustomerLoyaltyResponse response = new CustomerLoyaltyResponse();
        response.setList(customerLoyalties);
        return response;
    }

    public CustomerDemographicsResponse getCustomerDemoGraphicResponse(RevenueDateRequest revenueDateRequest){
        // Convert LocalDate to LocalDateTime to include the whole day
        LocalDate startDate = revenueDateRequest.getStartTime();
        LocalDate endDate = revenueDateRequest.getEndTime();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Fetch customers created within the date range
        List<Customer> customers = customerRepository.findCustomersByCreateDateRange(startDateTime, endDateTime);
        List<CustomerDemographics> customerDemoGraphics = new ArrayList<>();
        for(Customer customer: customers){
            CustomerDemographics customerDemoGraphic = new CustomerDemographics();
            customerDemoGraphic.setGender(customer.getGender());
            customerDemoGraphics.add(customerDemoGraphic);
        }
        CustomerDemographicsResponse customerDemoGraphicResponse = new CustomerDemographicsResponse();
        customerDemoGraphicResponse.setList(customerDemoGraphics);
        return customerDemoGraphicResponse;
    }

    public CustomerSignUpResponse getCustomerSignUpsByStaff(RevenueDateRequest revenueDateRequest) {
        LocalDate startDate = revenueDateRequest.getStartTime();
        LocalDate endDate = revenueDateRequest.getEndTime();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> results = customerRepository.findCustomerSignUpsByStaff(startDateTime, endDateTime);
        List<CustomerSignUp> customerSignUps = new ArrayList<>();

        for (Object[] result : results) {
            String staffName = (String) result[0];
            Long signUpCount = (Long) result[1];

            CustomerSignUp customerSignUp = new CustomerSignUp();
            customerSignUp.setStaffName(staffName);
            customerSignUp.setNumberSignUp(signUpCount.intValue());
            customerSignUps.add(customerSignUp);
        }

        CustomerSignUpResponse response = new CustomerSignUpResponse();
        response.setList(customerSignUps);
        return response;
    }

}