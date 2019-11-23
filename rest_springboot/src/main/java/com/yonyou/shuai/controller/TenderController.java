package com.yonyou.shuai.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yonyou.shuai.service.imp.TenderDatasource;
import com.yonyou.shuai.vo.Company;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * Created by shuai_pc on 2019/10/19.
 */
@RestController
public class TenderController {

    @PostMapping("/tender/upload")
    @ResponseBody
    public String upload(MultipartFile file, String type,String ratioC,String ratioN1, String ratioN2) {
        if (file.isEmpty()) {
            return "上传失败，请选择文件" ;
        }
        TenderDatasource tenderDatasource = new TenderDatasource();
        Double c = 0.0;
        Double n1 = 0.0;
        Double n2 = 0.0;
        try {
            c = Double.valueOf(ratioC)/100;
            n1 = Double.valueOf(ratioN1);
            n2 = Double.valueOf(ratioN2);
        }catch (Exception e){
            return "参数格式错误";
        }
        Workbook workbook = null;
        long a = System.currentTimeMillis();
        try {
            InputStream inputStream = file.getInputStream();
            if(file.getOriginalFilename().split("\\.")[1].equalsIgnoreCase("xlsx")){
                workbook = new XSSFWorkbook(inputStream);
            }else if(file.getOriginalFilename().split("\\.")[1].equalsIgnoreCase("xls")){
                workbook = new HSSFWorkbook(inputStream);
            }else{
                return "文件类型不支持";
            }
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row:sheet) {
                String bao = "";
                String companyName = "";
                Double moneyd = 0.0;

                try {
                    bao = row.getCell(0).getStringCellValue().trim();
                    companyName = row.getCell(1).getStringCellValue().trim();
                    moneyd= row.getCell(2).getNumericCellValue();
                    // 报价为0 清除数据
                    if(moneyd == 0.0){
                        System.out.println("清除 为 0 的数据");
                        continue;
                    }
                }catch (Exception e){
                    System.out.println("清除 报错 的数据");
                    continue;
                }
                String parentId =insertByValueBao(tenderDatasource.bao,bao);
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
        System.out.println(System.currentTimeMillis() - a);
        a = System.currentTimeMillis();
        setDataMap(tenderDatasource);
        cleanCompany(tenderDatasource);
        handleCompany1(tenderDatasource,c,n1,n2);
        sortSetIndex(tenderDatasource);
        JSONObject res = new JSONObject();
        res.put("bao",tenderDatasource.bao);
        res.put("datamap",JSON.toJSON(tenderDatasource.datamap));
        res.put("company",JSON.toJSON(tenderDatasource.company));
        res.put("excepList",JSON.toJSON(tenderDatasource.excepList));
        System.out.println(System.currentTimeMillis() - a);
        return JSON.toJSONString(res);
    }
    @PostMapping(value = "tender/export")
    @ResponseBody
    public void  download(HttpServletResponse response, @RequestBody JSONArray jsonArray) {
        try{
            File file = ResourceUtils.getFile("excleTemplate/frameExl.xlsx");
            ClassPathResource classPathResource = new ClassPathResource("excleTemplate/frameExl.xlsx");
            InputStream inputStream =classPathResource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            FileOutputStream outputStream = org.apache.commons.io.FileUtils.openOutputStream(file);
            //workbook.write(outputStream);
            outputStream.close();
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i <jsonArray.size(); i ++){
                Map item = (Map) jsonArray.get(i);
                Row row = sheet.createRow(i + 2);
                row.createCell(0).setCellValue(i+1+"");
                row.createCell(1).setCellValue((String)item.get("parentId"));
                row.createCell(2).setCellValue((String)item.get("name"));
                row.createCell(3).setCellValue((Double)item.get("money"));
                row.createCell(4).setCellValue((Double)item.get("a3"));
                row.createCell(5).setCellValue((item.get("total") instanceof Double)? (Double)item.get("total"):(Integer)item.get("total"));
                row.createCell(6).setCellValue((Integer) item.get("index"));
                System.out.print((Integer) item.get("index"));
            }
            response.addHeader("Content-Type", "application/octet-stream;charset=UTF-8");
            response.addHeader("Content-Disposition", "attachment;filename=" +java.net.URLEncoder.encode( UUID.randomUUID() + ".xlsx"));
            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            workbook.write(out);
            out.close();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    private void sortSetIndex(TenderDatasource tenderDatasource) {
        Iterator<Map.Entry<String, List<Company>>> iterator = tenderDatasource.datamap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Company>> entry = iterator.next();
            String parentId = entry.getKey();
            List<Company> list = entry.getValue();
            Collections.sort(list);
            for (int i = 0; i < list.size(); i ++){
                list.get(i).setIndex(i+1);
            }
        }
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
                Double total = 100 - Math.abs(company.getMoney() -a3)/a3*n*100;
                total = total < 0?0:total;
                company.setTotal(total);
            }
        }
    }
    private Double getRadioA1(List<Company> companies){
        List<Double> list = new LinkedList<Double>();
        for (int i = 0; i < companies.size(); i ++){
            list.add(companies.get(i).getMoney());
        }
        Double[] arr = list.toArray(new Double[0]);
        Arrays.sort(arr);
        double sum =0;
        arr = getNewArr(arr);
        for (int i = 0 ; i < arr.length; i ++){
            sum += arr[i];
        }
        return sum/arr.length;
    }

    private Double[] getNewArr(Double[] arr) {
        List<Double> templist = new LinkedList<Double>();
        if (arr.length <= 5){
            for (int i =0; i < arr.length; i ++){
                templist.add(arr[i]);
            }
        }else if (arr.length <= 10){
            for (int i = 1; i < arr.length -1; i++){
                templist.add(arr[i]);
            }
        }else if (arr.length <= 20){
            for (int i = 1; i < arr.length - 2; i ++){
                templist.add(arr[i]);
            }
        }else if (arr.length <= 30){
            for (int i = 2; i < arr.length - 3; i ++){
                templist.add(arr[i]);
            }
        }else {
            for (int i = 3; i < arr.length - 4; i ++){
                templist.add(arr[i]);
            }
        }
        arr = templist.toArray(new Double[0]);
        Arrays.sort(arr);
        return arr;
    }

    private Double getRadioA2(Double a1, List<Company> companies){
        List<Double> list = new LinkedList<Double>();
        for (int i = 0; i < companies.size(); i ++){
            list.add(companies.get(i).getMoney());
        }
        Double[] arr = list.toArray(new Double[0]);
        Arrays.sort(arr);
        arr = getNewArr(arr);
        int j = 0;
        Double sum = 0.0;
        for (int i = 0; i < arr.length ; i ++){
            if (arr[i] <= a1*1.1 && arr[i] >= a1 * 0.85){
                j ++;
                sum += arr[i];
            }else{
                System.out.println(arr[i]);
            }
        }
        return sum/j;
    }
    private void cleanCompany(TenderDatasource tenderDatasource) {
        TreeMap map = tenderDatasource.datamap;
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
        TreeMap map = tenderDatasource.bao;
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

    private String insertByValueBao(TreeMap<String, String> map, String value) {
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        String id = "";
        while (iterator.hasNext()){
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String val = entry.getValue();
            if (value != null && value.equals(val)){
                return key;
            }
        }
        id = value.trim().replace("包","");
        id = id.length() == 1 ? "0"+id :id;
        map.put(id,value);
        return id;
    }
    private String insertByValue(TreeMap<String, String> map, String value) {
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
