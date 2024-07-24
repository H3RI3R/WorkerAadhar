package com.scriza;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class BatchAadharProcess extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static List<String> aadhaarNumbers = new ArrayList<>();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        for (String str : request.getParameterValues("multiInput")) {
            aadhaarNumbers.add(str);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(aadhaarNumbers.size());

        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        
        for (String aadhar : aadhaarNumbers) {
            executorService.submit(() -> {
                try {
                    System.out.println("Started thread for aadharNumber : " + aadhar);
                    WebDriver driver = CustomDriver.getWebDriver(aadhar);
                    if (driver == null) {
                        driver = new ChromeDriver();
                        CustomDriver.setWebDriverMap(aadhar, driver);
                    }
                    driver.manage().window().maximize();
                    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                    driver.get("http://localhost:8080/Papa/add?ad=" + aadhar);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        while (!executorService.isShutdown()) {
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}