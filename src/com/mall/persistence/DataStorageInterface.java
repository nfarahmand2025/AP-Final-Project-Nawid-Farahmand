package com.mall.persistence;

import java.io.IOException;

public interface DataStorageInterface {
    void save(String filePath, SystemStateDto state) throws IOException;

    SystemStateDto load(String filePath) throws IOException;
}