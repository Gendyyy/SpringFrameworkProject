package org.example.dao;

import org.example.domain.Customer;

import java.util.ArrayList;

public class CustomerDao {
    private ArrayList<Customer> customers;

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public void addCustomers(Customer customer) {
        customers.add(customer);
    }
}
