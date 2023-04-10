package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class ScatterPlotArgs implements Serializable {
    @Annotation(Constants.BEDPATH_DESCRIPTION)
    public String bedPath = "";
    @Annotation(Constants.BIGWIG1_DESCRIPTION)
    public String bigwig1;
    @Annotation(Constants.BIGWIG2_DESCRIPTION)
    public String bigwig2;
    @Annotation(Constants.TAG_DESCRIPTION)
    public String tag = "";
    @Annotation(Constants.OUTFORMAT_DESCRIPTION)
    public String outFormat = "pdf";

    public String getBedPath() {
        return bedPath;
    }

    public void setBedPath(String bedPath) {
        this.bedPath = bedPath;
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

}
