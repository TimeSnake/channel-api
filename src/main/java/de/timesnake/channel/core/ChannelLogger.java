package de.timesnake.channel.core;

public abstract class ChannelLogger {

    private boolean printInfoLog = false;

    public void logInfo(String msg) {
        if (printInfoLog) {
            this.printInfo(msg);
        }
    }

    public void logInfo(String msg, boolean print) {
        if (printInfoLog || print) {
            this.printInfo(msg);
        }
    }

    public void logWarning(String msg) {
        this.printWarning(msg);
    }

    public abstract void printInfo(String msg);

    public abstract void printWarning(String msg);

    public void printInfoLog(boolean enable) {
        this.printInfoLog = enable;
    }

    public boolean isPrintingInfoLog() {
        return printInfoLog;
    }
}
