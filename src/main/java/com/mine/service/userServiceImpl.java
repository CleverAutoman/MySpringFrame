package com.mine.service;

import com.spring.*;

@component("userService")
//@scope("singleton") // -> prototype class
public class userServiceImpl implements BeanNameAware, userService {

    @Autowired
    private orderService orderService;

    private String beanName;

    @Value("Xiaoming")
    private String name;

    @PostConstruct
    public void pre() {
        System.out.println("postConstrcut_method");
    }

    @PreDestroy
    public void post() {
        System.out.println("preDestory_Method");
    }

    public void test() {
        System.out.println(orderService);
    }

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

//    @Override
//    public void afterPropertiesSet() throws Exception {
//        // 验证变量，赋值变量
//    }


    public String getBeanName() {
        return beanName;
    }

//    public userServiceImpl(com.mine.service.orderService orderService, String beanName, String name) {
//        this.orderService = orderService;
//        this.beanName = beanName;
//        this.name = name;
//    }

    public com.mine.service.orderService getOrderService() {
        return orderService;
    }

    public void setOrderService(com.mine.service.orderService orderService) {
        this.orderService = orderService;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "userServiceImpl{" +
                "orderService=" + orderService +
                ", beanName='" + beanName + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
