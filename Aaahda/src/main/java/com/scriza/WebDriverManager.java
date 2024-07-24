//package com.scriza;
//
//import org.openqa.selenium.WebDriver;
//import java.util.concurrent.ConcurrentHashMap;
//
//// This class manages WebDriver instances.
//public class WebDriverManager {
//    private static final ConcurrentHashMap<String, WebDriver> webDrivers = new ConcurrentHashMap<>();
//
//    // Store a WebDriver instance associated with a key (e.g., Aadhaar number)
//    public static void storeWebDriver(String key, WebDriver driver) {
//        webDrivers.put(key, driver);
//    }
//
//    // Retrieve a WebDriver instance by its key
//    public static WebDriver getWebDriver(String key) {
//        return webDrivers.get(key);
//    }
//
//    // Remove a WebDriver instance by its key
//    public static void removeWebDriver(String key) {
//        webDrivers.remove(key);
//    }
//}
