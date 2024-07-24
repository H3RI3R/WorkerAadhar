package com.scriza;



import org.openqa.selenium.NoSuchElementException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
//import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;
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

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;

import java.util.Base64;
//import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.collect.ImmutableMap;

import antiCaptcha.DebugHelper;
import antiCaptcha.ImageToText;

import java.time.Duration;


import org.openqa.selenium.support.ui.ExpectedConditions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProcessAadhar {

	    private Connection con;
    public ProcessAadhar() {
//    	this.driver = driver;
        try {
            con = DBConnectionManager.getConnection();
        } catch (SQLException e) {
            System.out.println("Failed to establish database connection: " + e.getMessage());
        }
    }


    public void getCaptcha(String aadhar, HttpServletRequest req) {
        // Assign the local driver to the class-level driver
    	ChromeDriver driver = (ChromeDriver) req.getAttribute("webdriver"+aadhar);
       
        try {
           
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.executeCdpCommand("Page.setLifecycleEventsEnabled", ImmutableMap.of("enabled", true));
            driver.get("https://myaadhaar.uidai.gov.in/");
            WebElement login = driver.findElement(By.xpath("//button[@class='button_btn__HeAxz']"));
            login.click();
            Thread.sleep(3000);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
         
            
            WebElement aadhaarInput = driver.findElement(By.name("uid"));
            aadhaarInput.sendKeys(aadhar);
            saveCaptchaImage(driver);
          
            try { String result = solveCaptcha(driver);
        	System.out.println("Captcha Found: " + result);
        	
            enterCaptcha(driver, result);
            	if (isSvgIconVisible(driver)) { // If true, CAPTCHA was incorrect
                    System.out.println("Captcha was incorrect. Retrying...");

                    // Refresh the CAPTCHA and re-solve
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
        } catch (Exception e) {
           e.printStackTrace();
        }
        
        CustomDriver.setWebDriverMap(aadhar, driver);
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

                    // Check if error message indicates captcha value doesn't match
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


