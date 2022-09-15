package com.common;

import com.bean.BedInfo;
import com.bean.MHapInfo;
import com.bean.R2Info;
import com.bean.Region;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.List;
import java.util.*;

public class Sort {
    public Comparator<Integer[]> sortByCpgRate = new Comparator<Integer[]>() {
        public int compare(Integer[] a, Integer[] b){
            Integer cpgNumA = 0;
            Integer unCpgNumA = 0;
            Integer emptyNumA = 0;
            for (int i = 0; i < a.length; i++) {
                if (a[i] != null) {
                    if (a[i] == 1) {
                        cpgNumA++;
                    } else {
                        unCpgNumA++;
                    }
                } else {
                    emptyNumA++;
                }
            }
            Integer cpgNumB = 0;
            Integer unCpgNumB = 0;
            Integer emptyNumB = 0;
            for (int i = 0; i < b.length; i++) {
                if (b[i] != null) {
                    if (b[i] == 1) {
                        cpgNumB++;
                    } else {
                        unCpgNumB++;
                    }
                } else {
                    emptyNumB++;
                }
            }
            return cpgNumB - cpgNumA;
        }
    };
}
