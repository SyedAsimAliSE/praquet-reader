package com.rocket.simpraquetreader;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        FileReader fileReader = new FileReader();

        try {

            fileReader.setFileName("ga_ls_wit_qld_class_myear_3.parquet");

//            String schema = fileReader.getSchema();
//            System.out.println(schema);

//            List<String> allRecords = fileReader.getAllRecords();

            int totalRecordsCount = fileReader.getTotalRecordsCount();

            if (totalRecordsCount > 0) {

                List<String> records = fileReader.getRecords(3);
                System.out.println(records);

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
