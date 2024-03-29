package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class MHBDiscoveryArgs implements Serializable {
    @Annotation(Constants.MHAPPATH_DESCRIPTION)
    public String mhapPath = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String cpgPath = "";
    @Annotation(Constants.REGION_DESCRIPTION)
    public String region = "";
    @Annotation(Constants.BEDPATH_DESCRIPTION)
    public String bedPath = "";
    @Annotation(Constants.WINDOW_DESCRIPTION + " [5]")
    public Integer window = 5;
    @Annotation(Constants.R2_DESCRIPTION + " [0.5]")
    public Double r2 = 0.5;
    @Annotation(Constants.PVALUE_DESCRIPTION + " [0.05]")
    public Double pvalue = 0.05;
    @Annotation(Constants.OUTPUTDIR_DESCRIPTION)
    public String outputDir = "";
    @Annotation(Constants.TAG_DESCRIPTION + ", default: MHBDiscovery_out")
    public String tag = "MHBDiscovery_out";
//    @Annotation(Constants.QCFLAG_DESCRIPTION)
//    public boolean qcFlag = false;

    public String getMhapPath() {
        return mhapPath;
    }

    public void setMhapPath(String mhapPath) {
        this.mhapPath = mhapPath;
    }

    public String getBedPath() {
        return bedPath;
    }

    public void setBedPath(String bedPath) {
        this.bedPath = bedPath;
    }

    public String getCpgPath() {
        return cpgPath;
    }

    public void setCpgPath(String cpgPath) {
        this.cpgPath = cpgPath;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getWindow() {
        return window;
    }

    public void setWindow(Integer window) {
        this.window = window;
    }

    public Double getR2() {
        return r2;
    }

    public void setR2(Double r2) {
        this.r2 = r2;
    }

    public Double getPvalue() {
        return pvalue;
    }

    public void setPvalue(Double pvalue) {
        this.pvalue = pvalue;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

//    public boolean isQcFlag() {
//        return qcFlag;
//    }
//
//    public void setQcFlag(boolean qcFlag) {
//        this.qcFlag = qcFlag;
//    }
}
