package com.hipicon.casestudy.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;

//    public String getUsername() {
//        return username;
//    }
//    public void setUsername(String username) {this.username = username;}
}
