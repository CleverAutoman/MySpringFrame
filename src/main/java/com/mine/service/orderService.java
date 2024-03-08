package com.mine.service;

import com.spring.Autowired;
import com.spring.DisposableBean;
import com.spring.component;

@component("orderService")
public class orderService implements DisposableBean {

    @Override
    public void destroy() throws Exception {

    }
}
