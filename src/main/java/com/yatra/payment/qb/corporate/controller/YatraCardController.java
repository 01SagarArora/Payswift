/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yatra.payment.qb.corporate.controller;

import com.yatra.payment.qb.corporate.manager.QBManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author YATRAONLINE\rohit.lohia
 */
@Controller
@RequestMapping("/yatra-quickbook/**")
public class YatraCardController {
    
    @Autowired
    private QBManager qbManager;
    
    @RequestMapping("get-yatra-cards")
    public @ResponseBody String getYatraCards(HttpServletRequest request, HttpServletResponse response) {
            return qbManager.getYatraCards(request);
    }
        
    @RequestMapping("save-yatra-card")
    public @ResponseBody String addCard(HttpServletRequest request, HttpServletResponse response) {
            return qbManager.saveYatraCard(request);
    }
    
    @RequestMapping("delete-yatra-card")
    public @ResponseBody String deleteCard(HttpServletRequest request, HttpServletResponse response) throws Exception {
            return qbManager.deleteYatraCard(request);
    }
}
