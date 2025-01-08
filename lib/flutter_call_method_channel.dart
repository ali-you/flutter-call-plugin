import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_call_platform_interface.dart';

/// An implementation of [FlutterCallPlatform] that uses method channels.
class MethodChannelFlutterCall extends FlutterCallPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_call');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
