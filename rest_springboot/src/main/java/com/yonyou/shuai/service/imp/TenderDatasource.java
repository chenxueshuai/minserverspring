package com.yonyou.shuai.service.imp;

import com.yonyou.shuai.vo.Company;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shuai_pc on 2019/10/19.
 */
public class TenderDatasource {
    // id  .... bao 名
    public  TreeMap<String, String> bao = new TreeMap<String, String>();
    // id .......公司名
    public  TreeMap<String, String> company = new TreeMap<String, String>();
    //baoid .....多个公司   JSONObject  companyid  去除空数据
    public  List<Company> dataList = new ArrayList<Company>();
    //   去除错误数据
    public  TreeMap<String, List<Company>> datamap = new TreeMap();

    public  List<Company> excepList = new ArrayList<Company>();
}
