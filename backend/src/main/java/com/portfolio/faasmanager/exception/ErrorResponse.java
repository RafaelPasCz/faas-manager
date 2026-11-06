package com.portfolio.faasmanager.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private LocalDateTime timeStamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse(int status, String error, String message, String path) {
        this.timeStamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimeStamp(){
        return timeStamp;
    }

    public int getStatus(){
        return status;
    }

    public String getError(){
        return error;
    }

    public String getMessage(){
        return message;
    }

    public String getPath(){
        return path;
    }
    
    public void setStatus(int newStatus){
        this.status = newStatus;
    }

    public void setTimeStamp(LocalDateTime newTimeStamp){
        this.timeStamp = newTimeStamp;

    }

    public void setError(String newError){
        this.error = newError;
    }

    public void setMessage(String newMessage){
        this.message = newMessage;
    }

    public void setPath(String newPath){
        this.path = newPath;
    }
}