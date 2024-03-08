package com.mine;

import com.mine.service.userService;
import com.spring.MyApplicationContext;

public class TEST {


    public static void main(String[] args) throws ClassNotFoundException{
        MyApplicationContext myApplicationContext = new MyApplicationContext(AppConfig.class);

        userService userService = (userService)myApplicationContext.getBean("userService");
        System.out.println(userService.toString());
    }
}
