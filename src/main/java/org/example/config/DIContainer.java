package org.example.config;

import org.example.config.annotations.Inject;
import org.example.config.annotations.Qualifier;
import org.example.config.annotations.Service;
import org.example.config.annotations.Value;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class DIContainer {
    private static final HashMap<Class<?>, ArrayList<Object>> container = new HashMap<>();

    public DIContainer() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        initContainer();
        initInjection();

    }

    private void initContainer() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Reflections reflections = new Reflections("org.example");
        Set<Class<?>> beans = reflections.getTypesAnnotatedWith(Service.class);
        // handling interfaces and their implementations. handling classes without interfaces
        for (Class<?> bean : beans) {
            container.put(bean, new ArrayList<>() {{
                add(bean.getConstructor().newInstance());
            }});
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

    //    @SuppressWarnings("unchecked")
    private void initInjection() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        ArrayList<Object> beansIsntances = container.values().stream()
                .flatMap(ArrayList::stream)
                .collect(Collectors.toCollection(ArrayList::new));
        for (Object bean : beansIsntances) {
            // constructor injection
            constructorInjection(bean);

            // field injections
            fieldInjection(bean);

            // method injection
            methodInjection(bean);
        }

    }

    private void constructorInjection(Object bean) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        for (Constructor<?> constructor : bean.getClass().getConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                Parameter[] params = constructor.getParameters();
                Object[] injectableInstances = new Object[constructor.getParameterCount()];
                for (int i = 0; i < params.length; i++) {
                    injectableInstances[i] = getInjectableInstance(params[i]);
                }
                Object newBean = constructor.newInstance(injectableInstances);
                container.put(bean.getClass(), new ArrayList<>() {{
                    add(newBean);
                }});
            }
        }
    }

    private void fieldInjection(Object bean) throws IllegalAccessException {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();
                Object getFieldTypeInstance = getInjectableInstance(fieldType);
                field.setAccessible(true);
                field.set(bean, getFieldTypeInstance);
            }
            if (field.isAnnotationPresent(Value.class)) {
                Object value = field.getAnnotation(Value.class).value();
                field.setAccessible(true);
                field.set(bean, value);
            }
        }
    }

    private void methodInjection(Object bean) throws IllegalAccessException, InvocationTargetException {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Inject.class)) {
                Parameter[] params = method.getParameters();
                Object[] injectableInstances = new Object[method.getParameterCount()];
                for (int i = 0; i < params.length; i++) {
                    injectableInstances[i] = getInjectableInstance(params[i]);
                }
                method.invoke(bean, injectableInstances);
            }
        }
    }

    private Object getInjectableInstance(Parameter parameter) {
        Object instance = null;
        String qualifier = null;
        Class<?> parameterClass = parameter.getType();

        if (parameter.isAnnotationPresent(Qualifier.class)) {
            qualifier = parameter.getAnnotation(Qualifier.class).value();
            if (container.containsKey(parameterClass)) {
                for (Object bean : container.get(parameterClass)) {
                    if (bean.getClass().getAnnotation(Service.class).value()
                            .equals(qualifier)) {
                        instance = bean;
                    }
                }
            }
        } else {
            if (container.containsKey(parameterClass)) {
                if (container.get(parameterClass).size() > 1) {
                    return new RuntimeException("there are more than 1 implementation for this interface");
                } else {
                    instance = container.get(parameterClass).get(0);
                }
            }
        }
        return instance;
    }
    private Object getInjectableInstance(Class<?> type) {
        Object instance = null;
        String qualifier = null;

        if (type.isAnnotationPresent(Qualifier.class)) {
            qualifier = type.getAnnotation(Qualifier.class).value();
            if (container.containsKey(type)) {
                for (Object bean : container.get(type)) {
                    if (bean.getClass().getAnnotation(Service.class).value()
                            .equals(qualifier)) {
                        instance = bean;
                    }
                }
            }
        } else {
            if (container.containsKey(type)) {
                if (container.get(type).size() > 1) {
                    return new RuntimeException("there are more than 1 implementation for this interface");
                } else {
                    instance = container.get(type).get(0);
                }
            }
        }
        return instance;
    }

}
