package cn.upus.app.upems.bean;

/**
 * 登录信息
 * Created by computer on 2018-04-03.
 */

public class LoginDataEntity {

    /**
     * roleno : 220 //角色代码 （403快递员）
     * logno : ADMIN
     * cocode : ZY
     * ServUrl : http://web.upus.wang:8081/UpService.asmx
     * jeeurl : http://web.upus.wang:8091/
     * userlev : 1
     * rolcode : ADMIN
     * compno : 87690001
     * branno : 001
     * userna : 亚普达
     * manno : 00001
     * codeName : eis
     * pwd : ?
     */

    private int roleno;
    private String logno;
    private String cocode;
    private String ServUrl;
    private String jeeurl;
    private int userlev;
    private String rolcode;
    private String compno;
    private String branno;
    private String userna;
    private String manno;
    private String codeName;
    private String pwd;

    private String custno;//服务商编号

    public String getCustno() {
        return custno;
    }

    public void setCustno(String custno) {
        this.custno = custno;
    }

    public int getRoleno() {
        return roleno;
    }

    public void setRoleno(int roleno) {
        this.roleno = roleno;
    }

    public String getLogno() {
        return logno;
    }

    public void setLogno(String logno) {
        this.logno = logno;
    }

    public String getCocode() {
        return cocode;
    }

    public void setCocode(String cocode) {
        this.cocode = cocode;
    }

    public String getServUrl() {
        return ServUrl;
    }

    public void setServUrl(String ServUrl) {
        this.ServUrl = ServUrl;
    }

    public String getJeeurl() {
        return jeeurl;
    }

    public void setJeeurl(String jeeurl) {
        this.jeeurl = jeeurl;
    }

    public int getUserlev() {
        return userlev;
    }

    public void setUserlev(int userlev) {
        this.userlev = userlev;
    }

    public String getRolcode() {
        return rolcode;
    }

    public void setRolcode(String rolcode) {
        this.rolcode = rolcode;
    }

    public String getCompno() {
        return compno;
    }

    public void setCompno(String compno) {
        this.compno = compno;
    }

    public String getBranno() {
        return branno;
    }

    public void setBranno(String branno) {
        this.branno = branno;
    }

    public String getUserna() {
        return userna;
    }

    public void setUserna(String userna) {
        this.userna = userna;
    }

    public String getManno() {
        return manno;
    }

    public void setManno(String manno) {
        this.manno = manno;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
