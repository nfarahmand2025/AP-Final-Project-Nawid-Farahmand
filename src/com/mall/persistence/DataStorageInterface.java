package com.mall.persistence;

import java.io.IOException;

// DataStorageInterface defines the contract for saving and loading the entire system state.
// Implementations may use different formats or storage backends (e.g., JSON files, databases).
public interface DataStorageInterface {
    // Persist the provided SystemStateDto to the given file path.
    // Implementations should throw IOException on I/O failures.
    void save(String filePath, SystemStateDto state) throws IOException;

    // Load and return a SystemStateDto from the given file path.
    // Implementations should throw IOException on I/O failures.
    SystemStateDto load(String filePath) throws IOException;
}