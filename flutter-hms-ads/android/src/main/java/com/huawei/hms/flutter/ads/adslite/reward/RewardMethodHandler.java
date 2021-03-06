/*
    Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.huawei.hms.flutter.ads.adslite.reward;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.huawei.hms.ads.reward.Reward;
import com.huawei.hms.flutter.ads.factory.EventChannelFactory;
import com.huawei.hms.flutter.ads.utils.FromMap;
import com.huawei.hms.flutter.ads.utils.ToMap;
import com.huawei.hms.flutter.ads.utils.constants.Channels;
import com.huawei.hms.flutter.ads.utils.constants.ErrorCodes;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;

public class RewardMethodHandler implements MethodChannel.MethodCallHandler {

    private final BinaryMessenger messenger;
    private final Activity activity;

    public RewardMethodHandler(BinaryMessenger messenger, Activity activity) {
        this.messenger = messenger;
        this.activity = activity;
    }

    @Override
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final Result result) {
        switch (call.method) {
            case "initRewardAd":
                initRewardAd(call, result);
                break;
            case "loadRewardAd":
                loadRewardAd(call, result);
                break;
            case "showRewardAd":
                showRewardAd(call, result);
                break;
            case "getRewardAdReward":
                getRewardAdReward(call, result);
                break;
            case "isAdLoaded":
                isAdLoaded(call, result);
                break;
            case "pauseAd":
                pauseAd(call, result);
                break;
            case "resumeAd":
                resumeAd(call, result);
                break;
            case "destroyAd":
                destroyAd(call, result);
                break;
            default:
                result.notImplemented();
        }
    }

    private void initRewardAd(MethodCall call, Result result) {
        Integer id = FromMap.toInteger("id", call.argument("id"));
        if (id == null) {
            result.error(ErrorCodes.NULL_PARAM, "Ad id is null. Reward ad init failed.", "");
            return;
        }

        EventChannelFactory.create(id, Channels.REWARD_EVENT_CHANNEL, messenger);
        EventChannelFactory.setup(id, new RewardStreamHandler());

        new HmsRewardAd(id, activity);
    }

    private void loadRewardAd(MethodCall call, Result result) {
        Integer id = FromMap.toInteger("id", call.argument("id"));
        HmsRewardAd hmsRewardAd = HmsRewardAd.get(id);
        if (hmsRewardAd == null) {
            result.error(ErrorCodes.NOT_FOUND, "No ad for given id. Load failed. | Ad id : " + id, "");
            return;
        }

        if (!hmsRewardAd.isCreated() && !hmsRewardAd.isFailed()) {
            result.success(true);
            return;
        }

        String adSlotId = call.argument("adSlotId");
        if (adSlotId == null || adSlotId.isEmpty()) {
            result.error(ErrorCodes.NULL_PARAM, "adSlotId is either null or empty. Load failed. | Ad id : " + id, "");
            return;
        }

        Map<String, Object> adParam = ToMap.fromObject(call.argument("adParam"));
        if (call.argument("adParam") == null) {
            result.error(ErrorCodes.NULL_PARAM, "Ad param is null. Load failed. | Ad id : " + id, "");
            return;
        }

        String userId = FromMap.toString("userId", call.argument("userId"));
        if (userId != null) {
            hmsRewardAd.setUserId(userId);
        }

        String data = FromMap.toString("data", call.argument("data"));
        if (data != null) {
            hmsRewardAd.setData(data);
        }

        Map<String, Object> rewardVerifyConfig = ToMap.fromObject(call.argument("rewardVerifyConfig"));
        if (!rewardVerifyConfig.isEmpty()) {
            hmsRewardAd.setRewardVerifyConfig(rewardVerifyConfig);
        }

        hmsRewardAd.loadAd(adSlotId, adParam);
        result.success(true);
    }

    private void showRewardAd(MethodCall call, Result result) {
        Integer id = FromMap.toInteger("id", call.argument("id"));
        HmsRewardAd hmsRewardAd = HmsRewardAd.get(id);
        if (hmsRewardAd == null) {
            result.error(ErrorCodes.NOT_FOUND, "No ad for given id. Show failed. | Ad id : " + id, null);
            return;
        }
        hmsRewardAd.show();
        result.success(true);
    }

    private void getRewardAdReward(MethodCall call, Result result) {
        Integer id = FromMap.toInteger("id", call.argument("id"));
        HmsRewardAd hmsRewardAd = HmsRewardAd.get(id);
        if (hmsRewardAd != null) {
            Reward reward = hmsRewardAd.getReward();
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("name", reward.getName());
            arguments.put("amount", reward.getAmount());
            result.success(arguments);
        } else {
            result.error(ErrorCodes.NOT_FOUND, "No ad for given id. getReward failed. | Ad id : " + id, "");
        }
    }

    private void isAdLoaded(MethodCall call, Result result) {
        Integer id = FromMap.toInteger("id", call.argument("id"));
        HmsRewardAd hmsRewardAd = HmsRewardAd.get(id);
        String adType = FromMap.toString("adType", call.argument("adType"));

        if (hmsRewardAd == null) {
            result.error(ErrorCodes.NOT_FOUND, "No ad for given id. isAdLoaded failed. | Ad id : " + id, "");
            return;
        }

        if (adType != null && adType.equals("Reward")) {
            result.success(hmsRewardAd.isLoaded());
        } else {
            result.error(ErrorCodes.NULL_PARAM, "Ad type parameter is invalid. isAdLoaded failed. | Ad id : " + id, "");
        }

    }

    private void pauseAd(MethodCall call, Result result) {
        Integer id = FromMap.toInteger("id", call.argument("id"));
        HmsRewardAd hmsRewardAd = HmsRewardAd.get(id);
        String adType = FromMap.toString("adType", call.argument("adType"));

        if (hmsRewardAd == null) {
            result.error(ErrorCodes.NOT_FOUND, "No ad for given id. Pause failed. | Ad id : " + id, "");
            return;
        }

        if (adType != null && adType.equals("Reward")) {
            hmsRewardAd.pause();
            result.success(true);
        } else {
            result.error(ErrorCodes.INVALID_PARAM, "Ad type parameter is invalid. Pause failed. | Ad id : " + id, "");
        }
    }

    private void resumeAd(MethodCall call, Result result) {
        Integer id = FromMap.toInteger("id", call.argument("id"));
        HmsRewardAd hmsRewardAd = HmsRewardAd.get(id);
        String adType = FromMap.toString("adType", call.argument("adType"));

        if (hmsRewardAd == null) {
            result.error(ErrorCodes.NOT_FOUND, "No ad for given id. Resume failed. | Ad id : " + id, "");
            return;
        }

        if (adType != null && adType.equals("Reward")) {
            hmsRewardAd.resume();
            result.success(true);
        } else {
            result.error(ErrorCodes.INVALID_PARAM, "Ad type parameter is invalid. Resume failed. | Ad id : " + id, "");
        }
    }

    private void destroyAd(MethodCall call, Result result) {
        Integer id = FromMap.toInteger("id", call.argument("id"));
        HmsRewardAd hmsRewardAd = HmsRewardAd.get(id);
        String adType = FromMap.toString("adType", call.argument("adType"));

        if (id == null || hmsRewardAd == null) {
            result.error(ErrorCodes.NOT_FOUND, "No ad for given id. Destroy failed. | Ad id : " + id, "");
            return;
        }

        if (adType != null && adType.equals("Reward")) {
            EventChannelFactory.dispose(id);
            hmsRewardAd.destroy();
            result.success(true);
        } else {
            result.error(ErrorCodes.INVALID_PARAM, "Ad type parameter is invalid. Destroy failed. | Ad id : " + id, "");
        }
    }
}
