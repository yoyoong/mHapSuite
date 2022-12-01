package com.bean;

import java.io.Serializable;

public class BedGraphInfo implements Serializable {
    public String chrom;
    public Integer start;
    public Integer end;
    public Integer Cov;
    public Float MM;
    public Float PDR;
    public Float CHALM;
    public Float MHL;
    public Float MCR;
    public Float MBS;
    public Float Entropy;
    public Float R2;

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

    public Integer getCov() {
        return Cov;
    }

    public void setCov(Integer cov) {
        Cov = cov;
    }

    public Float getMM() {
        return MM;
    }

    public void setMM(Float MM) {
        this.MM = MM;
    }

    public Float getPDR() {
        return PDR;
    }

    public void setPDR(Float PDR) {
        this.PDR = PDR;
    }

    public Float getCHALM() {
        return CHALM;
    }

    public void setCHALM(Float CHALM) {
        this.CHALM = CHALM;
    }

    public Float getMHL() {
        return MHL;
    }

    public void setMHL(Float MHL) {
        this.MHL = MHL;
    }

    public Float getMCR() {
        return MCR;
    }

    public void setMCR(Float MCR) {
        this.MCR = MCR;
    }

    public Float getMBS() {
        return MBS;
    }

    public void setMBS(Float MBS) {
        this.MBS = MBS;
    }

    public Float getEntropy() {
        return Entropy;
    }

    public void setEntropy(Float entropy) {
        Entropy = entropy;
    }

    public Float getR2() {
        return R2;
    }

    public void setR2(Float r2) {
        R2 = r2;
    }

    public String printCov() {
        return this.getChrom() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + this.getCov() + "\n";
    }

    public String printMM() {
        return this.getChrom() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + this.getMM() + "\n";
    }

    public String printPDR() {
        return this.getChrom() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + this.getPDR() + "\n";
    }

    public String printCHALM() {
        return this.getChrom() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + this.getCHALM() + "\n";
    }

    public String printMHL() {
        return this.getChrom() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + this.getMHL() + "\n";
    }

    public String printMCR() {
        return this.getChrom() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + this.getMCR() + "\n";
    }

    public String printMBS() {
        return this.getChrom() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + this.getMBS() + "\n";
    }

    public String printEntropy() {
        return this.getChrom() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + this.getEntropy() + "\n";
    }

    public String printR2() {
        return this.getChrom() + "\t" + this.getStart() + "\t" + this.getEnd() + "\t" + this.getR2() + "\n";
    }
}
