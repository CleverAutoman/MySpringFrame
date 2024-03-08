package com.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BeanDefinition {

    private Class clazz;
    private String scope;
    private List<Method> preDestroy = new ArrayList<>();
    private Object target;
    private Method Disposable;

    public Method getDisposable() {
        return Disposable;
    }

    public void setDisposable(Method disposable) {
        Disposable = disposable;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public BeanDefinition() {
    }

    public BeanDefinition(Class clazz, String scope) {
        this.clazz = clazz;
        this.scope = scope;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<Method> getPreDestroy() {
        return preDestroy;
    }

    public void setPreDestroy(List<Method> preDestroy) {
        this.preDestroy = preDestroy;
    }
}
