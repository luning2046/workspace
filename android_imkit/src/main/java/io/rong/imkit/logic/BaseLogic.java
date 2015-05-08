package io.rong.imkit.logic;

import io.rong.imkit.service.RCloudService;
import android.content.Intent;

/**
 * Created by zhjchen on 14-3-20.
 */
public abstract class BaseLogic implements ActionListener {

    public BaseLogic(RCloudService fCloudService) {

    }

    @Override
    public void onHandleAction(Intent intent) {

    }


    public void destroy() {

    }


}
