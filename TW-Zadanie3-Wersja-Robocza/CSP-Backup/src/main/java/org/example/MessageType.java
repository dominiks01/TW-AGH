package org.example;

public enum MessageType {
    ORDER_POST,
    ORDER_GET,
    RESPONSE_POST,
    RESPONSE_GET,
    TOKEN;

    public MessageType getResponse(){
        return switch (this){
            case RESPONSE_POST -> ORDER_POST;
            case RESPONSE_GET -> ORDER_GET;
            case TOKEN -> TOKEN;
            case ORDER_GET ->  RESPONSE_GET;
            case ORDER_POST -> RESPONSE_POST;
        };
    }
}
