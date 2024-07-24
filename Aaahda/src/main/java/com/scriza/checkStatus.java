package com.scriza;

import java.io.IOException;
//import org.jsoup:jsoup:1.17.2;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class checkStatus extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String orderId = request.getParameter("order_id");

        // Initialize the JSON response object
        JSONObject jsonResponse = new JSONObject();

        try {
            // Establish a connection to the database
            Connection connection = DBConnectionManager.getConnection();
            
            // SQL query to get user data from user_json_data where order_id matches
            String sql = "SELECT user_data_in_json FROM user_json_data WHERE order_id = ?";
            
            // Prepare the SQL statement
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, orderId);
            
            // Execute the query
            ResultSet resultSet = preparedStatement.executeQuery();
            
            // If a record is found, retrieve the data
            if (resultSet.next()) {
                String userDataInJson = resultSet.getString("user_data_in_json");
                jsonResponse.put("status", "success");
                jsonResponse.put("data", new JSONObject(userDataInJson));
                
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "No data found for the given order_id.");
            }
            
            // Close the resources
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Database error: " + e.getMessage());
        }

        // Set the response type to JSON and send the JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
}
