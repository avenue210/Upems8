package cn.upus.app.upems.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

@Entity
public class User {

    @Id
    private Long id;

    @Property(nameInDb = "user_id")
    private String user_id;
    @Property(nameInDb = "finger_id")
    private String finger_id;
    @Property(nameInDb = "finger_lev")
    private String finger_lev;
    @Generated(hash = 1650692330)
    public User(Long id, String user_id, String finger_id, String finger_lev) {
        this.id = id;
        this.user_id = user_id;
        this.finger_id = finger_id;
        this.finger_lev = finger_lev;
    }
    @Generated(hash = 586692638)
    public User() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUser_id() {
        return this.user_id;
    }
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
    public String getFinger_id() {
        return this.finger_id;
    }
    public void setFinger_id(String finger_id) {
        this.finger_id = finger_id;
    }
    public String getFinger_lev() {
        return this.finger_lev;
    }
    public void setFinger_lev(String finger_lev) {
        this.finger_lev = finger_lev;
    }

}
