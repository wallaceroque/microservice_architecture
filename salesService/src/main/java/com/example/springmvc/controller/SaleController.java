package com.example.springmvc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.springmvc.model.Sale;
import com.example.springmvc.repository.SaleRepository;

@Controller
public class SaleController {

	private SaleRepository saleRepository;

    @Autowired
    public void setSaleRepository(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @RequestMapping(path = "/")
    public String index() {
        return "index";
    }

    
    @RequestMapping(path = "/sales", method = RequestMethod.GET)
    public String getAllSales(Model model) {
        model.addAttribute("sales", saleRepository.findAll());
        return "sales";
    }	 
}
