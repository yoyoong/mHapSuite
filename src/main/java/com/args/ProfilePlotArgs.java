package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class ProfilePlotArgs implements Serializable {
    @Annotation(Constants.BEDPATHS_DESCRIPTION)
    public String bedPaths = "";
    @Annotation(Constants.BIGWIG_DESCRIPTION)
    public String bigwig;
    @Annotation(Constants.TAG_DESCRIPTION)
    public String tag = "";
    @Annotation(Constants.UPLENGTH_DESCRIPTION)
    public Integer upLength = 2000;
    @Annotation(Constants.DOWNLENGTH_DESCRIPTION)
    public Integer downLength = 2000;
    @Annotation(Constants.WINDOWNUM_DESCRIPTION)
    public Integer windowNum = 10;
    @Annotation(Constants.OUTFORMAT_DESCRIPTION)
    public String outFormat = "pdf";

    public String getBedPaths() {
        return bedPaths;
    }

    public void setBedPaths(String bedPaths) {
        this.bedPaths = bedPaths;
    }

    public String getBigwig() {
        return bigwig;
    }

    public void setBigwig(String bigwig) {
        this.bigwig = bigwig;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Integer getUpLength() {
        return upLength;
    }

    public void setUpLength(Integer upLength) {
        this.upLength = upLength;
    }

    public Integer getDownLength() {
        return downLength;
    }

    public void setDownLength(Integer downLength) {
        this.downLength = downLength;
    }

    public Integer getWindowNum() {
        return windowNum;
    }

    public void setWindowNum(Integer windowNum) {
        this.windowNum = windowNum;
    }

    public String getOutFormat() {
        return outFormat;
    }

    public void setOutFormat(String outFormat) {
        this.outFormat = outFormat;
    }
}
