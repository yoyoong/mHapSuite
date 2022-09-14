package com.bean;

import java.io.Serializable;

public class R2Info implements Serializable {
    public String chrom;
    public Integer start;
    public Integer end;
    public Integer n00;
    public Integer n01;
    public Integer n10;
    public Integer n11;
    public Double r2;
    public Double pvalue;

    public String getChrom() {
        return chrom;
    }

    public void setChrom(String chrom) {
        this.chrom = chrom;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getN00() {
        return n00;
    }

    public void setN00(Integer n00) {
        this.n00 = n00;
    }

    public Integer getN01() {
        return n01;
    }

    public void setN01(Integer n01) {
        this.n01 = n01;
    }

    public Integer getN10() {
        return n10;
    }

    public void setN10(Integer n10) {
        this.n10 = n10;
    }

    public Integer getN11() {
        return n11;
    }

    public void setN11(Integer n11) {
        this.n11 = n11;
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
}
