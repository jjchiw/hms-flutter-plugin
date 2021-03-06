/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import 'package:flutter/services.dart' show MethodCall;
import 'package:huawei_scan/hmsCustomizedView/CustomizedViewRequest.dart';
import 'package:huawei_scan/HmsScan.dart';
import 'package:huawei_scan/model/ScanResponse.dart';

class HmsCustomizedView {
  static CustomizedViewRequest customizedViewRequest;

  static Future<dynamic> customizedMethodCallHandler(MethodCall call) async {
    if (call.method == "CustomizedViewResponse") {
      ScanResponse response = ScanResponse.fromJson(call.arguments);
      HmsCustomizedView.customizedViewRequest
          .customizedCameraListener(response);
    }
  }

  static Future<dynamic> listenCustomizedLifecycle(MethodCall call) async {
    switch (call.method) {
      case "onStart":
        customizedViewRequest
            .customizedLifeCycleListener(CustomizedViewEvent.onStart);
        break;
      case "onResume":
        customizedViewRequest
            .customizedLifeCycleListener(CustomizedViewEvent.onResume);
        break;
      case "onPause":
        customizedViewRequest
            .customizedLifeCycleListener(CustomizedViewEvent.onPause);
        break;
      case "onDestroy":
        customizedViewRequest
            .customizedLifeCycleListener(CustomizedViewEvent.onDestroy);
        break;
      case "onStop":
        customizedViewRequest
            .customizedLifeCycleListener(CustomizedViewEvent.onStop);
        break;
    }
  }

  static Future<ScanResponse> startCustomizedView(
      CustomizedViewRequest request) async {
    customizedViewRequest = request;
    final ScanResponse result = ScanResponse.fromJson(await HmsScan
        .instance.customizedViewChannel
        .invokeMethod("customizedView", request.toMap()));
    customizedViewRequest = null;
    return result;
  }

  static Future<void> pauseScan() async {
    await HmsScan.instance.remoteViewChannel.invokeMethod("pause");
  }

  static Future<void> resumeScan() async {
    await HmsScan.instance.remoteViewChannel.invokeMethod("resume");
  }

  static Future<void> switchLight() async {
    await HmsScan.instance.remoteViewChannel.invokeMethod("switchLight");
  }

  static Future<bool> getLightStatus() async {
    return await HmsScan.instance.remoteViewChannel
        .invokeMethod("getLightStatus");
  }
}
