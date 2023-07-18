package org.example.dao;

import org.example.config.annotations.Profile;
import org.example.config.annotations.Service;
import org.example.config.annotations.Value;
import org.example.domain.Customer;

import java.util.ArrayList;


@Service
@Profile("two")
public class CustomerDao implements IDao{
    private ArrayList<Customer> customers;
    @Value("this is customer 1")
    private String text;

    public void getCustomer(){
        System.out.println(text);
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public void addCustomers(Customer customer) {
        customers.add(customer);
    }
}
