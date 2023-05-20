package me.lab7.common;

import me.lab7.common.exception.MustBeNotEmptyException;
import me.lab7.common.exception.RangeException;

import java.io.Serializable;

public class Authentication implements Serializable {
    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setUserName(String userName) throws RangeException, MustBeNotEmptyException {
        if (userName.length() > 127) throw new RangeException();
        if (userName.equals("")) throw new MustBeNotEmptyException();
        this.userName = userName;

    }

    public void setPassword(String userPassword) throws MustBeNotEmptyException, RangeException {
        this.password = userPassword;
        if(password.equals("")) throw new MustBeNotEmptyException();
        if (password.length() !=8) throw new RangeException();
    }
}
