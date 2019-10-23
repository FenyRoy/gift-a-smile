package com.giftsmile.app.smile;

public class ServiceDetails {

    private String Institution;
    private String Phone;
    private String Req;
    private String Type;
    private String Status;

    public ServiceDetails() {

    }

    public ServiceDetails(String institution, String phone, String req, String type, String status) {
        Institution = institution;
        Phone = phone;
        Req = req;
        Type = type;
        Status = status;
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

    public void setType(String type) {
        Type = type;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }


}


