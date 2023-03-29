package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class ProfileViewArgs implements Serializable {
    @Annotation(Constants.BEDPATHS_DESCRIPTION)
    public String bedPaths = "";
    @Annotation(Constants.BIGWIG_DESCRIPTION)
    public String bigwig;
    @Annotation(Constants.TAG_DESCRIPTION)
    public String tag = "";
    @Annotation(Constants.UPLENGTH_DESCRIPTION)
    public Integer upLength = 20000;
    @Annotation(Constants.DOWNLENGTH_DESCRIPTION)
    public Integer downLength = 20000;
    @Annotation(Constants.WINDOW2_DESCRIPTION)
    public Integer window = 1000;
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

    public Integer getWindow() {
        return window;
    }

    public void setWindow(Integer window) {
        this.window = window;
    }

    public String getOutFormat() {
        return outFormat;
    }

    public void setOutFormat(String outFormat) {
        this.outFormat = outFormat;
    }
}
