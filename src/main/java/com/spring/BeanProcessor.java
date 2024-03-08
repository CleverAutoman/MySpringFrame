package com.spring;

public interface BeanProcessor {

    Object processInitialization (Object bean, String beanName);

}
