package com.dan.rec.db;

import com.dan.rec.utils.Constants;

import org.litepal.crud.DataSupport;

/**
 * @功能描述:
 * @作者:Xixi
 * @创建时间: 16/5/20 17:26
 */
public class LogItem extends DataSupport {
    private int id = -1;
    private long time;
    private String logStr;
    private boolean isTimeSensitive;

    public LogItem() {

    }

    public LogItem(long time, String logStr, boolean isTimeSensitive) {
        this.time = time;
        this.logStr = logStr;
        this.isTimeSensitive = isTimeSensitive;
    }

    public int getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogStr() {
        return logStr;
    }

    public void setLogStr(String logStr) {
        this.logStr = logStr;
    }

    public boolean isTimeSensitive() {
        return isTimeSensitive;
    }

    public void setTimeSensitive(boolean timeSensitive) {
        isTimeSensitive = timeSensitive;
    }

    public String getLog() {
        String formatStr = isTimeSensitive ? Constants.TimeF_HMS : Constants.TimeF_HM;
        return String.format(formatStr, time) + "  " + getLogStr();
    }
}
