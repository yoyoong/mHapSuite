package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class ScatterViewArgs implements Serializable {
    @Annotation(Constants.REGION_DESCRIPTION)
    public String region = "";
    @Annotation(Constants.BIGWIG1_DESCRIPTION)
    public String bigwig1;
    @Annotation(Constants.BIGWIG2_DESCRIPTION)
    public String bigwig2;
    @Annotation(Constants.TAG_DESCRIPTION)
    public String tag = "";
    @Annotation(Constants.OUTFORMAT_DESCRIPTION)
    public String outFormat = "pdf";
    @Annotation(Constants.SAMPLEFLAG_DESCRIPTION)
    public boolean sampleFlag = true;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBigwig1() {
        return bigwig1;
    }

    public void setBigwig1(String bigwig1) {
        this.bigwig1 = bigwig1;
    }

    public String getBigwig2() {
        return bigwig2;
    }

    public void setBigwig2(String bigwig2) {
        this.bigwig2 = bigwig2;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getOutFormat() {
        return outFormat;
    }

    public void setOutFormat(String outFormat) {
        this.outFormat = outFormat;
    }

    public boolean isSampleFlag() {
        return sampleFlag;
    }

    public void setSampleFlag(boolean sampleFlag) {
        this.sampleFlag = sampleFlag;
    }

}
