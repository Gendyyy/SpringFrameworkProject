package org.example.service;

import org.example.config.annotations.Inject;
import org.example.config.annotations.Qualifier;
import org.example.config.annotations.Service;
import org.example.dao.IDao;

@Service
public class CustomerService {
//    @Inject
    private IDao customerDao;

    @Inject
    public CustomerService(@Qualifier("PRDDao") IDao customerDao) {
        this.customerDao = customerDao;
    }

    public CustomerService() {
    }

    //    @Inject
    public void setCustomerDao(IDao customerDao){
        this.customerDao = customerDao;
    }

    public void getCustomer(){
        customerDao.getCustomer();
    }
}
