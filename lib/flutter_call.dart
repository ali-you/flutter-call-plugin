
import 'flutter_call_platform_interface.dart';

class FlutterCall {
  Future<String?> getPlatformVersion() {
    return FlutterCallPlatform.instance.getPlatformVersion();
  }
}
