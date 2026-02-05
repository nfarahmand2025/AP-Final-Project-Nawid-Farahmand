package com.mall.persistence;

import java.io.IOException;

/**
 * Interface for persisting and retrieving the application's system state.
 *
 * Implementations of this interface handle saving and loading
 * {@link SystemStateDto} objects to and from a specified file path.
 *
 * Different implementations may store data in various formats such as
 * JSON, XML, or binary files.
 */
public interface DataStorageInterface {
    void save(String filePath, SystemStateDto state) throws IOException;

    SystemStateDto load(String filePath) throws IOException;
}
