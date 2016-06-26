package com.dan.rec.db;

import com.dan.rec.utils.DebugLog;

import org.litepal.crud.ClusterQuery;
import org.litepal.crud.DataSupport;

import java.util.Collections;
import java.util.List;

/**
 * @功能描述:
 * @作者:Xixi
 * @创建时间: 16/5/20 17:33
 */
public class DbManager {
    public static void insertGoods(LogItem item) {
        if (item == null) {
            return;
        }
        item.save();
    }

    public static List<LogItem> queryAll(long startTime) {
        ClusterQuery clusterQuery = DataSupport.where("time>=" + startTime);
        List<LogItem> datas = find(clusterQuery, LogItem.class);
        if (datas != null) {
            Collections.reverse(datas);
        }
        return datas;
    }

    public static void delete(long time) {
        try {
            int itemsCount = DataSupport.deleteAll(LogItem.class, "time<=" + time);
            DebugLog.d("", "delete itemsCount: " + itemsCount);
        } catch (Exception e) {
            DebugLog.d("", "delete : " + e.toString());
        }
    }

    private static <T> List<T> find(ClusterQuery clusterQuery, Class<T> modelClass) {
        List<T> datas = null;
        try {
            datas = clusterQuery.find(modelClass, false);
        } catch (Exception e) {
            //org.litepal.exceptions.DataSupportException
            DebugLog.e("", "find:" + e);
        }
        return datas;
    }
}
