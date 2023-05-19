package me.lab7.common;

import java.io.Serializable;

public class ResponseWithBooleanType implements Serializable {
    private final String response;
    private final boolean auth;
    public ResponseWithBooleanType(String response, boolean auth) {
        this.response = response;
        this.auth = auth;
    }

    public String getResponse() {
        return response;
    }
    public boolean getAuth(){
        return auth;
    }
}
