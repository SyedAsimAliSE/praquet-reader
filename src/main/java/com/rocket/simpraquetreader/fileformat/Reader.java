package com.rocket.simpraquetreader.fileformat;

import java.io.IOException;
import java.util.List;

public interface Reader {

  int getRecordsCount() throws IOException;

  List<String> getRecords(int numRecords) throws IOException;

  String getSchema() throws IOException;
}
