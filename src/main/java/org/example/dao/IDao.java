package org.example.dao;

import org.example.domain.Customer;

import java.util.ArrayList;

public interface IDao {
    void getCustomer();

    ArrayList<Customer> getCustomers();

    void addCustomers(Customer customer);
}
