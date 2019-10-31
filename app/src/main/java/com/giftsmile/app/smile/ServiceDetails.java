package com.giftsmile.app.smile;

public class ServiceDetails {

    private String Key;
    private String Institution;
    private String Phone;
    private String Req;
    private String Type;
    private String Status;
    private String InstId;
    private String Uid;
    private String Uname;
    private String Unumber;


    public ServiceDetails(String key,String institution, String phone, String req, String type, String status,String instId,String uid,String uname,String unumber) {
        Key = key;
        Institution = institution;
        Phone = phone;
        Req = req;
        Type = type;
        Status = status;
        InstId = instId;
        Uid = uid;
        Uname = uname;
        Unumber = unumber;

    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public String getInstitution() {
        return Institution;
    }


    public void setInstitution(String institution) {
        Institution = institution;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getReq() {
        return Req;
    }

    public void setReq(String req) {
        Req = req;
    }

    public String getType() {
        return Type;
    }

    public String getInstId() {
        return InstId;
    }

    public void setInstId(String instId) {
        InstId = instId;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getStatus() {
        return Status;
    }

    public String getUname() {
        return Uname;
    }

    public void setUname(String uname) {
        Uname = uname;
    }

    public String getUnumber() {
        return Unumber;
    }

    public void setUnumber(String unumber) {
        Unumber = unumber;
    }

    public void setStatus(String status) {
        Status = status;
    }


}


