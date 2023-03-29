package com;

import com.args.ProfileViewArgs;
import com.bean.Region;
import com.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProfileView {
    public static final Logger log = LoggerFactory.getLogger(ProfileView.class);
    ProfileViewArgs args = new ProfileViewArgs();
    Util util = new Util();
    public static final Integer MAXSIZE = 10000;

    public void profileView(ProfileViewArgs profileViewArgs) throws Exception {
        log.info("ProfileView start!");
        args = profileViewArgs;

        // check the command
        boolean checkResult = checkArgs();
        if (!checkResult) {
            log.error("Checkargs fail, please check the command.");
            return;
        }

        // get bedfile list
        String[] bedPaths = args.getBedPaths().split(" ");
        for (String bedPath : bedPaths) {
            List<Region> regionList = util.getBedRegionList(bedPath);
            if (regionList.size() < 1) {
                log.error("The bed file:" + bedPath + " is null, please check.");
                continue;
            }
        }

        log.info("ProfileView end!");
    }

    private boolean checkArgs() {
        if (args.getBigwig() == null || args.getBigwig().equals("")) {
            log.error("The bigwig file can not be null.");
            return false;
        }
        if (args.getBedPaths() == null || args.getBedPaths().equals("")) {
            log.error("The bed file can not be null.");
            return false;
        }
        if (args.getTag() == null) {
            log.error("The tag can not be null.");
            return false;
        }

        return true;
    }

}
