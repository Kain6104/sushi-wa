package com.mcnz.rps.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mcnz.rps.model.Notification;
import com.mcnz.rps.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@Controller
public class BaseController  {

    @Autowired
    private NotificationService notificationService;

  

}
