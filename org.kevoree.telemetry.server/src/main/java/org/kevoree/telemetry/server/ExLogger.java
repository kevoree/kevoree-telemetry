package org.kevoree.telemetry.server;

import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;

/**
 * Created by duke on 8/7/14.
 */
public class ExLogger implements InternalLogger {
    @Override
    public String name() {
        return ExLogger.class.getSimpleName();
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String s) {

    }

    @Override
    public void trace(String s, Object o) {

    }

    @Override
    public void trace(String s, Object o, Object o1) {

    }

    @Override
    public void trace(String s, Object... objects) {

    }

    @Override
    public void trace(String s, Throwable throwable) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String s) {

    }

    @Override
    public void debug(String s, Object o) {

    }

    @Override
    public void debug(String s, Object o, Object o1) {

    }

    @Override
    public void debug(String s, Object... objects) {

    }

    @Override
    public void debug(String s, Throwable throwable) {

    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String s) {

    }

    @Override
    public void info(String s, Object o) {

    }

    @Override
    public void info(String s, Object o, Object o1) {

    }

    @Override
    public void info(String s, Object... objects) {

    }

    @Override
    public void info(String s, Throwable throwable) {

    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String s) {

    }

    @Override
    public void warn(String s, Object o) {

    }

    @Override
    public void warn(String s, Object... objects) {

    }

    @Override
    public void warn(String s, Object o, Object o1) {

    }

    @Override
    public void warn(String s, Throwable throwable) {

    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String s) {

    }

    @Override
    public void error(String s, Object o) {

    }

    @Override
    public void error(String s, Object o, Object o1) {

    }

    @Override
    public void error(String s, Object... objects) {

    }

    @Override
    public void error(String s, Throwable throwable) {

    }

    @Override
    public boolean isEnabled(InternalLogLevel internalLogLevel) {
        return false;
    }

    @Override
    public void log(InternalLogLevel internalLogLevel, String s) {

    }

    @Override
    public void log(InternalLogLevel internalLogLevel, String s, Object o) {

    }

    @Override
    public void log(InternalLogLevel internalLogLevel, String s, Object o, Object o1) {

    }

    @Override
    public void log(InternalLogLevel internalLogLevel, String s, Object... objects) {

    }

    @Override
    public void log(InternalLogLevel internalLogLevel, String s, Throwable throwable) {

    }
}
