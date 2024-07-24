package com.scriza;

import org.openqa.selenium.WebDriver;

public class Worker {
    private String id;
    private WebDriver driver;
    private boolean available;

    public Worker(String id, WebDriver driver) {
        this.id = id;
        this.driver = driver;
        this.available = true;
    }

    public String getId() {
        return id;
    }

    public WebDriver getWebDriver() {
        return driver;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setBusy() {
        this.available = false;
    }

    public void setAvailable() {
        this.available = true;
    }
}