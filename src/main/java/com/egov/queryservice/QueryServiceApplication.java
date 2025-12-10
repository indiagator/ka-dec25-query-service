package com.egov.queryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class QueryServiceApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(QueryServiceApplication.class, args);
    }

}
