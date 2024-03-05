package com.spring;

import com.spring.Constant.IfInOperation;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MyApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, Object> singletonObject = new ConcurrentHashMap<>(); // the pool of prototype object
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(); // store all the beanDefinition
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    // processors of post construct
    private List<Method> postConstructProcessorList = new ArrayList<>();

    private Boolean inProgressing = IfInOperation.InOperation;

    public MyApplicationContext(Class configClass) {
        this.configClass = configClass;

        // scan the class -> get the annotation -> check whether it is a singleton or not
        scan(configClass);

        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();

            if (beanDefinition.getScope().equals("singleton")) {
                // create all the beans if they are singletons
                Object bean = createBean(beanDefinition, beanName);
                singletonObject.put(beanName, bean);
            }
        }


        /**
         * TODO 完善持续请求逻辑
         */
        int count = 0;
        while (inProgressing) {
            if (count == 5) {
                inProgressing = IfInOperation.NotInOperation;
            }
            count++;
            System.out.println("Connection, " + count);
        }

        abortProgress();

    }


    private void scan(Class configClass) {
        // 解析配置类
        // 注解 -> 扫描路径
        componentScan componentScanAnnotation  = (componentScan) configClass.getDeclaredAnnotation(componentScan.class);
        //拿到路径
        String path = componentScanAnnotation.value();
//        System.out.println(value); -> com.mine.service
        path = path.replace(".", "/");

        //扫描
        //Bootstrap -> jre/lib
        //Ext -------> jre/lib/ext
        //App -------> classpath
        ClassLoader classLoader = MyApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        // 得到目录 绝对路径
        File file = new File(resource.getFile());

        if (file.isDirectory()) {
            File[] files = file.listFiles();
//            System.out.println(files);
            for (File f: files) {
                String fileName = f.getAbsolutePath();
                if(fileName.endsWith(".class")) {

                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    className = className.replace("\\", ".");

                    try {

                        Class<?> clazz = classLoader.loadClass(className);

                        if (clazz.isAnnotationPresent(component.class)) {

                            // BeanPostProcessor, need to search
                            if(BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                try {
                                    BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
//                                    System.out.println(instance.getClass().getName() + "这是新创建的");
                                    beanPostProcessorList.add(instance);

                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                            }

                            // to verify current class is a bean -> prototype or not -----> BeanDefinition
                                // no lazy loading at that time -> add it later
                            component componentAnnotation = clazz.getDeclaredAnnotation(component.class);
                            String beanName = componentAnnotation.value();

                            // if beanName is void, using the lowercase of class's name
                            if ("".equals(beanName)) {
                                String packageName = clazz.getPackage().getName() + ".";
                                String clazzName = clazz.getName().replace(packageName, "");
                                beanName = clazzName.substring(0, 1).toLowerCase() + clazzName.substring(1);
//                                System.out.println(beanName);
                            }

                            // new a beanDefinition
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);

                            // check it whether has a PostConstructor or PreDestroy
                            Method[] declaredMethods = clazz.getDeclaredMethods();
                            for(Method me: declaredMethods) {
                                if (me .isAnnotationPresent(PostConstruct.class)) {
                                    postConstructProcessorList.add(me);
                                }
                            }

                            /**
                             * preDestroy
                             */
                            try {
                                beanDefinition.setTarget(clazz.getDeclaredConstructor().newInstance());
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            for (Method me : declaredMethods) {
                                if (me.isAnnotationPresent(PreDestroy.class)) {
                                    beanDefinition.getPreDestroy().add(me);
                                }
                            }

                            // check it whether has a scope annotation
                            if(clazz.isAnnotationPresent(scope.class)) {
                                scope scopeAnnotation = clazz.getDeclaredAnnotation(scope.class);

                                String value = scopeAnnotation.value();
                                beanDefinition.setScope(value);
                            } else {
                                beanDefinition.setScope("singleton"); // class is singleton
                            }

                            // store all beanDefinition in the map
                            beanDefinitionMap.put(beanName, beanDefinition);


                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    // create beans according to their definitions
    public Object createBean (BeanDefinition beanDefinition, String beanName) {

        Class clazz = beanDefinition.getClazz();

        // use reflection to create a new bean
        try {
            Object instance= clazz.getDeclaredConstructor().newInstance();

            // 依赖注入
            for (Field declaredField : clazz.getDeclaredFields()) {
                System.out.println(declaredField);
                if(declaredField.isAnnotationPresent(Autowired.class)) {

                    Object bean = getBean(declaredField.getName());

                    if(bean == null) {

                    }
                    declaredField.setAccessible(true); // -》 访问private对象
                    declaredField.set(instance, bean);
                }

                // annotation of value
                if (declaredField.isAnnotationPresent(Value.class)) {
//                    System.out.println(declaredField);
                    Value valueAnnotation = declaredField.getAnnotation(Value.class);
                    if (valueAnnotation != null) {
                        String value = valueAnnotation.value();
//                        System.out.println(value);
                        String name = declaredField.getName();
                        String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
                        System.out.println(methodName);
                        Method method = clazz.getMethod(methodName, declaredField.getType());

                        // 完成数据类型的转换
                        Object val = null;
                        System.out.println(declaredField.getType().getName());
                        switch (declaredField.getType().getName()){
                            case "java.lang.Integer":
                                val = Integer.parseInt(value);
                                break;
                            case "java.lang.String":
                                val = value;
                                break;
                            case "java.lang.float":
                                val = Float.parseFloat(value);
                                break;
                        }
                        method.invoke(instance, value);
                    }
                }

                // annotation of PostConstruct
                // invoke the methods in PostConstructList
                for (Method me: postConstructProcessorList) {
                    me.invoke(instance);
                }
            }

            // 判断是否实现了beanNameAware接口 -> 强转为接口对象 -> aware回调
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // 调用初始化框架时存入的前置方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // Initializing Bean
            if(instance instanceof InitializingBean) {
                try {
                    ((InitializingBean) instance).afterPropertiesSet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // annotation of PostConstruct
            // invoke the methods in PostConstructList
//            for (Method me: preDestoryProcessorList) {
//                me.invoke(instance);
//            }

            // 调用初始化框架时存入的后置方法， 生命周期最后一步
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }




            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Object getBean (String beanName) {
        // hard to achieve how to find the class with beanName
        // also hard to tackle with the repetitions of works ----> using definitionBeanMap
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object o = singletonObject.get(beanName);
                return o;
            } else {
                // the bean is not a singleton, need to create one
                Object bean = createBean(beanDefinition, beanName);
                return bean;
            }
        } else {
            // the bean of such beanName is not existed
            throw new NullPointerException("No Such Bean");
        }
    }

    public void abortProgress () {
        /**
         * 销毁单例池中的所有单例bean
         */
        for (Map.Entry<String, Object> singleton : singletonObject.entrySet()) {
            String key = singleton.getKey();
            Object value = singleton.getValue();
            System.out.println(key);
            System.out.println(value);
            BeanDefinition beanDefinition = beanDefinitionMap.get(key);

            destroyBean(beanDefinition, value);


        }
    }

    public void destroyBean (BeanDefinition beanDefinition, Object object) {
        List<Method> preDestroy = beanDefinition.getPreDestroy();
        if (preDestroy.size() == 0) {
            return;
        }
        Class clazz = beanDefinition.getClazz();
        Object sourceTarget = beanDefinition.getTarget();

        for (Method method : preDestroy) {
            try {
//                System.out.println(method.getDeclaringClass().getName());
                System.out.println(object.getClass().getName());
                method.invoke(sourceTarget);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
