package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class EnrichmentPlotArgs implements Serializable {
    @Annotation(Constants.BEDPATHS_DESCRIPTION)
    public String bedPaths = "";
    @Annotation(Constants.OPENCHROMATIN_DESCRIPTION)
    public String openChromatin = "";
    @Annotation(Constants.BIGWIG_DESCRIPTION)
    public String bigwig = "";
    @Annotation(Constants.GROUPNUM_DESCRIPTION)
    public Integer groupNum = 10;
    @Annotation(Constants.GROUPCUTOFF_DESCRIPTION)
    public Integer groupCutoff = 100;
    @Annotation(Constants.TAG_DESCRIPTION)
    public String tag = "";
    @Annotation(Constants.OUTFORMAT_DESCRIPTION)
    public String outFormat = "pdf";

    public String getBedPaths() {
        return bedPaths;
    }

    public void setBedPaths(String bedPaths) {
        this.bedPaths = bedPaths;
    }

    public String getOpenChromatin() {
        return openChromatin;
    }

    public void setOpenChromatin(String openChromatin) {
        this.openChromatin = openChromatin;
    }

    public String getBigwig() {
        return bigwig;
    }

    public void setBigwig(String bigwig) {
        this.bigwig = bigwig;
    }

    public Integer getGroupNum() {
        return groupNum;
    }

    public void setGroupNum(Integer groupNum) {
        this.groupNum = groupNum;
    }

    public Integer getGroupCutoff() {
        return groupCutoff;
    }

    public void setGroupCutoff(Integer groupCutoff) {
        this.groupCutoff = groupCutoff;
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
