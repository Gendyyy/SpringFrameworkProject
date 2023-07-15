package org.example.config;

import org.example.config.annotations.Inject;
import org.example.config.annotations.Qualifier;
import org.example.config.annotations.Service;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

//import static sun.reflect.misc.ConstructorUtil.getConstructor;

public class DIContainer {
    private static final HashMap<Class<?>, ArrayList<Object>> container = new HashMap<>();

    private void initContainer() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections("com.example");
        Set<Class<?>> beans = reflections.getTypesAnnotatedWith(Service.class);
        // handling interfaces and their implementations. handling classes without interfaces
        for (Class<?> bean : beans) {
            if (bean.getInterfaces().length != 0) {
                for (Class<?> interfaze : bean.getInterfaces()) {
                    if (container.get(interfaze) == null) {
                        container.put(interfaze, new ArrayList<>() {{
                            add(bean.getConstructor().newInstance());
                        }});
                    } else {
                        container
                                .get(interfaze)
                                .add(bean.getConstructor().newInstance());
                    }
                }
            } else {
                container.put(bean, new ArrayList<>() {{
                    add(bean.getConstructor().newInstance());
                }});
            }
        }
    }
        private void initInjection() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
            ArrayList<Object> beansIsntances = container.values().stream()
                    .flatMap(ArrayList::stream)
                    .collect(Collectors.toCollection(ArrayList::new));
        for (Object bean : beansIsntances){
            // field injection
            for (Field field : bean.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> fieldType = field.getType();
                    Object getFieldTypeInstance = getInjectableInstance(fieldType);
                    field.setAccessible(true);
                    field.set(bean, getFieldTypeInstance);
                }
            }
            // constructor injection
            for (Constructor<?> constructor : bean.getClass().getConstructors()) {
                if (constructor.isAnnotationPresent(Inject.class)) {
                    Class<?>[] params = constructor.getParameterTypes();
                    Object[] injectableInstances = new Object[constructor.getParameterCount()];
                    for(int i  = 0; i < params.length ; i ++){
                        injectableInstances[i] = getInjectableInstance(params[i]);
                    }
                        constructor.newInstance(injectableInstances);
                }
            }
            // method injection
            for (Method method : bean.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Inject.class)) {
                    Class<?>[] params = method.getParameterTypes();
                    Object[] injectableInstances = new Object[method.getParameterCount()];
                    for(int i  = 0; i < params.length ; i ++){
                        injectableInstances[i] = getInjectableInstance(params[i]);
                    }
                        method.invoke(injectableInstances);
                }
            }
        }

    }

    private Object getInjectableInstance(Class<?> fieldType) {
        Object instance = null;
        String qualifier = null;

        if (fieldType.isAnnotationPresent(Qualifier.class)) {
            qualifier = fieldType.getAnnotation(Qualifier.class).value();
            if (container.containsKey(fieldType)){
                for (Object bean : container.get(fieldType)){
                    if (bean.getClass().getAnnotation(Service.class).name()
                            .equals(qualifier)){
                        instance = bean;
                    }
                }
            }
        }
        else {
            if (container.containsKey(fieldType)){
                if (container.get(fieldType).size() > 1){
                    return new RuntimeException("there are more than 1 implementation for this interface");
                }
                else {
                    instance = container.get(fieldType).get(0);
                }
            }
        }
        return instance;
    }

}
