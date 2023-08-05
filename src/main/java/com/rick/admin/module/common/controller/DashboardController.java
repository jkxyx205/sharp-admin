package com.rick.admin.module.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Rick.Xu
 * @date 2023/5/29 17:01
 */
@Controller
@RequestMapping("dashboard")
public class DashboardController {

    @GetMapping
    public String gotoDashboard() {
        return "dashboard";
    }

    @GetMapping("help")
    public String gotoHelp() {
        return "help";
    }
}

