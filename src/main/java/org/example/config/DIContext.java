package org.example.config;

import org.example.config.annotations.Inject;
import org.example.config.annotations.Qualifier;
import org.example.config.annotations.Service;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

public class DIContext {
    private HashMap<Class<?>, Object> beans;
    private Set<Field> injectFields;
    private Set<Method> injectMethods;

    private void initBeans() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections("com.example");
        Set<Class<?>> serviceAnnotation = reflections.getTypesAnnotatedWith(Service.class);
        for(Class<?> type: serviceAnnotation){
            beans.put(type, type.getConstructor().newInstance());
        }

        for(Class<?> key: beans.keySet() ){
            for(Field field : key.getDeclaredFields()){
                if (field.isAnnotationPresent(Inject.class)){
                    injectFields.add(field);
                }

            }
        }
    }
//    private void initInject(){
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .setUrls(ClasspathHelper.forPackage("com.example"))
//                .setScanners(new FieldAnnotationsScanner(), new MethodAnnotationsScanner()));
//        injectFields = reflections.getFieldsAnnotatedWith(Inject.class);
//        injectMethods = reflections.getMethodsAnnotatedWith(Inject.class);
//
//    }
    private void initQualifier(){
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("com.example"))
                .setScanners(new FieldAnnotationsScanner(), new MethodAnnotationsScanner()));
        injectFields = reflections.getFieldsAnnotatedWith(Qualifier.class);
        injectMethods = reflections.getMethodsAnnotatedWith(Qualifier.class);
    }

    private void run() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        initBeans();
//        initInject();
        for(Field field : injectFields){
            Class<?> clazz = field.getDeclaringClass();
            beans.get(clazz);
            if(beans.get(field.getType()) != null &&
                    clazz.isAnnotationPresent(Service.class)){

            }
        }
    }
}
