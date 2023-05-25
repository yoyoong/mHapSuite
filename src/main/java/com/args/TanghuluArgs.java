package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class TanghuluArgs implements Serializable {
    @Annotation(Constants.MHAPPATH_DESCRIPTION)
    public String mhapPath = "";
    @Annotation(Constants.MHAPPATH_DESCRIPTION)
    public String cpgPath = ""; 
    @Annotation(Constants.MHAPPATH_DESCRIPTION)
    public String region = ""; 
    @Annotation(Constants.TAG_DESCRIPTION)
    public String tag = "";
    @Annotation(Constants.OUTFORMAT_DESCRIPTION)
    public String outFormat = "pdf"; 
    @Annotation(Constants.STRAND_DESCRIPTION)
    public String strand = "both"; 
    @Annotation(Constants.MAXREADS_DESCRIPTION)
    public Integer maxReads = 50; 
    @Annotation(Constants.MAXLENGTH_DESCRIPTION)
    public Integer maxLength = 2000; 
    @Annotation(Constants.MERGE_DESCRIPTION)
    public Boolean merge = false; 
    @Annotation(Constants.SIMULATION_DESCRIPTION)
    public Boolean simulation = false; 
    @Annotation(Constants.CUTREADS_DESCRIPTION)
    public Boolean cutReads = false; 

    public String getMhapPath() {
        return mhapPath;
    }

    public void setMhapPath(String mhapPath) {
        this.mhapPath = mhapPath;
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

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public Integer getMaxReads() {
        return maxReads;
    }

    public void setMaxReads(Integer maxReads) {
        this.maxReads = maxReads;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Boolean getMerge() {
        return merge;
    }

    public void setMerge(Boolean merge) {
        this.merge = merge;
    }

    public Boolean getSimulation() {
        return simulation;
    }

    public void setSimulation(Boolean simulation) {
        this.simulation = simulation;
    }

    public Boolean getCutReads() {
        return cutReads;
    }

    public void setCutReads(Boolean cutReads) {
        this.cutReads = cutReads;
    }

}
