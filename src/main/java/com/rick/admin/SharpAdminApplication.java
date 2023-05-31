package com.rick.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author Rick.Xu
 * @date 2023/5/27 18:09
 */
@SpringBootApplication
@EnableCaching
public class SharpAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SharpAdminApplication.class, args);
    }

}
