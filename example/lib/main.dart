import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_call/flutter_call.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _callResult = false;
  final _flutterCallPlugin = FlutterCall();

  Future<void> call() async {
    bool res;
    try {
      res = await _flutterCallPlugin.callNumber("+12125551212", simSlot: 0);
    } on PlatformException {
      rethrow;
    }
    if (!mounted) return;
    setState(() {
      _callResult = res;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              ElevatedButton(
                  onPressed: () async {
                    print(await _flutterCallPlugin.permissionStatus());
                  },
                  child: Text("Permission Status")),
              ElevatedButton(
                  onPressed: () async {
                    print(await _flutterCallPlugin.getSimSlots());
                  },
                  child: Text("Sim Slots")),
              ElevatedButton(
                  onPressed: () {
                    _flutterCallPlugin.requestPermission();
                  },
                  child: Text("Request Permission")),
              ElevatedButton(onPressed: call, child: Text("Call Number")),
              Text('Call number result: $_callResult')
            ],
          ),
        ),
      ),
    );
  }
}
