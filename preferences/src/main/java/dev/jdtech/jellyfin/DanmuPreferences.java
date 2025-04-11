package dev.jdtech.jellyfin;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.UUID;


import timber.log.Timber;

public class DanmuPreferences {
    private final SharedPreferences sharedPreferences;

    private static final String VIDEO_SPEED_KEY = "video_speed";
    private static final String DAN_MU_CONTROLLER = "danmu_controller";
    private static final String DAN_MU_SPEED = "danmu_speed";
    private static final String DAN_MU_FONT_SIZE = "danmu_f_size";
    private static final String DAN_MU_ROWS = "danmu_row";
    private static final String DAN_MU_STYLE= "dm_style";
    private static final String DAN_MU_OFFSET_TIME= "dm_o_t";

    private int danmuRows;
    private float danmuSpeed;
    private float danmuFontSize;
    private boolean danmuFps;
    private boolean danmuController;
    private int danmuStyle;

    private JSONObject seasonDanmuOffsetTimes;

    public DanmuPreferences(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        // IDisplayer.DANMAKU_STYLE_STROKEN
        danmuStyle = sharedPreferences.getInt(DAN_MU_STYLE, 2);
        danmuRows = sharedPreferences.getInt(DAN_MU_ROWS, 5);
        danmuController = sharedPreferences.getBoolean(DAN_MU_CONTROLLER, true);
        danmuSpeed = sharedPreferences.getFloat(DAN_MU_SPEED, 1.0f);
        danmuFontSize = sharedPreferences.getFloat(DAN_MU_FONT_SIZE, 1.0f);

        try {
            seasonDanmuOffsetTimes = JSON.parseObject(sharedPreferences.getString(DAN_MU_OFFSET_TIME, "{}"));
        } catch (Exception e) {
            Timber.e(e, "加载弹幕偏移数据失败");
            this.seasonDanmuOffsetTimes = new com.alibaba.fastjson.JSONObject();
            updateDanmuOffsetTime();
        }
    }

    public float getDanmuSpeed() {
        return danmuSpeed;
    }

    public boolean isDanmuFps() {
        return danmuFps;
    }

    public void setDanmuFps(boolean danmuFps) {
        this.danmuFps = danmuFps;
    }

    public float getDanmuFontSize() {
        return danmuFontSize;
    }

    public void setDanmuFontSize(float danmuFontSize) {
        setValue(DAN_MU_FONT_SIZE, danmuFontSize);
        this.danmuFontSize = danmuFontSize;
    }

    public int getDanmuRows() {
        return danmuRows;
    }

    public int getDanmuStyle() {
        return danmuStyle;
    }

    public void setDanmuStyle(int danmuStyle) {
        setValue(DAN_MU_STYLE, danmuStyle);
        this.danmuStyle = danmuStyle;
    }

    public void setDanmuRows(int danmuRows) {
        setValue(DAN_MU_ROWS, danmuRows);
        this.danmuRows = danmuRows;
    }

    public boolean isDanmuController() {
        return danmuController;
    }

    public void setDanmuController(boolean danmuController) {
        this.danmuController = danmuController;
        setValue(DAN_MU_CONTROLLER, danmuController);
    }

    public int getSeasonDanmuOffset(String seasonId) {
        return seasonDanmuOffsetTimes.getIntValue(seasonId);
    }

    public void putSeasonDanmuOffset(String seasonId, Integer offsetTime) {
        if (offsetTime == null || offsetTime == 0) {
            seasonDanmuOffsetTimes.remove(seasonId);
            return;
        }
        seasonDanmuOffsetTimes.put(seasonId, offsetTime);
        updateDanmuOffsetTime();
    }

    private void updateDanmuOffsetTime() {
        if (seasonDanmuOffsetTimes != null) {
            setValue(DAN_MU_OFFSET_TIME, seasonDanmuOffsetTimes.toJSONString());
        }
    }

    private  <T> void setValue(String key, T value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value == null) {
            editor.remove(key);
            editor.apply();
            return;
        }

        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        }


//        switch (value) {
//            case Boolean b -> editor.putBoolean(key, b);
//            case Integer i -> editor.putInt(key, i);
//            case Long l -> editor.putLong(key, l);
//            case Float v -> editor.putFloat(key, v);
//            case String s -> editor.putString(key, s);
//            default -> throw new RuntimeException("Unsupported type");
//        }
        editor.apply();
    }
}
