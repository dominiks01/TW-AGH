package org.example;

public enum MessageType {
    PRODUCER_REQUEST,
    CONSUMER_REQUEST,
    PRODUCER_RESPONSE,
    CONSUMER_RESPONSE,
    TOKEN;

    public MessageType getResponse(){
        return switch (this){
            case PRODUCER_RESPONSE -> PRODUCER_REQUEST;
            case CONSUMER_RESPONSE -> CONSUMER_REQUEST;
            case TOKEN -> TOKEN;
            case CONSUMER_REQUEST ->  CONSUMER_RESPONSE;
            case PRODUCER_REQUEST -> PRODUCER_RESPONSE;
        };
    }
}
