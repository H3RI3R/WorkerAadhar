package com.scriza;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.openqa.selenium.WebDriver;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AddServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Random random = new Random();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        handleRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        handleRequest(req, res);
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String aadhar = req.getParameter("ad");
        if (aadhar == null || aadhar.length() != 12) {
            sendErrorResponse(res, "Invalid Aadhar number. It must be 12 characters long.");
            return;
        }

        String orderId = generateUniqueOrderId();
        AsyncContext asyncContext = req.startAsync();
        sendInitialResponse(res, orderId);

        CompletableFuture.runAsync(() -> {
            try {
                processAadharTasks(asyncContext, req, aadhar, orderId);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                asyncContext.complete();
            }
        });
    }

    private void sendErrorResponse(HttpServletResponse res, String errorMessage) throws IOException {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try (PrintWriter out = res.getWriter()) {
            out.print("{\"error\": \"" + errorMessage + "\"}");
            out.flush();
        }
    }

    private void sendInitialResponse(HttpServletResponse res, String orderId) throws IOException {
        try (PrintWriter out = res.getWriter()) {
            String responseText = String.format(
                "{ \"order_id\": \"%s\", \"link\": \"http://103.101.59.60/API/OTPInputServlet?order_id=%s\" }",
                orderId, orderId
            );
            out.print(responseText);
            out.flush();
        }
    }

    private void processAadharTasks(AsyncContext asyncContext, HttpServletRequest req, String aadhar, String orderId) throws IOException {
        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        try (Connection connection = DBConnectionManager.getConnection()) {
            String sql = "SELECT COUNT(1) FROM otp.aadhar_data WHERE aadhar_number = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, aadhar);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        if (count == 0) {
                            sql = "INSERT INTO otp.aadhar_data (aadhar_number, otp, order_id) VALUES (?, ?, ?)";
                            try (PreparedStatement insertStatement = connection.prepareStatement(sql)) {
                                insertStatement.setString(1, aadhar);
                                insertStatement.setNull(2, java.sql.Types.VARCHAR);
                                insertStatement.setString(3, orderId);
                                insertStatement.executeUpdate();
                            }
                        } else {
                            sql = "UPDATE otp.aadhar_data SET otp = ?, order_id = ? WHERE aadhar_number = ?";
                            try (PreparedStatement updateStatement = connection.prepareStatement(sql)) {
                                updateStatement.setNull(1, java.sql.Types.VARCHAR);
                                updateStatement.setString(2, orderId);
                                updateStatement.setString(3, aadhar);
                                updateStatement.executeUpdate();
                            }
                        }
                    }
                }
            }

            Worker worker = WorkerManager.getWorker(aadhar);
            if (worker == null) {
                sendErrorResponse(response, "No available worker for the given Aadhar number.");
                return;
            }

            WebDriver webDriver = worker.getWebDriver();
            req.setAttribute("webdriver" + aadhar, webDriver);
            ProcessAadhar processAadhar = new ProcessAadhar();
            processAadhar.getCaptcha(aadhar, req);

        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, "Database error occurred.");
        }
    }

    private String generateUniqueOrderId() {
        return String.valueOf(random.nextInt(9000) + 1000);
    }
}