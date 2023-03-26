package com.example.demo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.RequestJson;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import jakarta.persistence.*;
@CrossOrigin
@RestController


public class Controller
{
    @Autowired
    private EntityManager entityManager;
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
}
