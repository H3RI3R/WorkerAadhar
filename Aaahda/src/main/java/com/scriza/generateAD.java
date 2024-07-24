//package com.scriza;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.Random;
//
//import org.openqa.selenium.chrome.ChromeDriver;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletResponse;
//
//public class generateAD implements Runnable {
//	 private String orderId = generateUniqueOrderId();
//     private final String aadhar;
//     private final int port;
//     private final HttpServletResponse res;
//     public void init() throws ServletException {
//         // Initialize the WebDriver here
//         System.setProperty("webdriver.chrome.driver", "C:\\selenium WebDriver\\chromedriver-win64\\chromedriver.exe");
//         CustomDriver.webDriver = new ChromeDriver();
//     }
//
//     public void destroy() {
//         // Quit the WebDriver and perform cleanup here
//         if (CustomDriver.webDriver != null) {
//         	CustomDriver.webDriver.quit();
//         }
//     }
//	public generateAD(String aadhaar1, int i, HttpServletResponse res) {
//		this.aadhar = aadhaar1;
//		this.port = i;
//		this.res = res;
//		// TODO Auto-generated constructor stub
//	}
//
//	private String generateUniqueOrderId() {
//		// TODO Auto-generated method stub
//		  Random rand = new Random();
//	        return String.format("%04d", rand.nextInt(10000));
//	
//	}
//
//	@Override
//	public void run() {
//		Random rand = new Random();
//	     String orderId = String.format("%04d", rand.nextInt(10000));
//
//	     // Insert Aadhar number into the database
//	     // (Assuming DBConnectionManager and other methods are properly defined)
//	     try {
//	     	Connection connection = DBConnectionManager.getConnection();
//	         String sql = "select count(1) from aadhar_data where aadhar_number=?";
//	         PreparedStatement preparedStatement = connection.prepareStatement(sql);
//	         preparedStatement.setString(1, aadhar);
//	         ResultSet rs = preparedStatement.executeQuery();
//	         int count =0;
//	         while(rs.next()) {
//	         	count++;
//	         }
//	         if(count<=0){
//	              sql = "INSERT INTO aadhar_data (aadhar_number, otp, order_id) VALUES (?, ?, ?)";
//	              preparedStatement = connection.prepareStatement(sql);
//	              preparedStatement.setString(1, aadhar);
//	              preparedStatement.setString(2, null);
//	              preparedStatement.setString(3, orderId);
//	         }
//	         else {
//	             sql = "UPDATE aadhar_data SET otp = ?, order_id = ? WHERE aadhar_number = ?";
//	             preparedStatement = connection.prepareStatement(sql);
//	             preparedStatement.setString(1, null);
//	             preparedStatement.setString(2, orderId);
//	             preparedStatement.setString(3, aadhar);
//	         }
//	         int rowsInserted = preparedStatement.executeUpdate();
//	         if (rowsInserted > 0) {
//	             System.out.println("Aadhar number inserted/updated successfully");
//	         } else {
//	             System.out.println("Failed to insert Aadhar number into the database");
//	             // You can handle the failure scenario here
//	         }
//	     } catch (SQLException e) {
//	         System.out.println("Failed to establish database connection: " + e.getMessage());
//	         // You can handle the database connection failure here
//	     }
//
//	     // Create an instance of ProcessAadhar and pass the WebDriver instance to it
//	     ProcessAadhar processAadhar = new ProcessAadhar(CustomDriver.webDriver);
//
//	     // Get the WebDriver object after capturing the captcha
//	    processAadhar.getCaptcha(aadhar, res);
//	     System.out.println(aadhar);
//
//	     // Forward the request to otpinput.html
//	     
//
//	     StringBuilder jsonResponse = new StringBuilder();
//	     jsonResponse.append("{\n");
//	     jsonResponse.append("\"order_id\": ").append(orderId).append(",\n");
//	     jsonResponse.append("\"link\": \"http://localhost:8081/Papa/OTPInputServlet?order_id=").append(orderId).append("\"\n");
//	     jsonResponse.append("}");
//
//	     // Set response content type to JSON
//	     res.setContentType("application/json");
//	     res.setCharacterEncoding("UTF-8");
//
//	     // Write JSON response to the client
//	     PrintWriter out;
//		try {
//			out = res.getWriter();
//			out.print(jsonResponse.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	     
//	
//		// TODO Auto-generated method stub
//
//	}
//
//}
