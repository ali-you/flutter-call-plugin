import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_call/flutter_call.dart';
import 'package:flutter_call/flutter_call_platform_interface.dart';
import 'package:flutter_call/flutter_call_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterCallPlatform
    with MockPlatformInterfaceMixin
    implements FlutterCallPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterCallPlatform initialPlatform = FlutterCallPlatform.instance;

  test('$MethodChannelFlutterCall is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterCall>());
  });

  test('getPlatformVersion', () async {
    FlutterCall flutterCallPlugin = FlutterCall();
    MockFlutterCallPlatform fakePlatform = MockFlutterCallPlatform();
    FlutterCallPlatform.instance = fakePlatform;

    expect(await flutterCallPlugin.getPlatformVersion(), '42');
  });
}
