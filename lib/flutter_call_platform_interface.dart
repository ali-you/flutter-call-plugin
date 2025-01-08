import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_call_method_channel.dart';

abstract class FlutterCallPlatform extends PlatformInterface {
  /// Constructs a FlutterCallPlatform.
  FlutterCallPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterCallPlatform _instance = MethodChannelFlutterCall();

  /// The default instance of [FlutterCallPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterCall].
  static FlutterCallPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterCallPlatform] when
  /// they register themselves.
  static set instance(FlutterCallPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool> callNumber(String number, {int? simSlot}) =>
      throw UnimplementedError('callNumber() has not been implemented.');

  Future<bool> requestPermission() =>
      throw UnimplementedError('requestPermission() has not been implemented.');

  Future<String?> permissionStatus() =>
      throw UnimplementedError('permissionStatus() has not been implemented.');

  Future<List> getSimSlots() =>
      throw UnimplementedError('getSimSlot() has not been implemented.');
}
