package com.example.springmvc.controller;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import com.example.springmvc.model.Product;
import com.example.springmvc.repository.ProductRepository;

@Controller
public class StoreController {
	
	static Logger logger = Logger.getLogger(StoreController.class.getName());
   
	static final String URL_CREATE_EMPLOYEE = "http://localhost:5555/sale";

	private ProductRepository productRepository;

    @Autowired
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @RequestMapping(path = "/store", method = RequestMethod.GET)
    public String getAllProductsStore(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "store";
    }
    
    @RequestMapping(path = "/products/store/{id}", method = RequestMethod.GET)
    public String buyProduct(@PathVariable(name = "id") String id) {
    	Product product = productRepository.findOne(id);
    	RestTemplate restTemplate = new RestTemplate();
	 
     // Data attached to the request.
         HttpEntity<Product> requestBody = new HttpEntity<>(product);
  
         // Send request with POST method.
         ResponseEntity<Product> result = restTemplate.postForEntity(URL_CREATE_EMPLOYEE, requestBody, Product.class);
  
         logger.info("Status code:" + result.getStatusCode());
  
         // Code = 200.
         if (result.getStatusCode() == HttpStatus.OK) {
        	 product = result.getBody();
        	 logger.info("(Client Side) Product Sale: "+ product.getName());
        	 return result.getBody().getName();
         }
         return "";
         
    }
    

	
    public String buyProductService(Product product) {
 		
       
 
       return "Sale Canceled!!!";
    }
	 
}
