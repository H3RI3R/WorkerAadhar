package com.scriza;

import java.util.concurrent.ConcurrentHashMap;
import org.openqa.selenium.WebDriver;

public class WorkerManager {
    private static ConcurrentHashMap<String, Worker> workerMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> aadharWorkerMap = new ConcurrentHashMap<>();

    public static void addWorker(String workerId, WebDriver driver) {
        Worker worker = new Worker(workerId, driver);
        workerMap.put(workerId, worker);
    }

    public static Worker getWorker(String workerId) {
        return workerMap.get(workerId);
    }

    public static Worker getAvailableWorker() {
        for (Worker worker : workerMap.values()) {
            if (worker.isAvailable()) {
                worker.setBusy();
                return worker;
            }
        }
        return null;
    }

    public static void assignAadharToWorker(String aadhar, String workerId) {
        aadharWorkerMap.put(aadhar, workerId);
    }

    public static Worker getWorkerByAadhar(String aadhar) {
        String workerId = aadharWorkerMap.get(aadhar);
        return workerId != null ? getWorker(workerId) : null;
    }

    public static void setWorkerAvailable(String workerId) {
        Worker worker = workerMap.get(workerId);
        if (worker != null) {
            worker.setAvailable();
        }
    }
}