package com.example.springmvc.boot;

import com.example.springmvc.model.Product;
import com.example.springmvc.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private ProductRepository productRepository;

    @Autowired
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... strings) throws Exception {

        Product product1 = new Product();
        product1.setName("Arroz");
        product1.setDescription("Arroz Integral");
        product1.setType("Grãos");
        product1.setCategory("GRA");
        product1.setPrice(1.99);

        productRepository.save(product1);

        Product product2 = new Product();
        product2.setName("Chá");
        product2.setDescription("Chá Matte");
        product2.setType("Bebida");
        product2.setCategory("BED");
        product2.setPrice(2.99);

        productRepository.save(product2);
    }
}
