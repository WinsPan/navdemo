package com.test.navdemo.util;

import android.content.Context;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;

public class ORMUtil {

    private static LiteOrm liteOrm;

    public static LiteOrm getLiteOrm(Context context) {
        if (liteOrm == null) {
            DataBaseConfig config = new DataBaseConfig(context, "liteorm.db");
            config.debugged = true; // open the log
            config.dbVersion = 1; // set database version
            config.onUpdateListener = null; // set database update listener
            liteOrm = LiteOrm.newSingleInstance(config);
        }
        return liteOrm;
    }
}
