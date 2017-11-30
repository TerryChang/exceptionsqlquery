package com.terry.mybatis.mybatis;

import org.springframework.dao.DataAccessException;

public class CustomDataAccessException extends DataAccessException {
    String query;

    public CustomDataAccessException(String msg){
        super(msg);
    }

    public CustomDataAccessException(String msg, String query){
        this(msg);
        this.query = query;
    }

    public CustomDataAccessException(String msg, Throwable cause){
        super(msg,cause);
    }

    public CustomDataAccessException(String msg, Throwable cause, String query){
        this(msg,cause);
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
