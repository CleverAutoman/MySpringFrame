package com.mine.service;

import com.spring.BeanPostProcessor;
import com.spring.component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@component
public class tryBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
//        System.out.println("before");
//
//        if (beanName.equals("userService")) {
//            ((userServiceImpl)bean).setBeanName("test_test");
//        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("after");

        if (beanName.equals("userService")) {

            Object proxyInstance = Proxy.newProxyInstance(tryBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("Proxy");
                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
        }

        return bean;
    }
}
