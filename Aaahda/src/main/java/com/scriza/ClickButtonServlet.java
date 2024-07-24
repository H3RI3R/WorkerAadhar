package com.scriza;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.openqa.selenium.WebDriver;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import antiCaptcha.DebugHelper;
import antiCaptcha.ImageToText;

public class ClickButtonServlet extends HttpServlet {
    private static final Random random = new Random(); 
    private static final long serialVersionUID = 1L;

    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String aadhar = req.getParameter("ad");
        
        // Validate Aadhar number
        if (aadhar == null || aadhar.length() != 12) {
            sendErrorResponse(res, "Invalid Aadhar number. It must be 12 characters long.");
            return;
        }
        String orderId = generateUniqueOrderId();
        sendInitialResponse(res, orderId);
        // Get an available worker
        Worker availableWorker = WorkerManager.getAvailableWorker();
        if (availableWorker == null) {
            sendErrorResponse(res, "No available workers.");
            return;
        }

        WebDriver driver = availableWorker.getWebDriver();
        if (driver == null) {
            sendErrorResponse(res, "Worker has no WebDriver instance.");
            return;
        }
        
        WorkerManager.assignAadharToWorker(aadhar, availableWorker.getId());



        try {
            WebElement login = driver.findElement(By.xpath("//button[@class='button_btn__HeAxz']"));
            login.click();
            Thread.sleep(3000);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
         
            WebElement aadhaarInput = driver.findElement(By.name("uid"));    
            aadhaarInput.sendKeys(aadhar);
            isAadhaarNumberInvalid(driver);
            processAadharTasks( aadhar, orderId, req, res);
            saveCaptchaImage(driver);
            try { 
                String result = solveCaptcha(driver);
                System.out.println("Captcha Found: " + result);
                enterCaptcha(driver, result);
                if (isSvgIconVisible(driver)) { // If true, CAPTCHA was incorrect
                    System.out.println("Captcha was incorrect. Retrying...");
                    refreshCaptcha(driver);
                    saveCaptchaImage(driver); // Save the refreshed CAPTCHA image
                    result = solveCaptcha(driver); // Re-solve CAPTCHA
                    enterCaptcha(driver, result); // Re-enter CAPTCHA
                } else {
                    System.out.println("Captcha entered successfully.");
                }
            } catch (Exception e) {
                System.err.println("Error while solving captcha: " + e.getMessage());
            } 
            sendInitialResponse(res, orderId);
        } catch (NoSuchElementException e) {
            sendErrorResponse(res, "Button element not found.");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(res, "An error occurred while processing the request.");
        } finally {
            WorkerManager.setWorkerAvailable(availableWorker.getId()); // Set worker back to available
        }
    }
    
    private void processAadharTasks( String aadhar, String orderId, HttpServletRequest req,HttpServletResponse res) {
    	try {
    		Connection connection = DBConnectionManager.getConnection();

            String sql = "SELECT COUNT(1) FROM otp.aadhar_data WHERE aadhar_number = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, aadhar);
            ResultSet rs = preparedStatement.executeQuery();
            rs.first();

            int count = rs.getInt(1);

            // Insert or update depending on the existing record
            if (count == 0) {
                sql = "INSERT INTO otp.aadhar_data (aadhar_number, otp, order_id) VALUES (?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(sql);
                insertStatement.setString(1, aadhar);
                insertStatement.setNull(2, java.sql.Types.VARCHAR); // Null OTP
                insertStatement.setString(3, orderId);
                insertStatement.executeUpdate();
            } else {
                sql = "UPDATE otp.aadhar_data SET otp = ?, order_id = ? WHERE aadhar_number = ?";
                PreparedStatement updateStatement = connection.prepareStatement(sql);
                updateStatement.setNull(1, java.sql.Types.VARCHAR); // Null OTP
                updateStatement.setString(2, orderId);
                updateStatement.setString(3, aadhar);
                updateStatement.executeUpdate();
            }

    		
    		
    		
		} catch (Exception e) {
			// TODO: handle exception
			  e.printStackTrace(); 
		}
    }

    private String generateUniqueOrderId() {
        return String.valueOf(random.nextInt(9000) + 1000); // Generate unique 4-digit order ID
    }
//    private void sendErrorResponse(HttpServletResponse res, String errorMessage) throws IOException {
//        PrintWriter out = res.getWriter();
//        out.print("{\n  \"error\": \"" + errorMessage + "\"\n}");
//        out.flush(); // Ensure response is sent
//        out.close(); // Complete the response
//    }

    private void sendInitialResponse(HttpServletResponse res, String orderId) throws IOException {
        PrintWriter out = res.getWriter();
        String responseText = String.format(
            "{\n  \"order_id\": \"%s\",\n  \"link\": \"http://103.101.59.60/API/OTPInputServlet?order_id=%s\"\n}",
            orderId, orderId
        );
        out.print(responseText);
        out.flush(); // Ensure response is sent
        out.close(); // Complete the response
    }
    private boolean isAadhaarNumberInvalid(WebDriver driver) {
        try {
            WebElement errorElement = driver.findElement(By.className("sc-cBNfnY")); // Class indicating error
            return errorElement.isDisplayed(); // If visible, Aadhaar is invalid
        } catch (NoSuchElementException e) {
            return false; // No error element found, Aadhaar seems valid
        }
    }
    private void refreshCaptcha(WebDriver driver) {
	 WebElement refreshButton = driver.findElement(By.xpath("//img[@src='./static/media/RefreshIcon.874efff6da316d5687c409d5d2763bbe.svg']"));
        refreshButton.click();
		
	}
    private void sendErrorResponse(HttpServletResponse res, String errorMessage) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        out.print("{\n  \"error\": \"" + errorMessage + "\"\n}");
        out.flush();
    }
	private boolean isSvgIconVisible(WebDriver driver) {
		// TODO Auto-generated method stub
    	try {
            driver.findElement(By.className("MuiSvgIcon-root")); // Attempt to find the element
            return true; // If found, return true
        } catch (NoSuchElementException e) {
            return false; // If not found, return false
        }
	}

	public static String solveCaptcha(WebDriver driver) throws InterruptedException, MalformedURLException, JSONException {
        DebugHelper.setVerboseMode(true);

        ImageToText api = new ImageToText();
        api.setClientKey("2bc5595d96c12f3b83088a967c4b2f2e"); // Load key from environment variable
	    api.setFilePath("C:/Users/H3RI3R/git/AadharAutomation/Papa/captchaImages/captchaImage.jpg");

        //Specify softId to earn 10% commission with your app.
        //Get your softId here: https://anti-captcha.com/clients/tools/devcenter
        api.setSoftId(0);

        if (!api.createTask()) {
            DebugHelper.out(
                    "API v2 send failed. " + api.getErrorMessage(),
                    DebugHelper.Type.ERROR
            );
        } else if (!api.waitForResult()) {
            DebugHelper.out("Could not solve the captcha.", DebugHelper.Type.ERROR);
        } else {
        	String result = api.getTaskSolution().getText();
        	if (result.length() == 5) {
                System.out.println("Captcha Found: " + result);
            DebugHelper.out("Result: " + api.getTaskSolution().getText(), DebugHelper.Type.SUCCESS);
            return result;}
            else{
            	WebElement refreshButton = driver.findElement(By.xpath("//img[@src='./static/media/RefreshIcon.874efff6da316d5687c409d5d2763bbe.svg']"));
                refreshButton.click(); 
                
            }
        }
		return null;
    }
    public void saveCaptchaImage(WebDriver driver) {
    	  try {
              // Get the CAPTCHA image source
              WebElement imageElement = driver.findElement(By.xpath("//img[@alt='captcha']"));
              String imageSrc = imageElement.getAttribute("src");

              // Create a folder for the CAPTCHA images
              File folder = new File("C:/Users/H3RI3R/git/AadharAutomation/Papa/captchaImages");
              if (!folder.exists()) {
                  folder.mkdirs();
              }

              // Handle base64 encoded images
              if (imageSrc.startsWith("data:image")) {
                  String base64Image = imageSrc.split(",")[1]; // Get the base64 data
                  byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                  // Save the image to a file
                  File outputFile = new File(folder, "captchaImage.jpg");
                  try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                      fos.write(imageBytes);
                  }

                  System.out.println("Image downloaded and saved to: " + outputFile.getAbsolutePath());
              } else {
                  // If not a base64 data URI, treat it as a regular URL
                  URL url = new URL(imageSrc);
                  HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                  connection.setRequestMethod("GET");
                  connection.connect();

                  // Write to file
                  InputStream inputStream = connection.getInputStream();
                  File outputFile = new File(folder, "captchaImage.jpg");
                  try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                      byte[] buffer = new byte[1024];
                      int bytesRead;
                      while ((bytesRead = inputStream.read(buffer)) != -1) {
                          fos.write(buffer, 0, bytesRead);
                      }
                  }
                  
                  inputStream.close();
                  connection.disconnect();

                  System.out.println("Image downloaded and saved to: " + outputFile.getAbsolutePath());
              }

          } catch (Exception e) {
              System.err.println("Error during image download or save: " + e.getMessage());
          }
    }
    private void enterCaptcha(WebDriver driver, String captchaText) {
        int maxAttempts = 6; // Maximum number of attempts to enter captcha
        int attempts = 0; // Initialize attempts counter
        while (attempts < maxAttempts) {
            try {
                if (captchaText != null) {
                    WebElement captchaInput = driver.findElement(By.name("captcha"));
                    captchaInput.sendKeys(captchaText);
                    Thread.sleep(2000);
                    // Click on Send OTP button
                    WebElement sendOTPButton = driver.findElement(By.xpath("//button[@class='button_btn__A84dV']"));
                    sendOTPButton.click();
                    Thread.sleep(2000);

                    // Check if error messagew indicates captcha value doesn't match
                    WebElement errorMessage = driver.findElement(By.xpath("//div[@class='login-section__error']"));
                    if (errorMessage.getText().contains("Captcha value doesn't match")) {
                        // Click on refresh icon to refresh captcha
                        WebElement refreshIcon = driver.findElement(By.xpath("//img[@src='./static/media/RefreshIcon.874efff6da316d5687c409d5d2763bbe.svg']"));
                        refreshIcon.click();
                        Thread.sleep(2000);
                        saveCaptchaImage(driver);
                        captchaText =solveCaptcha(driver);
                        enterCaptcha(driver, captchaText);
                         // Retrieve new captcha text
                        attempts++; // Increment attempts counter
                        continue; // Retry entering captcha with new text
                    }

                    return; // Exit method
                } else {
                    Thread.sleep(5000); // Wait for 5 seconds before checking again
                }
            } catch (Exception e) {
                System.out.println("Exception caught while entering captcha: " + e.getMessage());
            }
            attempts++; // Increment attempts counter
        }
    }
    public String retrieveOTPFromDatabase(String aadharNumber) throws SQLException, InterruptedException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String retrievedOTP = null;

        try {
            // Establish a connection to the database
            con = DBConnectionManager.getConnection();
            
            // Prepare SQL statement to select the OTP for the given Aadhar number
            String sql = "SELECT otp FROM aadhar_data WHERE aadhar_number = ? ORDER BY timestamp_column DESC LIMIT 1";
            ps = con.prepareStatement(sql);
            
            // Set the Aadhar number as a parameter in the prepared statement
            ps.setString(1, aadharNumber);
            
            // Continuously check if the OTP is not null
            while (retrievedOTP == null) {
                // Execute the query
                rs = ps.executeQuery();

                // Retrieve the OTP from the result set
                if (rs.next()) {
                    retrievedOTP = rs.getString("otp");
                } else {
                    // Sleep for a short duration before checking again
                    Thread.sleep(1000); // Sleep for 1 second
                }

                // Close the result set
                if (rs != null) {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving OTP from database: " + e.getMessage());
        } finally {
            // Close the prepared statement and connection in a finally block to ensure they are closed even if an exception occurs
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        }

        return retrievedOTP;
    }}