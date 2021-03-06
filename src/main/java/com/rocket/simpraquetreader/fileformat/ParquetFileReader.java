package com.rocket.simpraquetreader.fileformat;

import com.rocket.simpraquetreader.fileformat.int96.ParquetTimestampUtils;
import org.apache.avro.Conversion;
import org.apache.avro.LogicalType;
import org.apache.avro.Schema;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;

import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ParquetFileReader implements Reader {

  private final Path path;
  private final Configuration conf;

  public ParquetFileReader(File file) {
    this.path = file.toPath();
    this.conf = new Configuration();
    this.conf.set("parquet.avro.readInt96AsFixed", "true");
    GenericDataConfigurer.configureGenericData();
  }

  @Override
  public String getSchema() throws IOException {
    try (ParquetReader<Object> parquetReader =
        AvroParquetReader.builder(new LocalInputFile(this.path)).withConf(this.conf).build()) {
      GenericData.Record firstRecord = (GenericData.Record) parquetReader.read();
      if (firstRecord == null) {
        throw new IOException("Can't process empty Parquet file");
      }
      return firstRecord.getSchema().toString(true);
    }
  }

  @Override
  public int getRecordsCount() throws IOException {
    try (ParquetReader<Object> parquetReader =
        AvroParquetReader.builder(new LocalInputFile(this.path))
            .withDataModel(GenericData.get())
            .withConf(this.conf)
            .build()) {
      GenericData.Record value;
      int i = 0;
      while (true) {
        value = (GenericData.Record) parquetReader.read();
        if (value == null) {
          return i;
        } else {
          i++;
        }
      }
    }
  }

  @Override
  public List<String> getRecords(int numRecords) throws IOException, IllegalArgumentException {
    try (ParquetReader<Object> parquetReader =
        AvroParquetReader.builder(new LocalInputFile(this.path))
            .withDataModel(GenericData.get())
            .withConf(this.conf)
            .build()) {
      List<String> records = new ArrayList<>();
      GenericData.Record value;
      for (int i = 0; i < numRecords; i++) {
        value = (GenericData.Record) parquetReader.read();
        if (value == null) {
          System.out.printf("Retrieved %d records%n", records.size());
          return records;
        } else {
          String jsonRecord =
              deserialize(value.getSchema(), toByteArray(value.getSchema(), value)).toString();
          jsonRecord = ParquetTimestampUtils.convertInt96(jsonRecord);
          records.add(jsonRecord);
        }
      }
      System.out.printf("Retrieved %d records%n", records.size());
      return records;
    }
  }

  /**
   * Correctly converts timestamp-milis LogicalType values to strings. Taken from
   * https://stackoverflow.com/a/52041154/729819.
   */
  private GenericRecord deserialize(Schema schema, byte[] data) throws IOException {
    GenericData.get().addLogicalTypeConversion(new TimestampMillisConversion());
    InputStream is = new ByteArrayInputStream(data);
    Decoder decoder = DecoderFactory.get().binaryDecoder(is, null);
    DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema, schema, GenericData.get());
    return reader.read(null, decoder);
  }

  private byte[] toByteArray(Schema schema, GenericRecord genericRecord) throws IOException {
    GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
    writer.getData().addLogicalTypeConversion(new TimeConversions.TimestampMillisConversion());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
    writer.write(genericRecord, encoder);
    encoder.flush();
    return baos.toByteArray();
  }

  public static class TimestampMillisConversion extends Conversion<String> {
    public TimestampMillisConversion() {}

    public Class<String> getConvertedType() {
      return String.class;
    }

    public String getLogicalTypeName() {
      return "timestamp-millis";
    }

    public String fromLong(Long millisFromEpoch, Schema schema, LogicalType type) {
      return Instant.ofEpochMilli(millisFromEpoch).toString();
    }

    public Long toLong(String timestamp, Schema schema, LogicalType type) {
      return Long.valueOf(timestamp);
    }
  }
}
