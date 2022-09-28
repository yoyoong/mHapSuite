package com.args;

import java.io.Serializable;

public class GenomeWideArgs implements Serializable {
    public String tag = ""; // prefix of the output file(s)
    public String mhapPath = ""; // input file,mhap.gz format,generated by mHapTools and indexed
    public String cpgPath = ""; // genomic CpG file, gz format and indexed
    public String metrics = ""; // mHap-level metrics,including MM,PDR,CHALM,MHL,MCR,MBS,Entropy,and R2
    public String outputDir = ""; // output directory, created in advance
    public Integer minK = 1; // minimum k-mer length for MHL [1]
    public Integer maxK = 10; // maximum k-mer length for MHL [10]
    public Integer K = 4; // k-mer length for entropy, PDR, and CHALM, can be 3, 4, or 5 [4]
    public String strand = "both"; // plus,minus,both [both]
    public String region = ""; // one region, in the format of chr:start-end
    public String bedFile = ""; // a bed file
    public Integer cpgCov = 5; // minimal number of CpG coverage for MM calculation [5]
    public Integer r2Cov = 20; // minimal number of reads that cover two CpGs for R2 calculation [20]
    public Integer k4Plus = 5; // minimal number of reads that cover 4 or more CpGs for PDR, CHALM, MHL, MCR, MBS and Entropy [5]

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

    public String getBedFile() {
        return bedFile;
    }

    public void setBedFile(String bedFile) {
        this.bedFile = bedFile;
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
