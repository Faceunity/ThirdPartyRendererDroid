package com.faceunity.litenama;

import android.app.Application;

import com.faceunity.nama.utils.PreferenceUtil;
import com.faceunity.nama.ui.BeautyParameterModel;

/**
 * @author Richie on 2019.12.20
 */
public class NamaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceUtil.init(this);
        BeautyParameterModel.init();
    }

}
