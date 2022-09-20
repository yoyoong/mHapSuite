package com.bean;

import java.io.Serializable;
import java.util.List;

public class StatInfo implements Serializable {
    public String chr;
    public Integer start;
    public Integer end;
    public Integer nReads = 0; // 总read个数
    public Integer mBase = 0; // 甲基化位点个数
    public Integer cBase = 0; // 存在甲基化的read中的未甲基化位点个数
    public Integer tBase = 0; // 总位点个数
    public Integer K4plus = 0; // 长度大于等于K个位点的read个数
    public Integer nDR = 0; // 长度大于等于K个位点且同时含有甲基化和未甲基化位点的read个数
    public Integer nMR = 0; // 长度大于等于K个位点且含有甲基化位点的read个数
    public Integer nCPG = 0; // 甲基化位点个数
    public Integer nPairs = 0; // 可计算r2的pair个数
    public Double MM = 0.0; // MM=mBase\tBase
    public Double CHALM = 0.0; // CHALM=nMR/K4plus
    public Double PDR = 0.0; // PDR=nDR/K4plus
    public Double MHL = 0.0; // Guo et al.,2017
    public Double MBS = 0.0; //
    public Double MCR = 0.0; // MCR=cBase\tBase
    public Double Entropy = 0.0; //
    public Double R2 = 0.0; //

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
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

    public Integer getnReads() {
        return nReads;
    }

    public void setnReads(Integer nReads) {
        this.nReads = nReads;
    }

    public Integer getmBase() {
        return mBase;
    }

    public void setmBase(Integer mBase) {
        this.mBase = mBase;
    }

    public Integer getcBase() {
        return cBase;
    }

    public void setcBase(Integer cBase) {
        this.cBase = cBase;
    }

    public Integer gettBase() {
        return tBase;
    }

    public void settBase(Integer tBase) {
        this.tBase = tBase;
    }

    public Integer getK4plus() {
        return K4plus;
    }

    public void setK4plus(Integer k4plus) {
        K4plus = k4plus;
    }

    public Integer getnDR() {
        return nDR;
    }

    public void setnDR(Integer nDR) {
        this.nDR = nDR;
    }

    public Integer getnMR() {
        return nMR;
    }

    public void setnMR(Integer nMR) {
        this.nMR = nMR;
    }

    public Integer getnCPG() {
        return nCPG;
    }

    public void setnCPG(Integer nCPG) {
        this.nCPG = nCPG;
    }

    public Integer getnPairs() {
        return nPairs;
    }

    public void setnPairs(Integer nPairs) {
        this.nPairs = nPairs;
    }

    public Double getMM() {
        return MM;
    }

    public void setMM(Double MM) {
        this.MM = MM;
    }

    public Double getCHALM() {
        return CHALM;
    }

    public void setCHALM(Double CHALM) {
        this.CHALM = CHALM;
    }

    public Double getPDR() {
        return PDR;
    }

    public void setPDR(Double PDR) {
        this.PDR = PDR;
    }

    public Double getMHL() {
        return MHL;
    }

    public void setMHL(Double MHL) {
        this.MHL = MHL;
    }

    public Double getMBS() {
        return MBS;
    }

    public void setMBS(Double MBS) {
        this.MBS = MBS;
    }

    public Double getMCR() {
        return MCR;
    }

    public void setMCR(Double MCR) {
        this.MCR = MCR;
    }

    public Double getEntropy() {
        return Entropy;
    }

    public void setEntropy(Double entropy) {
        this.Entropy = entropy;
    }

    public Double getR2() {
        return R2;
    }

    public void setR2(Double r2) {
        R2 = r2;
    }


    public String print(List<String> metricsList) {
        String line = this.chr + "\t" + this.start + "\t" + this.end + "\t" + this.nReads + "\t" + this.mBase + "\t" + this.cBase + "\t"
                + this.tBase + "\t" + this.K4plus + "\t" + this.nDR + "\t" + this.nMR + "\t" + this.nCPG + "\t" + this.nPairs;
        for (int i = 0; i < metricsList.size(); i++) {
            if (metricsList.get(i).equals("MM")) {
                line += "\t" + String.format("%.8f", this.MM);
            } else if (metricsList.get(i).equals("CHALM")) {
                line += "\t" + String.format("%.8f", this.CHALM);
            } else if (metricsList.get(i).equals("PDR")) {
                line += "\t" + String.format("%.8f", this.PDR);
            } else if (metricsList.get(i).equals("MHL")) {
                line += "\t" + String.format("%.8f", this.MHL);
            } else if (metricsList.get(i).equals("MBS")) {
                line += "\t" + String.format("%.8f", this.MBS);
            } else if (metricsList.get(i).equals("MCR")) {
                line += "\t" + String.format("%.8f", this.MCR);
            } else if (metricsList.get(i).equals("Entropy")) {
                line += "\t" + String.format("%.8f", this.Entropy);
            } else if (metricsList.get(i).equals("R2")) {
                line += "\t" + String.format("%.8f", this.R2);
            }
        }
        line += "\n";
        return line;
    }
}
