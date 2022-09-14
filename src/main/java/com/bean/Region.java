package com.bean;

import java.io.Serializable;

public class Region implements Serializable {
    public String chrom;
    public Integer start;
    public Integer end;

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

    public String toFileString() { return this.chrom + "_" + this.start + "_" + this.end; }

    public String toHeadString() {
        return this.chrom + ":" + this.start + "-" + this.end;
    }
}
