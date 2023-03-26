package com.example.demo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.RequestJson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import jakarta.persistence.*;


@CrossOrigin
@RestController


public class Controller
{
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;


    @PostMapping(path="/getData")
    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> getTable(@RequestBody RequestJson requestJson) {
        try {
            String tableName = requestJson.getSemester() +"_circ_stats_" + requestJson.getSubParts() + "_" + requestJson.getYear();
            String query = "SELECT  PATRON_GROUP_NAME, CountOfCHARGE_DATE_ONLY, CountOfRENEWAL_COUNT, StartDate, EndDate, TITLE, CALL_NO, AUTHOR, PUBLISHER, Circulation_Notes FROM " + tableName;
            List<Object[]> results = entityManager.createNativeQuery(query).getResultList();    
            List<Map<String, Object>> responseList = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("PATRON_GROUP_NAME", row[0]);
                responseMap.put("CountOfCHARGE_DATE_ONLY", row[1]);
                responseMap.put("CountOfRENEWAL_COUNT", row[2]);
                responseMap.put("StartDate", row[3]);
                responseMap.put("EndDate", row[4]);
                responseMap.put("TITLE", row[5]);
                responseMap.put("CALL_NO", row[6]);
                responseMap.put("AUTHOR", row[7]);
                responseMap.put("PUBLISHER", row[8]);
                responseMap.put("Circulation_Notes", row[9]);
                responseList.add(responseMap);
            }
            return responseList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
  
    @PostMapping(path = "/uploadData")
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> uploadData(@RequestParam("file") MultipartFile file, @RequestParam("tableName") String tableName) {
        try {
            List<String[]> rows = new ArrayList<>();
            Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                rows.add(nextRecord);
            }
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    boolean tableExists = false;
                    Query checkTable = entityManager.createNativeQuery("SHOW TABLES LIKE :tableName");
                    checkTable.setParameter("tableName", tableName);
                    List<Object> tables = checkTable.getResultList();
                    if (!tables.isEmpty()) {
                        tableExists = true;
                    }
                    if (!tableExists) {
                        String createTableQuery = "CREATE TABLE " + tableName + " ("
                                + "id INT NOT NULL AUTO_INCREMENT,"
                                + "PATRON_GROUP_NAME VARCHAR(255),"
                                + "CountOfCHARGE_DATE_ONLY INT,"
                                + "CountOfRENEWAL_COUNT INT,"
                                + "StartDate DATE,"
                                + "EndDate DATE,"
                                + "TITLE VARCHAR(255),"
                                + "CALL_NO VARCHAR(255),"
                                + "AUTHOR VARCHAR(255),"
                                + "PUBLISHER VARCHAR(255),"
                                + "Circulation_Notes VARCHAR(255),"
                                + "PRIMARY KEY (id)"
                                + ")";
                        Query createTable = entityManager.createNativeQuery(createTableQuery);
                        createTable.executeUpdate();
                    }
                    for (String[] row : rows) {
                        String query = "INSERT INTO " + tableName + " (PATRON_GROUP_NAME, CountOfCHARGE_DATE_ONLY, CountOfRENEWAL_COUNT, StartDate, EndDate, TITLE, CALL_NO, AUTHOR, PUBLISHER, Circulation_Notes) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        System.out.println("SQL Query: " + query);
                        Query q = entityManager.createNativeQuery(query);
                        q.setParameter(1, row[0]);
                        q.setParameter(2, Integer.parseInt(row[1]));
                        q.setParameter(3, Integer.parseInt(row[2]));
                        try {
                            q.setParameter(4, dateFormat.parse(row[3]));
                            q.setParameter(5, dateFormat.parse(row[4]));
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                        q.setParameter(6, row[5]);
                        q.setParameter(7, row[6]);
                        q.setParameter(8, row[7]);
                        q.setParameter(9, row[8]);
                        q.setParameter(10, row[9]);
    
                        q.executeUpdate();
                    }
                }
    
            });
            return ResponseEntity.ok("Data uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading data: " + e.getMessage());
        }
    }
     
   
}
