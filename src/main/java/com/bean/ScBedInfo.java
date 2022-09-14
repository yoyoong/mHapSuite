package com.bean;

import java.io.Serializable;

public class ScBedInfo implements Serializable {
    public String chrom; // query template name
    public String nuc; // the nucleotide on reference genome
    public Integer pos; // 1-based leftmost mapping postion
    public String cont; // context
    public String dinuc; // dinucleotide context
    public Float meth; // methylation level of "Not Available"
    public Integer mc; // counts of reads suppport methylated cytosine
    public Integer nc; // counts of reads suppport all cytosine

    public String getChrom() {
        return chrom;
    }

    public void setChrom(String chrom) {
        this.chrom = chrom;
    }

    public String getNuc() {
        return nuc;
    }

    public void setNuc(String nuc) {
        this.nuc = nuc;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public String getCont() {
        return cont;
    }

    public void setCont(String cont) {
        this.cont = cont;
    }

    public String getDinuc() {
        return dinuc;
    }

    public void setDinuc(String dinuc) {
        this.dinuc = dinuc;
    }

    public Float getMeth() {
        return meth;
    }

    public void setMeth(Float meth) {
        this.meth = meth;
    }

    public Integer getMc() {
        return mc;
    }

    public void setMc(Integer mc) {
        this.mc = mc;
    }

    public Integer getNc() {
        return nc;
    }

    public void setNc(Integer nc) {
        this.nc = nc;
    }

    public String getChrNum() {
        return this.chrom.substring("chr".length(), this.chrom.length());
    }
}
