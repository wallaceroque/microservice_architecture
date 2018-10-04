package com.example.springmvc.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.springmvc.model.Sale;
import com.example.springmvc.repository.SaleRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private SaleRepository salesRepository;

    @Autowired
    public void setSalesRepository(SaleRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    @Override
    public void run(String... strings) throws Exception {

        Sale sale1 = new Sale();
        sale1.setName("João Silva");
        sale1.setDescription("compra");
        sale1.setAdress("Rua Ipiranga");
        sale1.setCredcard("VISA");
        
        salesRepository.save(sale1);

        Sale sale2 = new Sale();
        sale2.setName("Maria Silva");
        sale2.setDescription("outra compra");
        sale2.setAdress("Rua Hilário de Gouveia");
        sale2.setCredcard("MASTER");
        
        salesRepository.save(sale2);
    }
}
