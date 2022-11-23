package org.wltea.analyzer.help;

import org.apache.logging.log4j.Logger;

public class Sleep {

    private static final Logger logger = ESPluginLoggerFactory.getLogger(Sleep.class.getName());

    public enum Type {MSEC, SEC, MIN, HOUR}

    ;

    public static void sleep(Type type, int num) {
        try {
            switch (type) {
                case MSEC:
                    Thread.sleep(num);
                    return;
                case SEC:
                    Thread.sleep(num * 1000);
                    return;
                case MIN:
                    Thread.sleep(num * 60 * 1000);
                    return;
                case HOUR:
                    Thread.sleep(num * 60 * 60 * 1000);
                    return;
                default:
                    System.err.println("輸入型別錯誤，應為MSEC,SEC,MIN,HOUR之一");
                    return;
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }


}
