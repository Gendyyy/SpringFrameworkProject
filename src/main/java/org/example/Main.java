package org.example;

import org.example.config.DIContainer;
import org.example.config.annotations.Inject;
import org.example.config.annotations.Service;
import org.example.service.CustomerService;

import java.lang.reflect.InvocationTargetException;

@Service
public class Main {
    @Inject
    private static CustomerService service;
    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        DIContainer container = new DIContainer();
        service.getCustomer();
    }
}