package com.cnebula.nature.entity;

import java.util.List;

public class FileNameEntity {

    private List<List<String>> fileNames;

    public FileNameEntity() {
    }

    public FileNameEntity(List<List<String>> fileNames) {
        this.fileNames = fileNames;
    }

    public List<List<String>> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<List<String>> fileNames) {
        this.fileNames = fileNames;
    }
}
