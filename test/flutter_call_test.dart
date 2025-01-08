import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_call/flutter_call.dart';
import 'package:flutter_call/flutter_call_platform_interface.dart';
import 'package:flutter_call/flutter_call_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterCallPlatform
    with MockPlatformInterfaceMixin
    implements FlutterCallPlatform {
  @override
  Future<bool> callNumber(String number, {int? simSlot}) => Future.value(true);

  @override
  Future<bool> requestPermission() => Future.value(true);

  @override
  Future<String?> permissionStatus() => Future.value("granted");

  @override
  Future<List> getSimSlots() => throw UnimplementedError();
}

void main() {
  final FlutterCallPlatform initialPlatform = FlutterCallPlatform.instance;

  test('$MethodChannelFlutterCall is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterCall>());
  });

  test('callNumber', () async {
    FlutterCall flutterCallPlugin = FlutterCall();
    MockFlutterCallPlatform fakePlatform = MockFlutterCallPlatform();
    FlutterCallPlatform.instance = fakePlatform;

    expect(await flutterCallPlugin.callNumber("+12125551212"), true);
  });
}
