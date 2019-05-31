package com.cnebula.nature.entity;

import lombok.Builder;
import lombok.Data;

@Data
public class Institution {

    private String orgDivision = "";
    private String orgName = "";
    private String city = "";
    private String state = "";
    private String country = "";
    //private String email = "";

    public Institution() {
    }

    @Override
    public String toString() {
        return this.orgDivision + " " + this.orgName + " " + this.city + " " + this.state + " " + this.country;
    }
}
