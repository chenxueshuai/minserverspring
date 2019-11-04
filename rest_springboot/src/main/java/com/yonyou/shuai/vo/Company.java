package com.yonyou.shuai.vo;

/**
 * Created by shuai_pc on 2019/10/19.
 */
public class Company  implements Comparable<Company>{

    private String id;
    private String name;
    private Double money;
    public String parentId;
    // 基准价A3
    public Double a3;
    public Double total;
    // 排序号
    private int index;
    private Company company;

    public String getId() {
        return id;
    }

    public Double getA3() {
        return a3;
    }

    public void setA3(Double a3) {
        this.a3 = (double) Math.round(a3 * 10000) / 10000;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = (double) Math.round(total * 10000) / 10000;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        if (!id.equals(company.id)) return false;
        if (!name.equals(company.name)) return false;
        if (!money.equals(company.money)) return false;
        return parentId.equals(company.parentId);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + money.hashCode();
        result = 31 * result + parentId.hashCode();
        return result;
    }

    @Override
    public int compareTo(Company company) {
        return (this.getTotal() > company.getTotal() ? -1 :
                (this.getTotal() == company.getTotal() ? 0 : 1));
    }
}
