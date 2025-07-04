import junit.framework.TestCase;
import com.Main;
import org.junit.Test;

import java.io.File;

public class MainTest extends TestCase {
    @Test
    public void test_help() throws Exception {
        Main main = new Main();
        String arg0 = "help";
        String[] args = {arg0};
        main.main(args);
    }

    @Test
    public void test_convert() throws Exception {
        Main main = new Main();
        String arg0 = "convert";
        String arg1 = "-inputFile";
        String arg2 = "GSM5652232_Cortex-Neuron-Z000000TD.pat.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-region";
        String arg6 = "chr1:1-10000000";
//        String arg5 = "-bedPath";
//        String arg6 = "colon_MHB.bed";
        String arg7 = "-nonDirectional";
        String arg8 = "-outputFile";
        String arg9 = "GSM5652232_Cortex_2.mhap.gz";
        String arg10 = "-mode";
        String arg11 = "BS";
        String arg12 = "-pat";
        String arg13 = "-filterFlag";

        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg8, arg9, arg10, arg11, arg12};
        // String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg8, arg9, arg10, arg11};
        // String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_merge() throws Exception {
        Main main = new Main();
        String arg0 = "merge";
        String arg1 = "-inputFile";
        String arg2 = "GSM5652261_Kidney-Glomerular-Podocytes-Z0000042W.mhap.gz GSM5652262_Kidney-Glomerular-Podocytes-Z00000441.mhap.gz GSM5652263_Kidney-Glomerular-Podocytes-Z00000442.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-outputFile";
        String arg6 = "test.mhap.gz";

        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_tanghulu() throws Exception {
        Main main = new Main();
        String arg0 = "tanghulu";
        String arg1 = "-mhapPath";
        String arg2 = "P1_LIHC.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-region";
        String arg6 = "chr22:27195698-27197698";
        String arg7 = "-tag";
        String arg8 = "P1_LIHC";
        String arg9 = "-strand";
        String arg10 = "both";
        String arg11 = "-outFormat";
        String arg12 = "png";
        String arg13 = "-maxReads";
        String arg14 = "50";
        String arg15 = "-maxLength";
        String arg16 = "2000";
        String arg17 = "-merge";
        String arg18 = "-simulation";
        String arg19 = "-cutReads";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17};
        // String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg18};
        // String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18};
        // String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg19};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_MHapView() throws Exception {
        Main main = new Main();
        String arg0 = "mHapView";
        String arg1 = "-mhapPath";
        String arg2 = "SRX203011.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-region";
        String arg6 = "chr10:27195698-27196698";
        String arg7 = "-bedPath";
        String arg8 = "/sibcb2/bioinformatics2/fengyan/Plot/Process/figure6/MHB_Bulk/sperm_MHB.bed";
        String arg9 = "-tag";
        String arg10 = "P1_LIHC";
        String arg11 = "-outFormat";
        String arg12 = "png";
        String arg13 = "-strand";
        String arg14 = "both";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg9, arg10, arg11, arg12, arg13, arg14};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_stat() throws Exception {
        Main main = new Main();
        String arg0 = "stat";
        String arg1 = "-metrics";
        String arg2 = "MHL";
        String arg3 = "-mhapPath";
        String arg4 = "SRX8181972.mhap.gz";
        String arg5 = "-cpgPath";
        String arg6 = "hg19_CpG.gz";
//        String arg7 = "-region";
//        String arg8 = "chr1:566520-566816";
        String arg7 = "-bedPath";
        String arg8 = "background_tumor.MHB.bed";
        String arg9 = "-outputFile";
        String arg10 = "test.tsv";
        String arg11 = "-minK";
        String arg12 = "1";
        String arg13 = "-maxK";
        String arg14 = "10";
        String arg15 = "-K";
        String arg16 = "4";
        String arg17 = "-strand";
        String arg18 = "both";
        String arg19 = "-cutReads";
        String arg20 = "-k4Plus";
        String arg21 = "10";
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18};
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19};
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg20, arg21};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_genomeWide() throws Exception {
        Main main = new Main();
        String arg0 = "genomeWide";
        String arg1 = "-tag";
        String arg2 = "test";
        String arg3 = "-mhapPath";
        String arg4 = "ENCFF285ZGH.mhap.gz";
        String arg5 = "-cpgPath";
        String arg6 = "hg19_CpG.gz";
        String arg7 = "-metrics";
        String arg8 = "MM";
        String arg9 = "-outputDir";
        String arg10 = "outputDir";
        String arg11 = "-minK";
        String arg12 = "1";
        String arg13 = "-maxK";
        String arg14 = "10";
        String arg15 = "-K";
        String arg16 = "4";
        String arg17 = "-strand";
        String arg18 = "both";
//        String arg19 = "-region";
//        String arg20 = "chr1:10469-100903";
        String arg19 = "-bedPath";
        String arg20 = "chr1.bed";
        String arg21 = "-cpgCov";
        String arg22 = "0";
        String arg23 = "-r2Cov";
        String arg24 = "20";
        String arg25 = "-k4Plus";
        String arg26 = "5";

        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14,
                arg15, arg16, arg17, arg18, arg21, arg22, arg23, arg24, arg25, arg26};
//        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14,
//                arg15, arg16, arg17, arg18, arg21, arg22, arg23, arg24, arg25, arg26};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_MHBDiscovery() throws Exception {
        Main main = new Main();
        String arg0 = "MHBDiscovery";
        String arg1 = "-mhapPath";
        String arg2 = "20A042628_bam.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
//        String arg5 = "-region";
//        String arg6 = "chr14:100327345-100327370";
        String arg5= "-bedPath";
        String arg6 = "target.sort.bed";
        String arg7 = "-window";
        String arg8 = "5";
        String arg9 = "-r2";
        String arg10 = "0.5";
        String arg11 = "-pvalue";
        String arg12 = "0.05";
//        String arg13 = "-outputDir";
//        String arg14 = "outputDir";
        String arg15 = "-tag";
        String arg16 = "20A042628_bam.MHB";
        String arg17 = "-qcFlag";
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16};
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg7, arg8, arg9, arg10, arg11, arg12, arg15, arg16};
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_ScatterPlot() throws Exception {
        Main main = new Main();
        String arg0 = "scatterPlot";
        String arg1 = "-bedPath";
        String arg2 = "MHB_cervix_normal.bed";
        String arg7 = "-bigwig1";
        String arg8 = "P1_ESCC_MM.bw";
        String arg9 = "-bigwig2";
        String arg10 = "P1_BRCA_MM.bw";
        String arg11 = "-tag";
        String arg12 = "scatterPlot.output";
        String arg13 = "-outFormat";
        String arg14 = "png";
        String[] args = {arg0, arg1, arg2, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_BoxPlot() throws Exception {
        Main main = new Main();
        String arg0 = "boxPlot";
        String arg1 = "-bedPath";
        String arg2 = "MHB_cervix_normal.bed";
        String arg7 = "-bigwigs";
        String arg8 = "colon_MM.bw";
        String arg11 = "-tag";
        String arg12 = "boxPlot.output";
        String arg13 = "-outFormat";
        String arg14 = "png";
        String[] args = {arg0, arg1, arg2, arg7, arg8, arg11, arg12, arg13, arg14};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_HeatMapPlot() throws Exception {
        Main main = new Main();
        String arg0 = "heatMapPlot";
        String arg1 = "-bedPaths";
        // String arg2 = "MHB_cervix_normal.bed";
        String arg2 = "test.bed";
        String arg7 = "-bigwig";
        String arg8 = "esophagus_N_MM.bw";
        String arg3 = "-upLength";
        String arg4 = "15000";
        String arg5 = "-downLength";
        String arg6 = "10000";
        String arg9 = "-window";
        String arg10 = "200";
        String arg16 = "-sortRegions";
        String arg17 = "missingValues";
        String arg11 = "-tag";
        String arg12 = "heatMapPlot.output";
        String arg13 = "-outFormat";
        String arg14 = "png";
        String arg15 = "-matrixFlag";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg9, arg10, arg7, arg8, arg11, arg12, arg13, arg14, arg15, arg16, arg17};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_ProfilePlot() throws Exception {
        Main main = new Main();
        String arg0 = "profilePlot";
        String arg1 = "-bedPaths";
        // String arg2 = "MHB_cervix_normal.bed";
        String arg2 = "test.bed";
        String arg7 = "-bigwig";
        String arg8 = "esophagus_T_MM.bw";
        String arg3 = "-upLength";
        String arg4 = "2000";
        String arg5 = "-downLength";
        String arg6 = "2000";
        String arg9 = "-windowNum";
        String arg10 = "10";
        String arg11 = "-tag";
        String arg12 = "profilePlot.output";
        String arg13 = "-outFormat";
        String arg14 = "png";
        String arg15 = "-matrixFlag";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg9, arg10, arg7, arg8, arg11, arg12, arg13, arg14, arg15};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_EnrichmentPlot() throws Exception {
        Main main = new Main();
        String arg0 = "enrichmentPlot";
        String arg1 = "-bedPaths";
        String arg2 = "MHB_cervix_normal.bed esophagus_normal_MHB.bed";
        String arg3 = "-openChromatin";
        String arg4 = "hg19_cpgisland.bed";
        String arg7 = "-bigwig";
        String arg8 = "P1_BRCA_MM.bw";
        String arg5 = "-groupNum";
        String arg6 = "10";
        String arg9 = "-groupCutoff";
        String arg10 = "100";
        String arg11 = "-tag";
        String arg12 = "enrichmentPlot.output";
        String arg13 = "-outFormat";
        String arg14 = "png";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg9, arg10, arg7, arg8, arg11, arg12, arg13, arg14};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }

    @Test
    public void test_ComputeCpgCov() throws Exception {
        Main main = new Main();
        String arg0 = "computeCpgCov";
        String arg1 = "-bigwig";
        String arg2 = "breast_T_Merged_CpG.bw";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg7 = "-bedPath";
        String arg8 = "MHB_BRCA.bed";
        String arg5 = "-openChromatin";
        String arg6 = "BRCA_hg19_peak.bed";
        String arg9 = "-tag";
        String arg10 = "test2";
        String arg11 = "-missingDataAsZero";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg9, arg10, arg7, arg8, arg11};

        System.out.println("Work direqtory: " + System.getProperty("user.dir"));
        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }
}