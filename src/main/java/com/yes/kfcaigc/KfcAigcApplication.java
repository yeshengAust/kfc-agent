package com.yes.kfcaigc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yes.kfcaigc.mapper")
public class KfcAigcApplication {

    public static void main(String[] args) {
        SpringApplication.run(KfcAigcApplication.class, args);
    }

}
