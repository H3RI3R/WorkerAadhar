package com.scriza;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WorkerInitializerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int numberOfWorkers = Integer.parseInt(req.getParameter("count")); // Number of workers to initialize
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");

        for (int i = 0; i < numberOfWorkers; i++) {
            ChromeOptions options = new ChromeOptions();
            options.setBinary("/Applications/Chromium.app/Contents/MacOS/Chromium");
            WebDriver driver = new ChromeDriver(options);

            // Use worker ID to create a new Worker instance
            String workerId = "worker" + i;
            Worker worker = new Worker(workerId, driver); 
            WorkerManager.addWorker(workerId, worker);
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().print("{\"message\":\"Workers initialized successfully.\"}");
    }
}