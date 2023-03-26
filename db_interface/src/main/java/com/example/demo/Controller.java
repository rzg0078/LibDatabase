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
    //if you want to include any file name possibilities do it here
    public String fileNameConnector = "_circ_stats_";
    public ArrayList<String> semesters = new ArrayList<>(Arrays.asList("fall", "spring" , "summer"));
    public ArrayList<String> subParts = new ArrayList<>(Arrays.asList("microfilm", "miscellaneous" , "traditional","nocall","micro"));
    public ArrayList<Integer> years = new ArrayList<>(Arrays.asList(2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021));
    //This is data retrival it is a get request only but given postmapping in order to accomidate request body from Front End
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
  //This is to upload CSV file
    @PostMapping(path = "/uploadData")
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> uploadData(@RequestParam("file") MultipartFile file) {
        try {
            boolean flag = false;
            String fileName = file.getOriginalFilename();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            System.out.println("------------");
            System.out.println(fileName);
            String[] fileNameSplit = fileName.split("_");    
            // file name format only works for format "Semester_SubPart_year"
            
            if(fileNameSplit.length == 3)
            {
                if(semesters.contains(fileNameSplit[0].toLowerCase()) && subParts.contains(fileNameSplit[1].toLowerCase())
                && years.contains(Integer.parseInt(fileNameSplit[2])))
                {
                    flag = true;
                }
            }
            if(flag)
            {
                String tableName = fileNameSplit[0].toLowerCase() + fileNameConnector + (fileNameSplit[1].toLowerCase().equals("micro") ? "microfilm" : fileNameSplit[1].toLowerCase()) +
                "_" + fileNameSplit[2];
                System.out.println(fileName);
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
                                    + "TITLE TEXT,"
                                    + "CALL_NO CHAR(255),"
                                    + "AUTHOR TEXT,"
                                    + "PUBLISHER TEXT,"
                                    + "Circulation_Notes TEXT,"
                                    + "PRIMARY KEY (id)"
                                    + ")";
                            Query createTable = entityManager.createNativeQuery(createTableQuery);
                            createTable.executeUpdate();
                        }
                        //If any date format errors check here
                        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM/dd/yyyy");
                        SimpleDateFormat dateFormat3 = new SimpleDateFormat("dd/MM/yyyy");
                        for (String[] row : rows) {
                            String query = "INSERT INTO " + tableName + " (PATRON_GROUP_NAME, CountOfCHARGE_DATE_ONLY, CountOfRENEWAL_COUNT, StartDate, EndDate, TITLE, CALL_NO, AUTHOR, PUBLISHER, Circulation_Notes) "
                                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                            System.out.println("SQL Query: " + query);
                            Query q = entityManager.createNativeQuery(query);
                            q.setParameter(1, row[0]);
                            q.setParameter(2, Integer.parseInt(row[1]));
                            q.setParameter(3, Integer.parseInt(row[2]));
                            Date startDate = null;
                            Date endDate = null;
                            for (SimpleDateFormat dateFormat : Arrays.asList(dateFormat1, dateFormat2, dateFormat3)) {
                                try {
                                    startDate = dateFormat.parse(row[3]);
                                    endDate = dateFormat.parse(row[4]);
                                    break;
                                } catch (ParseException e) {
                                    // Ignoring the exception and trying the next date format
                                }
                            }
                            q.setParameter(4, startDate);
                            q.setParameter(5, endDate);
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
            }
            else
            {
                throw new Exception("pleae check file name it should be in format Semester_SubPart_year");
            }               
            } catch (Exception e) 
            {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading data: " + e.getMessage());
        }
    }
     
   
}
