import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_call_platform_interface.dart';

/// An implementation of [FlutterCallPlatform] that uses method channels.
class MethodChannelFlutterCall extends FlutterCallPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_call.aliyou.dev');


  @override
  Future<bool> callNumber(String number, {int? simSlot}) async {
    final result = await methodChannel.invokeMethod<bool>('callNumber', {
      'number': number,
      'simSlot': simSlot,
    });
    return result ?? false;
  }

  @override
  Future<bool> requestPermission() async {
    final result = await methodChannel.invokeMethod<bool>('requestPermission');
    return result ?? false;
  }

  @override
  Future<String?> permissionStatus() async {
    final result = await methodChannel.invokeMethod<String>('getPermissionStatus');
    return result;
  }

  @override
  Future<List> getSimSlots() async {
    final result = await methodChannel.invokeMethod<List>('getSimSlots');
    return result ?? [];
  }
}
