package com.aliyou.flutter_call

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener
import android.telephony.TelephonyManager
import android.provider.Settings

class FlutterCallPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, RequestPermissionsResultListener {
    private lateinit var channel: MethodChannel
    private var activityBinding: ActivityPluginBinding? = null
    private var flutterResult: Result? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_call.aliyou.dev")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activityBinding = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activityBinding = binding
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activityBinding = null
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onMethodCall(call: MethodCall, result: Result) {
        flutterResult = result
        when (call.method) {
            "callNumber" -> {
                val number = call.argument<String>("number")
                val simSlot = call.argument<Int>("simSlot")
                if (number != null) {
                    handleCallNumber(number, simSlot, result)
                } else {
                    result.error("INVALID_ARGUMENT", "Phone number is null", null)
                }
            }

            "requestPermission" -> {
                requestPermission()
            }

            "getPermissionStatus" -> {
                result.success(getPermissionStatus())
            }

            "getSimSlots" -> {
                val simSlots = getSimSlots()
                result.success(simSlots)
            }

            else -> result.notImplemented()

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        if (requestCode == CALL_REQ_CODE) {
            val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            flutterResult?.success(granted)
            return true
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun handleCallNumber(number: String, simSlot: Int?, result: Result) {
        val formattedNumber = number.replace("#", "%23").let {
            if (!it.startsWith("tel:")) "tel:$it" else it
        }

        when (getPermissionStatus()) {
            "granted" -> {
                try {
                    if (simSlot == null) {
                        // If no slot is provided, show a dialog or fallback logic.
                        result.error("SIM_SLOT_REQUIRED", "No SIM slot specified. Please select a SIM slot.", null)
                    } else {
                        result.success(callPhoneNumber(formattedNumber, simSlot))
                    }
                } catch (e: Exception) {
                    result.error("CALL_ERROR", "Error making the call: ${e.message}", null)
                }
            }

            "denied" -> {
                requestPermission()
            }

            "permanently_denied" -> {
                result.error("PERMISSION_DENIED", "Permission permanently denied. Enable it from settings.", null)
            }
        }
    }

    private fun requestPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            true
        } else {
            // Permission not granted, check if we can show rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CALL_PHONE)) {
                // Show rationale dialog
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CALL_PHONE), CALL_REQ_CODE)
                false
            } else {
                // Permission is permanently denied, open the app settings page
                openAppSettings()
                false
            }
        }
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app settings: ${e.message}")
        }
    }


    private fun getPermissionStatus(): String {
        return when {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED -> {
                "granted"
            }

            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CALL_PHONE) -> {
                "denied"
            }

            else -> {
                "permanently_denied"
            }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
//    private fun callPhoneNumber(number: String, simSlot: Int): Boolean {
//        return try {
//            val subscriptionManager = activity.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
//            val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
//
//            if (subscriptionInfoList.isNullOrEmpty()) {
//                throw IllegalArgumentException("No active SIM slots found.")
//            }
//
//            // Get the SubscriptionInfo for the selected SIM slot
//            val subscriptionInfo = subscriptionInfoList.find { it.simSlotIndex == simSlot }
//                    ?: throw IllegalArgumentException("Invalid SIM slot selected: $simSlot")
//
//            // Get the phone account handle for the subscription ID
//            val telecomManager = activity.getSystemService(Context.TELECOM_SERVICE) as android.telecom.TelecomManager
//            val phoneAccountHandle = telecomManager.callCapablePhoneAccounts.find { handle ->
//                handle.id.contains(subscriptionInfo.subscriptionId.toString())
//            } ?: throw IllegalArgumentException("No matching PhoneAccountHandle found for SIM slot: $simSlot")
//
//            // Create the intent to make the call
//            val formattedNumber = number.replace("#", "%23") // Encode `#` for tel URI
//            val intent = Intent(Intent.ACTION_CALL).apply {
//                data = Uri.parse("tel:$formattedNumber")
//                putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandle)
//            }
//
//            activity.startActivity(intent)
//            true
//        } catch (e: Exception) {
//            Log.e(TAG, "Error calling number: ${e.message}")
//            false
//        }
//    }


    private fun callPhoneNumber(number: String, simSlot: Int): Boolean {
        return try {
            val intent = Intent(if (isTelephonyEnabled) Intent.ACTION_CALL else Intent.ACTION_VIEW)
            intent.data = Uri.parse(number)
            activity.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.d("Caller", "error: " + e.message)
            false
        }
    }

    private val isTelephonyEnabled: Boolean
        get() {
            val tm = activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.phoneType != TelephonyManager.PHONE_TYPE_NONE
        }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun getSimSlots(): List<Map<String, Any>> {
        val subscriptionManager = activity.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList

        val simSlots = mutableListOf<Map<String, Any>>()

        subscriptionInfoList?.forEach { info ->
            val slotMap = mapOf("slotIndex" to info.simSlotIndex, "displayName" to (info.displayName?.toString()
                    ?: "Unknown"), "carrierName" to (info.carrierName?.toString() ?: "Unknown"))
            simSlots.add(slotMap)
        }

        return simSlots
    }

    private val activity: Activity
        get() = activityBinding?.activity ?: throw IllegalStateException("Activity is not attached")

    companion object {
        private const val TAG = "FlutterCallPlugin"
        private const val CALL_REQ_CODE = 100
    }
}
