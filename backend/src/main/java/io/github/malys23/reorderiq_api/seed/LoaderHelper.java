package io.github.malys23.reorderiq_api.seed;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class LoaderHelper{
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder().setHeader().get();

    static List<CSVRecord> readCsv(Path file){
        try(BufferedReader reader =  Files.newBufferedReader(file, StandardCharsets.UTF_8); CSVParser parser = FORMAT.parse(reader)){
            return parser.getRecords();
        } catch(IOException e){
            throw new IllegalStateException("Could not read " + file.getFileName(), e);
        }
    }

    static String cellContent(CSVRecord record, String columnName){
        return record.get(columnName).trim();
    }

    static Integer textToInteger(CSVRecord record, String columnName){
        return Integer.parseInt(cellContent(record, columnName));
    }

    static BigDecimal textToBigDecimal(CSVRecord record, String columnName){
        return new BigDecimal(cellContent(record, columnName));
    }

    static LocalDate textToDate(CSVRecord record, String columnName){
        String input = cellContent(record, columnName);
        if (input.isBlank()){
            return null;
        } else{
            return LocalDate.parse(input);
        }
    }

    static Integer textToNullableInteger(CSVRecord record, String columnName){
        String input = cellContent(record, columnName);
        if(input.isBlank()){
            return null;
        } else{
            return Integer.parseInt(input);
        }
    }

    static Boolean textToBoolean(CSVRecord record, String columnName){
        String input = cellContent(record, columnName);
        if(!input.equals("1") && !input.equals("0")){
            throw new IllegalArgumentException("Invalid boolean value: " + input);
        } else {
            return input.equals("1");
        }
    }
}
