import junit.framework.TestCase;
import com.Main;
import org.junit.Test;

public class MainTest extends TestCase {

    @Test
    public void test_tanghulu() throws Exception {
        Main main = new Main();
        String arg0 = "tanghulu";
        String arg1 = "-mhapPath";
        String arg2 = "esophagus_T.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-region";
        String arg6 = "chr1:566520-566816";
        String arg7 = "-outputFile";
        String arg8 = "esophagus_T";
        String arg9 = "-strand";
        String arg10 = "both";
        String arg11 = "-outFormat";
        String arg12 = "pdf";
        String arg13 = "-maxReads";
        String arg14 = "1000";
        String arg15 = "-maxLength";
        String arg16 = "2000";
        String arg17 = "-merge";
        String arg18 = "-simulation";
        String arg19 = "-cutReads";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17};
        // String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg18};
        // String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18};
        // String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19};

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
        String arg2 = "esophagus_T.mhap.gz";
        String arg3 = "-cpgPath";
        String arg4 = "hg19_CpG.gz";
        String arg5 = "-region";
        String arg6 = "chr1:3229375-3230473";
        String arg7 = "-bed";
        String arg8 = "CRC_sc_bulk.bed";
        String arg9 = "-outputFile";
        String arg10 = "test";
        String arg11 = "-outFormat";
        String arg12 = "png";
        String arg13 = "-strand";
        String arg14 = "both";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14};

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
        String arg2 = "MM PDR CHALM MHL MCR MBS Entropy";
        String arg3 = "-mhapPath";
        String arg4 = "esophagus_T.mhap.gz";
        String arg5 = "-cpgPath";
        String arg6 = "hg19_CpG.gz";
        String arg7 = "-region";
        String arg8 = "chr1:566520-566816";
//        String arg7 = "-bedPath";
//        String arg8 = "esophagus_T_MHB.bed";
        String arg9 = "-outputFile";
        String arg10 = "outStat.tsv";
        String arg11 = "-minK";
        String arg12 = "1";
        String arg13 = "-maxK";
        String arg14 = "10";
        String arg15 = "-K";
        String arg16 = "4";
        String arg17 = "-strand";
        String arg18 = "both";
        String arg19 = "-cutReads";
        String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18};
        //String[] args = {arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19};

        String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i] + " ";
        }
        System.out.println(argsStr);

        main.main(args);
    }
}