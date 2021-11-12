package com.rocket.simpraquetreader;

import com.rocket.simpraquetreader.fileformat.ParquetFileReader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FileReader {

    private String fileName;

    // GET SCHEMA
    public String getSchema() throws IOException {

        ParquetFileReader parquetFileReader = readRecords(fileName);

        return parquetFileReader.getSchema();

    }

    // GET TOTAL RECORDS IN THE FILE
    public int getTotalRecordsCount() throws IOException {

        ParquetFileReader parquetFileReader = readRecords(fileName);

        return parquetFileReader.getRecordsCount();

    }

    // GET THE LIST OF RECORDS
    public List<String> getRecords(int count) throws IOException {

        ParquetFileReader parquetFileReader = readRecords(fileName);

        return parquetFileReader.getRecords(count);
    }

    // GET ALL RECORDS
    public List<String> getAllRecords() throws IOException {

        ParquetFileReader parquetFileReader = readRecords(fileName);
        int totalRecords = parquetFileReader.getRecordsCount();

        return parquetFileReader.getRecords(totalRecords);
    }

    private ParquetFileReader readRecords(String fileName) {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile());
        return new ParquetFileReader(file);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
