package com.scriza;

import java.util.concurrent.ConcurrentHashMap;
import org.openqa.selenium.WebDriver;

public class CustomDriver {
    private static ConcurrentHashMap<String, WebDriver> webDriverMap = new ConcurrentHashMap<>();

    // Add WebDriver to the map
    public static void addWebDriver(String aadhar, WebDriver driver) {
        webDriverMap.put(aadhar, driver);
    }

    // Get WebDriver from the map
    public static WebDriver getWebDriver(String aadhar) {
        return webDriverMap.get(aadhar);
    }

    // Remove WebDriver from the map
    public static void removeWebDriver(String aadhar) {
        WebDriver driver = webDriverMap.remove(aadhar);
        if (driver != null) {
            driver.quit();
        }
    }
}