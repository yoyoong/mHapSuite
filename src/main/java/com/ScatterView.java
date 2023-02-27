package com;

import com.args.ScatterViewArgs;
import com.bean.Region;
import com.common.bigwigTool.BBFileReader;
import com.common.bigwigTool.BigWigIterator;
import com.common.Util;
import com.common.bigwigTool.WigItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScatterView {
    public static final Logger log = LoggerFactory.getLogger(ScatterView.class);

    Util util = new Util();
    ScatterViewArgs args = new ScatterViewArgs();
    Region region = new Region();

    public void scatterView(ScatterViewArgs r2Args) throws Exception {
        log.info("ScatterView start!");
        args = r2Args;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // parse the region
        region = util.parseRegion(args.getRegion());

        String bigwig1 = args.getBigwig1();

        BBFileReader reader=new BBFileReader(bigwig1);
        System.out.println(reader.getChromosomeNames());
        BigWigIterator iter = reader.getBigWigIterator(region.getChrom(), region.getStart(), region.getChrom(), region.getEnd(), true);
        while(iter.hasNext()){
            WigItem line = iter.next();
            System.out.println(iter.next().getStartBase());
        }

        boolean getMhapViewResult = getMhapView();
        if (!getMhapViewResult) {
            log.error("getMhapView fail, please check the command.");
            return;
        }

        log.info("ScatterView end!");
    }

    private boolean checkArgs() {
        return true;
    }

    private boolean getMhapView() throws Exception {

        return true;
    }
}
