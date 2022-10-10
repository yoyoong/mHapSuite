package com.bean;

public enum StrandType {
    UNKNOWN("UNKNOWN","*"),
    MINUS("MINUS","-"),
    PLUS("PLUS", "+");

    private final String StrandType;
    private final String StrandFlag;

    private StrandType(String StrandType, String StrandFlag){
        this.StrandType = StrandType;
        this.StrandFlag = StrandFlag;
    }

    public String getStrandType() {
        return StrandType;
    }

    public String getStrandFlag() {
        return StrandFlag;
    }
}
