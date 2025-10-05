package uai.helcio.utils;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class AppLogger {
    private static final String NAME = "uai.helcio";
    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(NAME);

    public static void setLoggingLevel(Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        ch.qos.logback.classic.Level logbackLevel = ch.qos.logback.classic.Level.convertAnSLF4JLevel(level);
        rootLogger.setLevel(logbackLevel);
    }

    public static void peekDebug(Object obj) {
        logger.debug(obj.toString());
    }
}
