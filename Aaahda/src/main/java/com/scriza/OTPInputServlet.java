package com.scriza;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@MultipartConfig
public class OTPInputServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part otpPart = request.getPart("otp");
        String orderId = request.getParameter("order_id");
        String otp = otpPart != null ? new String(otpPart.getInputStream().readAllBytes()) : null;

        if (orderId == null || otp == null) {
            sendErrorResponse(response, "Order ID and OTP are required.");
            return;
        }

        String aadhar = null;

        try (Connection connection = DBConnectionManager.getConnection()) {
            // Update OTP in the database
            String updateOtpSql = "UPDATE otp.aadhar_data SET otp = ? WHERE order_id = ?";
            try (PreparedStatement updatePreparedStatement = connection.prepareStatement(updateOtpSql)) {
                updatePreparedStatement.setString(1, otp);
                updatePreparedStatement.setString(2, orderId);
                updatePreparedStatement.executeUpdate();
            }

            // Retrieve Aadhar number based on order ID
            String sql = "SELECT aadhar_number FROM otp.aadhar_data WHERE order_id=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, orderId);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        aadhar = rs.getString("aadhar_number");
                    }
                }
            }

            // Handle case where aadhar is null
            if (aadhar == null) {
                sendErrorResponse(response, "Aadhar number not found for the given Order ID.");
                return;
            }

            // Get Worker for the Aadhar number
            Worker worker = WorkerManager.getWorkerByAadhar(aadhar);
            if (worker == null) {
                sendErrorResponse(response, "Worker is not available.");
                return;
            }
            
            WebDriver driver = worker.getWebDriver();
            if (driver == null) {
                sendErrorResponse(response, "Worker has no WebDriver instance.");
                return;
            }

            // Enter OTP and submit
            driver.findElement(By.name("otp")).sendKeys(otp);
            try {
    			Thread.sleep(2000);
    		} catch (InterruptedException e) {

    			e.printStackTrace();
    		}
            
            driver.findElement(By.name("otp")).sendKeys(otp);
            try {
    			Thread.sleep(2000);
    		} catch (InterruptedException e) {

    			e.printStackTrace();
    		}
            
            driver.findElement(By.className("button_btn__A84dV")).click();
            Thread.sleep(2000);
            try {
                WebElement errorElement = driver.findElement(By.className("login-section__error"));
                if (errorElement.isDisplayed() && errorElement.getText().contains("Invalid OTP")) {
                    // If an error message is displayed indicating invalid OTP, return a custom response
                    sendErrorResponse(response, "Invalid OTP. Please enter again.");
                    return; // Stop further processing
                }
            } catch (NoSuchElementException e) {
                // No error message found, continue
            }

            if (!driver.findElements(By.className("aadhaar-front")).isEmpty()) {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                WebElement profileElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='profile']")));
                profileElement.click();

                JSONObject jsonData = scrapeDataFromWebsite(driver);
                if (jsonData != null) {
                    // Insert scraped data into the database
                    String insertSql = "INSERT INTO user_json_data (order_id, user_data_in_json) VALUES (?, ?)";
                    try (PreparedStatement insertPreparedStatement = connection.prepareStatement(insertSql)) {
                        insertPreparedStatement.setString(1, orderId);
                        insertPreparedStatement.setString(2, jsonData.toString());
                        insertPreparedStatement.executeUpdate();
                    }

                    // Send success response
                    sendSuccessResponse(response, orderId);
                } else {
                    sendErrorResponse(response, "Failed to scrape data from website.");
                }

                // Logout
                try {
                    driver.findElement(By.className("header__log-out-button")).click();
                } catch (NoSuchElementException e) {
                    // Handle the case where the logout button is not found
                    sendErrorResponse(response, "Failed to log out.");
                }
            } else {
                sendErrorResponse(response, "Invalid OTP. Please enter again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, "Database error occurred.");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, "An error occurred during processing.");
        }
    }

    private void sendErrorResponse(HttpServletResponse res, String errorMessage) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        try (PrintWriter out = res.getWriter()) {
            out.print("{\n  \"error\": \"" + errorMessage + "\"\n}");
            out.flush();
        }
    }

    private void sendSuccessResponse(HttpServletResponse res, String orderId) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        StringBuilder jsonResponse = new StringBuilder();
        jsonResponse.append("{\n");
        jsonResponse.append("\"data\": \"Successful\",").append("\n");
        jsonResponse.append("\"link\": \"http://103.101.59.60/API/checkStatus?order_id=").append(orderId).append("\"\n");
        jsonResponse.append("}");

        try (PrintWriter out = res.getWriter()) {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }

    private JSONObject scrapeDataFromWebsite(WebDriver driver) {
        JSONObject jsonData = new JSONObject();

        try {
            String htmlContent = driver.getPageSource();
            Document doc = Jsoup.parse(htmlContent);

            Element nameElement = doc.selectFirst("div.name-local");
            String name = nameElement != null ? nameElement.text() : "";
            jsonData.put("Name", name);

            Element aadharContentElement = doc.selectFirst("div.aadhaar-front__aadhaar-content");
            String aadharContent = aadharContentElement != null ? aadharContentElement.text() : "";
            jsonData.put("AadharContent", aadharContent);

            Element ageElement = doc.selectFirst("div.name-english");
            String age = ageElement != null ? ageElement.text() : "";
            jsonData.put("Age", age);

            Element dobElement = doc.selectFirst(".dob");
            String dob = dobElement != null ? dobElement.text() : "";
            jsonData.put("DateOfBirth", dob);

            Element genderElement = doc.selectFirst(".gender");
            String gender = genderElement != null ? genderElement.text() : "";
            jsonData.put("Gender", gender);

            Element aadharNumElement = doc.selectFirst(".aadhaar-front__aadhaar-number");
            String aadharNum = aadharNumElement != null ? aadharNumElement.text() : "";
            jsonData.put("AadharNumber", aadharNum);

            Element aadharAddressElement = doc.selectFirst(".aadhaar-back__address-english");
            String aadharAddress = aadharAddressElement != null ? aadharAddressElement.text() : "";
            jsonData.put("AadharAddress", aadharAddress);

            return jsonData;
        } catch (Exception e) {
            System.out.println("Error scraping data from website: " + e.getMessage());
            return null;
        }
    }
}