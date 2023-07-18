package org.example;

import org.example.config.DIContext;
import org.example.config.annotations.Inject;
import org.example.config.annotations.Service;
import org.example.service.CustomerService;

@Service
public class Main {
    @Inject
    private static CustomerService service;
    public static void main(String[] args){
        DIContext container = new DIContext();
//        service = new CustomerService();
        service.getCustomer();
    }
}