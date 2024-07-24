package com.scriza;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class StartWorkersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String workerId = req.getParameter("worker");
        if (workerId == null || workerId.isEmpty()) {
            sendErrorResponse(resp, "Worker ID is required.");
            return;
        }

        Executors.newSingleThreadExecutor().submit(() -> {
            WebDriver driver = null;
            try {
                // Set up WebDriver
                System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
                ChromeOptions options = new ChromeOptions();
                options.setBinary("/Applications/Chromium.app/Contents/MacOS/Chromium");
                driver = new ChromeDriver(options);

                // Add worker to WorkerManager
                WorkerManager.addWorker(workerId, driver);

                // Navigate to the initial URL
                driver.get("https://myaadhaar.uidai.gov.in/");
                TimeUnit.SECONDS.sleep(10); // Wait for the page to load

            } catch (Exception e) {
                e.printStackTrace();
                // Handle exception properly (e.g., log it, update status)
            }
        });

        // Send initial response
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"message\":\"Worker started successfully.\"}");
            out.flush();
        }
    }

    private void sendErrorResponse(HttpServletResponse resp, String errorMessage) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"error\": \"" + errorMessage + "\"}");
            out.flush();
        }
    }
}