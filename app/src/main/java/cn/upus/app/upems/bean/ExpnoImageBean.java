package cn.upus.app.upems.bean;

import java.util.List;

/**
 * 存取 上传的附件列表
 */
public class ExpnoImageBean {

    private String type;
    private List<String> expnos;

    public ExpnoImageBean() {
    }

    public ExpnoImageBean(String type, List<String> expnos) {
        this.type = type;
        this.expnos = expnos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getExpnos() {
        return expnos;
    }

    public void setExpnos(List<String> expnos) {
        this.expnos = expnos;
    }
}
