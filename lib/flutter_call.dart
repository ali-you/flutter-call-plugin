import 'flutter_call_platform_interface.dart';

class FlutterCall {
  Future<bool> callNumber(String number, {int? simSlot}) async {
    final bool res =
        await FlutterCallPlatform.instance.callNumber(number, simSlot: simSlot);
    return res;
  }

  Future<bool> requestPermission() async {
    final bool res = await FlutterCallPlatform.instance.requestPermission();
    return res;
  }

  Future<String?> permissionStatus() async {
    final String? res = await FlutterCallPlatform.instance.permissionStatus();
    return res;
  }

  Future<List> getSimSlots() async {
    final List res = await FlutterCallPlatform.instance.getSimSlots();
    return res;
  }
}
