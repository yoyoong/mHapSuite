package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class BoxPlotArgs implements Serializable {
    @Annotation(Constants.BEDPATH_DESCRIPTION)
    public String bedPath = "";
    @Annotation(Constants.BIGWIGS_DESCRIPTION)
    public String bigwigs;
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

    public String getBigwigs() {
        return bigwigs;
    }

    public void setBigwigs(String bigwigs) {
        this.bigwigs = bigwigs;
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
