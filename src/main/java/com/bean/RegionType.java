package com.bean;

public enum RegionType {
    SINGLE_REGION("SINGLE_REGION",0),
    MULTI_REGION("MULTI_REGION",1),
    WHOLE_FILE("WHOLE_FILE", 2);

    private final String RegionType;
    private final Integer RegionFlag;

    private RegionType(String RegionType, Integer RegionFlag){
        this.RegionType = RegionType;
        this.RegionFlag = RegionFlag;
    }

    public String getRegionType() {
        return RegionType;
    }

    public Integer getRegionFlag() {
        return RegionFlag;
    }
}
