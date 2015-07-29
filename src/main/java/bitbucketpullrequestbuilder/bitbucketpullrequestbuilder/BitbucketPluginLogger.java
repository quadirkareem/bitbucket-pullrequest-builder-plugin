package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BitbucketPluginLogger {

    public static final Level LEVEL_DEBUG = Level.FINER;

    public static void debug(Logger logger, String message) {
        logger.finer(message);
    }
}
