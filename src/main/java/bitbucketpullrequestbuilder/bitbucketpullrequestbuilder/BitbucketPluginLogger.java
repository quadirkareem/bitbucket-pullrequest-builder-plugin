package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BitbucketPluginLogger {

    public static final Level LEVEL_DEBUG = Level.FINER;

    public static void debug(Logger logger, String message) {
        logger.finer(message);
    }

    public static void main(String[] args) throws SecurityException, IOException, InterruptedException {

        FileHandler handler = new FileHandler("D:\\tools\\jenkins-ci\\jenkins-finer-logs.%u.%g.log", 10 * 1024 * 1024,
            10, true);
        handler.setLevel(Level.FINER);
        handler.setEncoding(StandardCharsets.UTF_8.toString());
        SimpleFormatter formatter = new SimpleFormatter();
        handler.setFormatter(formatter);

        Logger logger = Logger.getLogger("MyLog");
        logger.addHandler(handler);
        for (int i = 1; i <= 25; i++) {
            logger.log(Level.WARNING, "Logging: " + i);
            Thread.sleep(1000);
        }
    }
}
