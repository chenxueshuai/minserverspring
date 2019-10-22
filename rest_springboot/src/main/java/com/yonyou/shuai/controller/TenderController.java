package com.yonyou.shuai.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yonyou.shuai.service.imp.TenderDatasource;
import com.yonyou.shuai.vo.Company;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shuai_pc on 2019/10/19.
 */
@RestController
public class TenderController {

    @PostMapping("/tender/upload")
    @ResponseBody
    public String upload(MultipartFile file, String type,String ratioC,String ratioN1, String ratioN2) {
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }
        TenderDatasource tenderDatasource = new TenderDatasource();
        Double c = 0.0;
        Double n1 = 0.0;
        Double n2 = 0.0;
        try {
            c = Double.valueOf(ratioC);
            n1 = Double.valueOf(ratioN1);
            n2 = Double.valueOf(ratioN2);
        }catch (Exception e){
            return "参数格式错误";
        }

        try {
            InputStream inputStream = file.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            for (Row row:sheet) {
                String bao = "";
                String companyName = "";
                Double moneyd = 0.0;

                try {
                    bao = row.getCell(0).getStringCellValue().trim();
                    companyName = row.getCell(1).getStringCellValue().trim();
                    moneyd= row.getCell(2).getNumericCellValue();
                }catch (Exception e){
                    continue;
                }
                String parentId =insertByValue(tenderDatasource.bao,bao);
                String conpanyId =insertByValue(tenderDatasource.company,companyName);
                tenderDatasource.bao.put(parentId,bao);
                tenderDatasource.company.put(conpanyId,companyName);
                Company company = new Company();
                company.setId(conpanyId);
                company.setMoney(moneyd);
                company.setName(companyName);
                company.setParentId(parentId);
                tenderDatasource.dataList.add(company);
            }
        } catch (IOException e) {

        }
        setDataMap(tenderDatasource);
        cleanCompany(tenderDatasource);
        handleCompany1(tenderDatasource,c,n1,n2);
        return JSON.toJSONString(tenderDatasource.datamap);
    }

    private void handleCompany1(TenderDatasource tenderDatasource, Double ratioC, Double ratioN1, Double ratioN2) {
        Iterator<Map.Entry<String, List<Company>>> iterator = tenderDatasource.datamap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, List<Company>> entry = iterator.next();
            String parentId = entry.getKey();
            List<Company> list = entry.getValue();
            Double a1 = getRadioA1(list);
            Double a2 = getRadioA2(a1, list);
            Double a3 = a2 * (1 - ratioC);
            for (Company company:list) {
                company.setA3(a3);
                Double n = company.getMoney() >= a3 ? ratioN1:ratioN2;
                Double total = 100 - (company.getMoney() -a3)/a3*n*100;
                total = total < 0?0:total;
                company.setTotal(total);
            }
        }
    }
    private Double getRadioA1(List<Company> companies){
        Set<Double> set = new TreeSet<Double>();
        for (int i = 0; i < companies.size(); i ++){
            set.add(companies.get(i).getMoney());
        }
        Double[] arr = set.toArray(new Double[0]);
        double sum =0;
        if (arr.length <= 5){
            for (int i =0; i < arr.length; i ++){
                sum += arr[i];
            }
            return sum/arr.length;
        }else if (arr.length <= 10){
            for (int i = 1; i < arr.length -1; i++){
                sum += arr[i];
            }
            return sum/(arr.length-2);
        }else if (arr.length <= 20){
            for (int i = 1; i < arr.length - 2; i ++){
                sum += arr[i];
            }
            return sum/(arr.length - 3);
        }else if (arr.length <= 30){
            for (int i = 2; i < arr.length - 3; i ++){
                sum += arr[i];
            }
            return sum/(arr.length - 5);
        }else {
            for (int i = 3; i < arr.length - 4; i ++){
                sum += arr[i];
            }
            return sum/(arr.length - 7);
        }
    }
    private Double getRadioA2(Double a1, List<Company> companies){
        Set<Double> set = new TreeSet<Double>();
        for (int i = 0; i < companies.size(); i ++){
            set.add(companies.get(i).getMoney());
        }
        Double[] arr = set.toArray(new Double[0]);
        int j = 0;
        Double sum = 0.0;
        for (int i = 0; i < arr.length ; i ++){
            if (arr[i] <= a1*1.1 && arr[i] >= a1 * 0.85){
                j ++;
                sum += arr[i];
            }
        }
        return sum/j;
    }
    private void cleanCompany(TenderDatasource tenderDatasource) {
        ConcurrentHashMap map = tenderDatasource.datamap;
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
             List<Company> companies = (List<Company>) entry.getValue();
             Double avg = getAVG(companies);
            for (int i = 0; i < companies.size(); i ++){
                double v = companies.get(i).getMoney() / avg;
                if (v>7||v<0.2){
                    tenderDatasource.excepList.add(companies.get(i));
                    companies.remove(i);
                }
            }
        }
    }
    private Double getAVG(List<Company> dataList){
        double total = 0;
        for (int i = 0; i<dataList.size(); i ++){
            total += dataList.get(i).getMoney();
        }
        return  total/dataList.size();
    }

    private void setDataMap(TenderDatasource tenderDatasource){
        ConcurrentHashMap map = tenderDatasource.bao;
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            String parentId = (String) entry.getKey();
            List<Company> list = new ArrayList<Company>();
            for (Company company:tenderDatasource.dataList ) {
                if(company.parentId.equals(parentId)){
                    list.add(company);
                }
            }
            tenderDatasource.datamap.put(parentId,list);
        }
    }

    private String insertByValue(ConcurrentHashMap<String, String> map, String value) {
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String val = entry.getValue();
            if (value != null && value.equals(val)){
                return key;
            }
        }
        String id = UUID.randomUUID().toString();
        map.put(id,value);
        return id;
    }
}
