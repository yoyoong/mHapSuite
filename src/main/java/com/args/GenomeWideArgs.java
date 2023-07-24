package com.args;

import com.common.Annotation;
import com.common.Constants;

import java.io.Serializable;

public class GenomeWideArgs implements Serializable {
    @Annotation(Constants.TAG_DESCRIPTION + ", default: genomeWide_out")
    public String tag = "genomeWide_out";
    @Annotation(Constants.MHAPPATH_DESCRIPTION)
    public String mhapPath = "";
    @Annotation(Constants.CPGPATH_DESCRIPTION)
    public String cpgPath = "";
    @Annotation(Constants.METRICS_DESCRIPTION)
    public String metrics = "";
    @Annotation(Constants.OUTPUTDIR_DESCRIPTION)
    public String outputDir = "";
    @Annotation(Constants.MINK_DESCRIPTION)
    public Integer minK = 1;
    @Annotation(Constants.MAXK_DESCRIPTION)
    public Integer maxK = 10;
    @Annotation(Constants.K_DESCRIPTION)
    public Integer K = 4;
    @Annotation(Constants.STRAND_DESCRIPTION)
    public String strand = "both";
    @Annotation(Constants.REGION_DESCRIPTION)
    public String region = "";
    @Annotation(Constants.BEDPATH_DESCRIPTION)
    public String bedPath = "";
    @Annotation(Constants.CPGCOV_DESCRIPTION)
    public Integer cpgCov = 10;
    @Annotation(Constants.R2COV_DESCRIPTION)
    public Integer r2Cov = 20;
    @Annotation(Constants.K4PLUS_DESCRIPTION)
    public Integer k4Plus = 10;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

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

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public Integer getMinK() {
        return minK;
    }

    public void setMinK(Integer minK) {
        this.minK = minK;
    }

    public Integer getMaxK() {
        return maxK;
    }

    public void setMaxK(Integer maxK) {
        this.maxK = maxK;
    }

    public Integer getK() {
        return K;
    }

    public void setK(Integer k) {
        K = k;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBedPath() {
        return bedPath;
    }

    public void setBedPath(String bedPath) {
        this.bedPath = bedPath;
    }

    public Integer getCpgCov() {
        return cpgCov;
    }

    public void setCpgCov(Integer cpgCov) {
        this.cpgCov = cpgCov;
    }

    public Integer getR2Cov() {
        return r2Cov;
    }

    public void setR2Cov(Integer r2Cov) {
        this.r2Cov = r2Cov;
    }

    public Integer getK4Plus() {
        return k4Plus;
    }

    public void setK4Plus(Integer k4Plus) {
        this.k4Plus = k4Plus;
    }

}
