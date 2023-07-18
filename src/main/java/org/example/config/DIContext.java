package org.example.config;

import org.example.config.annotations.*;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class DIContext {
    private static final Map<Class<?>, List<Object>> container = new HashMap<>();
    private List<Object> beansInstances;
    private Scheduling scheduling;
    private String profile;

    public DIContext() {
        initContainer();
        try {
            profile = ProfileConfig.getProfile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get profile", e);
        }
        beansInstances = container.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        initInjection();
        scheduling = new Scheduling();
        initScheduling();
    }

    private void initScheduling() {
        scheduling.MyScheduler((ArrayList<Object>) beansInstances);
    }

    private void initContainer() {
        Reflections reflections = new Reflections("org.example");
        Set<Class<?>> beans = reflections.getTypesAnnotatedWith(Service.class);

        for (Class<?> bean : beans) {
            try {
                createBeanInstance(bean);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to instantiate bean " + bean.getName(), e);
            }
        }
    }

    private void createBeanInstance(Class<?> bean) throws ReflectiveOperationException {
        Object beanInstance = bean.getConstructor().newInstance();
        addToContainer(bean, beanInstance);

        Class<?>[] interfaces = bean.getInterfaces();
        if (interfaces.length != 0) {
            for (Class<?> interfaze : interfaces) {
                addToContainer(interfaze, beanInstance);
            }
        }
    }

    private void addToContainer(Class<?> type, Object instance) {
        container.computeIfAbsent(type, k -> new ArrayList<>()).add(instance);
    }

    private void initInjection() {
        for (Object bean : beansInstances) {
            try {
                constructorInjection(bean);
                fieldInjection(bean);
                methodInjection(bean);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to inject dependencies into bean " + bean.getClass().getName(), e);
            }
        }
    }

    private void constructorInjection(Object bean) throws ReflectiveOperationException {
        for (Constructor<?> constructor : bean.getClass().getConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                Object[] injectableInstances = getInjectableInstances(constructor.getParameters());
                Object newBean = constructor.newInstance(injectableInstances);
                container.get(bean.getClass()).clear();
                container.get(bean.getClass()).add(newBean);
            }
        }
    }

    private void fieldInjection(Object bean) throws IllegalAccessException {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object injectableInstance = getInjectableInstance(field.getType()).orElseThrow();
                field.setAccessible(true);
                field.set(bean, injectableInstance);
            } else if (field.isAnnotationPresent(Value.class)) {
                String value = field.getAnnotation(Value.class).value();
                field.setAccessible(true);
                field.set(bean, value);
            }
        }
    }

    private void methodInjection(Object bean) throws ReflectiveOperationException {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Inject.class)) {
                Object[] injectableInstances = getInjectableInstances(method.getParameters());
                method.invoke(bean, injectableInstances);
            }
        }
    }

    private Object[] getInjectableInstances(Parameter[] parameters) {
        return Arrays.stream(parameters)
                .map(this::getInjectableInstance)
                .map(Optional::orElseThrow)
                .toArray();
    }

    private Optional<Object> getInjectableInstance(Class<?> type) {
        return Optional.ofNullable(container.get(type))
                .flatMap(this::chooseInjectableInstance);
    }

    private Optional<Object> getInjectableInstance(Parameter parameter) {
        Class<?> parameterClass = parameter.getType();
        Optional<Object> instance = getInjectableInstance(parameterClass);

        if (parameter.isAnnotationPresent(Qualifier.class)) {
            String qualifier = parameter.getAnnotation(Qualifier.class).value();
            instance = instance.filter(bean -> isQualifyingBean(bean, qualifier));
        }

        return instance;
    }

    private Optional<Object> chooseInjectableInstance(List<Object> instances) {
        if (instances.size() == 1) {
            return Optional.of(instances.get(0));
        }

        for (Object instance : instances) {
            if (isProfileBean(instance)) {
                return Optional.of(instance);
            }
        }

        return Optional.empty();
    }

    private boolean isProfileBean(Object bean) {
        Profile profileAnnotation = bean.getClass().getAnnotation(Profile.class);
        return profileAnnotation != null && profileAnnotation.value().equals(profile);
    }

    private boolean isQualifyingBean(Object bean, String qualifier) {
        Service serviceAnnotation = bean.getClass().getAnnotation(Service.class);
        return serviceAnnotation != null && serviceAnnotation.value().equals(qualifier);
    }
}
