package com.molpay.molpayxdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.molpay.molpayxdk.googlepay.ActivityGP;
import com.molpay.molpayxdk.utils.DeviceInfoUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MOLPayActivity extends AppCompatActivity {

    public final static int MOLPayXDK = 9999;
    public final static String MOLPayPaymentDetails = "paymentDetails";
    public final static String MOLPayTransactionResult = "transactionResult";
    public final static String mp_amount = "mp_amount";
    public final static String mp_username = "mp_username";
    public final static String mp_password = "mp_password";
    public final static String mp_merchant_ID = "mp_merchant_ID";
    public final static String mp_app_name = "mp_app_name";
    public final static String mp_order_ID = "mp_order_ID";
    public final static String mp_extended_vcode = "mp_extended_vcode";
    public final static String mp_currency = "mp_currency";
    public final static String mp_country = "mp_country";
    public final static String mp_verification_key = "mp_verification_key";
    public final static String mp_channel = "mp_channel";
    public final static String mp_bill_description = "mp_bill_description";
    public final static String mp_bill_name = "mp_bill_name";
    public final static String mp_bill_email = "mp_bill_email";
    public final static String mp_bill_mobile = "mp_bill_mobile";
    public final static String mp_channel_editing = "mp_channel_editing";
    public final static String mp_editing_enabled = "mp_editing_enabled";
    public final static String mp_transaction_id = "mp_transaction_id";
    public final static String mp_request_type = "mp_request_type";
    public final static String mp_ga_enabled = "mp_ga_enabled";
    public final static String mp_filter = "mp_filter";
    public final static String mp_custom_css_url = "mp_custom_css_url";
    public final static String mp_is_escrow = "mp_is_escrow";
    public final static String mp_bin_lock = "mp_bin_lock";
    public final static String mp_bin_lock_err_msg = "mp_bin_lock_err_msg";
    public final static String mp_preferred_token = "mp_preferred_token";
    public final static String mp_tcctype = "mp_tcctype";
    public final static String mp_is_recurring = "mp_is_recurring";
    public final static String mp_allowed_channels = "mp_allowed_channels";
    public final static String mp_sandbox_mode = "mp_sandbox_mode";
    public final static String mp_secured_verified = "mp_secured_verified";
    public final static String mp_express_mode = "mp_express_mode";
    public final static String mp_advanced_email_validation_enabled = "mp_advanced_email_validation_enabled";
    public final static String mp_advanced_phone_validation_enabled = "mp_advanced_phone_validation_enabled";
    public final static String mp_bill_name_edit_disabled = "mp_bill_name_edit_disabled";
    public final static String mp_bill_email_edit_disabled = "mp_bill_email_edit_disabled";
    public final static String mp_bill_mobile_edit_disabled = "mp_bill_mobile_edit_disabled";
    public final static String mp_bill_description_edit_disabled = "mp_bill_description_edit_disabled";
    public final static String mp_dev_mode = "mp_dev_mode";
    public final static String mp_language = "mp_language";
    public final static String mp_cash_waittime = "mp_cash_waittime";
    public final static String mp_non_3DS = "mp_non_3DS";
    public final static String mp_card_list_disabled = "mp_card_list_disabled";
    public final static String mp_disabled_channels = "mp_disabled_channels";
    public final static String mp_dpa_id = "mp_dpa_id";
    public final static String mp_company = "mp_company";
    public final static String mp_closebutton_display = "mp_closebutton_display";
    public final static String mp_metadata = "mp_metadata";
    public final static String device_info = "device_info";

    public final static String MOLPAY = "logMOLPAY";
    private final static String mpopenmolpaywindow = "mpopenmolpaywindow://";
    private final static String mpcloseallwindows = "mpcloseallwindows://";
    private final static String mptransactionresults = "mptransactionresults://";
    private final static String mprunscriptonpopup = "mprunscriptonpopup://";
    private final static String mppinstructioncapture = "mppinstructioncapture://";
    private final static String mpclickgpbutton = "mpclickgpbutton://";
    private final static String module_id = "module_id";
    private final static String wrapper_version = "wrapper_version";
    private final static String wrapperVersion = "15a-beta";

    private String filename;
    private Bitmap imgBitmap;

    private WebView mpMainUI, mpMOLPayUI, mpBankUI;
    private HashMap<String, Object> paymentDetails;
    private Boolean isMainUILoaded = false;
    private Boolean isClosingReceipt = false;
    private Boolean isClosebuttonDisplay = false;

    private Boolean isTNGResult = false;
    private String channel = "";
    private static final Gson gson =  new Gson();
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Private API
    private void closemolpay() {
        mpMainUI.loadUrl("javascript:closemolpay()");
        if (isClosingReceipt) {
            isClosingReceipt = false;
            finish();
        }
    }

    public static String generateSHA512(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String sendXDKLogs(String reference, String type, String process, String details) {
        try {

            Log.d(MOLPAY, "try sendXDKLogs");

            String urlString = "https://vtapi.merchant.razer.com/api/mobile/vt/logs";

            String datetime = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

            String input = "4faf214e-172b-419f-9fc1-9b12744115eb" + datetime;
            String checksum = generateSHA512(input);
//            String checksum = Helper.getInstance().generateSHA512(PRODUCTION.APP_KEY + datetime);

            Log.d(MOLPAY, "try 1");

            // Build JSON body
            JSONObject deviceInfo = new JSONObject()
                    .put("platform", "Android")
                    .put("os", Build.VERSION.RELEASE)
                    .put("brand", Build.BRAND)
                    .put("model", Build.MODEL)
                    .put("modelNo", Build.DEVICE);

            JSONObject productInfo = new JSONObject()
                    .put("type", "XDK")
                    .put("version", "latest")
                    .put("merchantId", "chageesg_Dev");

            JSONObject logs = new JSONObject()
                    .put("referenceNumber", reference)
                    .put("type", type)
                    .put("process", process)
                    .put("details", details);

            JSONObject data = new JSONObject()
                    .put("deviceInfo", deviceInfo)
                    .put("productInfo", productInfo)
                    .put("logs", logs);

            JSONObject body = new JSONObject()
                    .put("datetime", datetime)
                    .put("checksum", checksum)
                    .put("data", data);

            Log.d(MOLPAY, "try 2");
            Log.d(MOLPAY, "urlString = " + urlString);
            Log.d(MOLPAY, "body = " + body.toString());

            // Build request
            RequestBody requestBody = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                    .url(urlString)
                    .post(requestBody)
                    .build();

            Log.d(MOLPAY, "try 3");

            // Send request
            try (Response response = client.newCall(request).execute()) {

                Log.d(MOLPAY, "try client.newCall");

                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    Log.d(MOLPAY, "returnMsg = " + responseStr);
                    return responseStr;
                } else {
                    Log.e(MOLPAY, "Request failed: " + response.code());
                }
            }

        } catch (Exception e) {
            Log.d(MOLPAY, "catch Exception = " + e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        JSONObject json = new JSONObject(paymentDetails);

        try {
            if (json.has("mp_closebutton_display")) {
                isClosebuttonDisplay = json.getBoolean("mp_closebutton_display");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (isClosebuttonDisplay) {
            getMenuInflater().inflate(R.menu.menu_molpay, menu);
            return super.onCreateOptionsMenu(menu);
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // closebtn clicked
        if (id == R.id.closeBtn) {
            closemolpay();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_molpay);

        paymentDetails = (HashMap<String, Object>) getIntent().getSerializableExtra(MOLPayPaymentDetails);

        // For submodule wrappers
        boolean is_submodule = false;

        isTNGResult = false;

        if (paymentDetails != null) {
            if (paymentDetails.containsKey("is_submodule")) {
                is_submodule = Boolean.parseBoolean(Objects.requireNonNull(paymentDetails.get("is_submodule")).toString());
            }
            String submodule_module_id = null;
            if (paymentDetails.containsKey("module_id")) {
                submodule_module_id = Objects.requireNonNull(paymentDetails.get("module_id")).toString();
            }
            String submodule_wrapper_version = null;
            if (paymentDetails.containsKey("wrapper_version")) {
                submodule_wrapper_version = Objects.requireNonNull(paymentDetails.get("wrapper_version")).toString();
            }
            if (is_submodule && !Objects.equals(submodule_module_id, "") && !Objects.equals(submodule_wrapper_version, "")) {
                paymentDetails.put(module_id, submodule_module_id);
                paymentDetails.put(wrapper_version, wrapperVersion + "." + submodule_wrapper_version);
            } else {
                paymentDetails.put(module_id, "molpay-mobile-xdk-android");
                paymentDetails.put(wrapper_version, wrapperVersion);
            }
            paymentDetails.put(device_info, gson.toJson(DeviceInfoUtil.getDeviceInfo(this)));
        }

        // Bind resources
        mpMainUI = findViewById(R.id.MPMainUI);
        mpMOLPayUI = findViewById(R.id.MPMOLPayUI);

        // Enable js
        mpMainUI.getSettings().setJavaScriptEnabled(true);
        mpMOLPayUI.getSettings().setJavaScriptEnabled(true);

        // Hide UI by default
        mpMOLPayUI.setVisibility(View.GONE);

        // Load the main ui
        mpMainUI.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mpMainUI.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mpMainUI.setWebViewClient(new MPMainUIWebClient());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(mpMainUI, true);
        cookieManager.setAcceptThirdPartyCookies(mpMOLPayUI, true);

        mpMainUI.loadUrl("https://pay.merchant.razer.com/RMS/API/xdk/");

        // Configure MOLPay ui
        mpMOLPayUI.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mpMOLPayUI.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mpMOLPayUI.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mpMOLPayUI.getSettings().setSupportMultipleWindows(true);
        mpMOLPayUI.getSettings().setDomStorageEnabled(true);
        mpMOLPayUI.setWebViewClient(new MPMOLPayUIWebClient());
        mpMOLPayUI.setWebChromeClient(new MPMOLPayUIWebChromeClient());
        mpMOLPayUI.getSettings().setLoadWithOverviewMode(true);
        mpMOLPayUI.getSettings().setUseWideViewPort(true);

        CookieManager.getInstance().setAcceptCookie(true);

        mpMOLPayUI.setLongClickable(true);
        mpMOLPayUI.setOnLongClickListener(view -> {
            //Log.d(MOLPAY, "Long press fired!");
            mpMOLPayUI.evaluateJavascript("document.getElementById(\"qrcode_img\").src", qrdata -> {
                //Log.d(MOLPAY, "QR data = " + qrdata);
                if (qrdata != null && !qrdata.equals("null")) {
                    String imageQrCode = qrdata.replaceAll("data:image/png;base64,", "");
                    //Log.d(MOLPAY, "imageQrCode = " + imageQrCode);
                    byte[] decodedBytes = Base64.decode(imageQrCode, 0);
                    imgBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    filename = Objects.requireNonNull(paymentDetails.get("mp_order_ID")) + ".png";

                    isStoragePermissionGranted();
                }
            });
            return false;
        });

        // Register a callback for handling the back press
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //Log.e("logGooglePay", "WebCore onBackPressed");
                closemolpay();
            }
        };

        // Add the callback to the OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void nativeWebRequestUrlUpdates(String url) {
        //Log.d(MOLPAY, "nativeWebRequestUrlUpdates url = " + url);

        HashMap<String, String> data = new HashMap<>();
        data.put("requestPath", url);

        // Create JSON object for Payment details
        JSONObject json = new JSONObject(data);

        // Init javascript
        mpMainUI.loadUrl("javascript:nativeWebRequestUrlUpdates(" + json + ")");
    }

//    private void nativeWebRequestUrlUpdatesOnFinishLoad(String url) {
//        //Log.d(MOLPAY, "nativeWebRequestUrlUpdatesOnFinishLoad url = " + url);
//
//        HashMap<String, String> data = new HashMap<>();
//        data.put("requestPath", url);
//
//        // Create JSON object for Payment details
//        JSONObject json = new JSONObject(data);
//
//        // Init javascript
//        mpMainUI.loadUrl("javascript:nativeWebRequestUrlUpdatesOnFinishLoad(" + json + ")");
//    }

    private class MPBankUIWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //Log.d(MOLPAY, "MPBankUIWebClient onPageStarted url = " + url);

            if (url != null) {
                nativeWebRequestUrlUpdates(url);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //Log.d(MOLPAY, "MPBankUIWebClient onPageFinished url = " + url);
            nativeWebRequestUrlUpdates(url);
        }
    }

    private class MPMOLPayUIWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //Log.d(MOLPAY, "MPMOLPayUIWebClient onPageStarted url = " + url);

            if (url != null) {
                nativeWebRequestUrlUpdates(url);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            //Log.d(MOLPAY, "MPMOLPayUIWebClient shouldOverrideUrlLoading url = " + url);

            paymentDetails.put(MOLPayActivity.mp_merchant_ID, Objects.requireNonNull(paymentDetails.get("mp_merchant_ID"))); // Your sandbox / production merchant ID
            paymentDetails.put(MOLPayActivity.mp_verification_key, Objects.requireNonNull(paymentDetails.get("mp_verification_key"))); // Your sandbox / production verification key
            paymentDetails.put(MOLPayActivity.mp_app_name, Objects.requireNonNull(paymentDetails.get("mp_app_name")));
            paymentDetails.put(MOLPayActivity.mp_username, Objects.requireNonNull(paymentDetails.get("mp_username")));
            paymentDetails.put(MOLPayActivity.mp_password, Objects.requireNonNull(paymentDetails.get("mp_password")));

            paymentDetails.put(MOLPayActivity.mp_amount, Objects.requireNonNull(paymentDetails.get("mp_amount"))); // Must be in 2 decimal points format
            paymentDetails.put(MOLPayActivity.mp_order_ID, Objects.requireNonNull(paymentDetails.get("mp_order_ID"))); // Must be unique
            paymentDetails.put(MOLPayActivity.mp_currency, Objects.requireNonNull(paymentDetails.get("mp_currency"))); // Must matched mp_country
            paymentDetails.put(MOLPayActivity.mp_country, Objects.requireNonNull(paymentDetails.get("mp_country"))); // Must matched mp_currency
            paymentDetails.put(MOLPayActivity.mp_bill_description, Objects.requireNonNull(paymentDetails.get("mp_bill_description")));
            paymentDetails.put(MOLPayActivity.mp_bill_name, Objects.requireNonNull(paymentDetails.get("mp_bill_name")));
            paymentDetails.put(MOLPayActivity.mp_bill_email, Objects.requireNonNull(paymentDetails.get("mp_bill_email")));
            paymentDetails.put(MOLPayActivity.mp_bill_mobile, Objects.requireNonNull(paymentDetails.get("mp_bill_mobile")));

            Gson gson = new Gson();
            String paymentDetailsString = gson.toJson(paymentDetails);

            // Call your logging method in a background thread
            Executors.newSingleThreadExecutor().execute(() -> {
                // sendXDKLogs(String reference, String type, String process, String details)
                sendXDKLogs("paymentDetails = " + paymentDetailsString , "response MOLPayActivity.java" , "MPMOLPayUIWebClient shouldOverrideUrlLoading" , "url = " + url);
            });

            if (url != null) {
                if (url.contains("scbeasy/easy_app_link.html")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // Define what your app should do if no activity can handle the intent.
//                        e.printStackTrace();
                    }
                    view.evaluateJavascript("document.getElementById(\"ref_no\").value", ref_no -> {
                        //Log.d(MOLPAY, "MPMOLPayUIWebClient trans_id = " + ref_no.replaceAll("\"", ""));
                        view.loadUrl("https://pay.merchant.razer.com/RMS/intermediate_app/loading.php?tranID=" + ref_no.replaceAll("\"", ""));
                    });
                    return true;
                } else if (url.contains("atome-my.onelink.me") ||
                        url.contains("myboost.app") ||
                        url.contains("market://") ||
                        url.contains("intent://") ||
                        url.contains("alipays://") ||
                        url.contains("https://app.shopback.com/pay") ||
                        url.contains("https://m.tngdigital.com.my/s/cashier/")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // Define what your app should do if no activity can handle the intent.
                        //TODO implement logger
//                        e.printStackTrace();
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onPageFinished(final WebView view, String url) {
            //Log.d(MOLPAY, "MPMOLPayUIWebClient onPageFinished url = " + url);
            //            nativeWebRequestUrlUpdates(url);

            if (url.contains("intermediate_appTNG-EWALLET.php") || url.contains("intermediate_app/processing.php")) {

                //Log.d(MOLPAY, "contains url");

                view.evaluateJavascript("document.getElementById(\"systembrowserurl\").innerHTML", s -> {
                    //Log.d(MOLPAY, "MPMOLPayUIWebClient base64String = " + s);
                    // Decode base64
                    byte[] data = Base64.decode(s, Base64.DEFAULT);
                    String dataString = new String(data);
                    //Log.d(MOLPAY, "MPBankUIWebClient dataString = " + dataString);

                    if (!s.isEmpty()) {
                        //Log.d(MOLPAY, "MPMOLPayUIWebClient success");
                        isTNGResult = true;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(dataString));
                        startActivity(intent);
                    } else {
                        //Log.d(MOLPAY, "MPMOLPayUIWebClient empty dataString");
                    }
                });

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Custom onResume condition for TNG only
        if (mpMOLPayUI != null && !paymentDetails.isEmpty() && isTNGResult) {
            //Log.d(MOLPAY , "onResume TNG condition");
            closemolpay();
        }
    }

    private class MPMOLPayUIWebChromeClient extends WebChromeClient {
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {

            //Log.d(MOLPAY, "MPMOLPayUIWebChromeClient onCreateWindow resultMsg = " + resultMsg);

            RelativeLayout container = findViewById(R.id.MPContainer);

            mpBankUI = new WebView(MOLPayActivity.this);

            mpBankUI.getSettings().setJavaScriptEnabled(true);
            mpBankUI.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            mpBankUI.getSettings().setAllowUniversalAccessFromFileURLs(true);
            mpBankUI.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            mpBankUI.getSettings().setSupportMultipleWindows(true);

            mpBankUI.setWebViewClient(new MPBankUIWebClient());
            mpBankUI.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onCloseWindow(WebView window) {
                    closemolpay();
                }
            });

            mpBankUI.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            container.addView(mpBankUI);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mpBankUI);
            resultMsg.sendToTarget();
            return true;

        }
    }

    private class MPMainUIWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //Log.d(MOLPAY, "MPMainUIWebClient shouldOverrideUrlLoading url = " + url);

            paymentDetails.put(MOLPayActivity.mp_merchant_ID, Objects.requireNonNull(paymentDetails.get("mp_merchant_ID"))); // Your sandbox / production merchant ID
            paymentDetails.put(MOLPayActivity.mp_verification_key, Objects.requireNonNull(paymentDetails.get("mp_verification_key"))); // Your sandbox / production verification key
            paymentDetails.put(MOLPayActivity.mp_app_name, Objects.requireNonNull(paymentDetails.get("mp_app_name")));
            paymentDetails.put(MOLPayActivity.mp_username, Objects.requireNonNull(paymentDetails.get("mp_username")));
            paymentDetails.put(MOLPayActivity.mp_password, Objects.requireNonNull(paymentDetails.get("mp_password")));

            paymentDetails.put(MOLPayActivity.mp_amount, Objects.requireNonNull(paymentDetails.get("mp_amount"))); // Must be in 2 decimal points format
            paymentDetails.put(MOLPayActivity.mp_order_ID, Objects.requireNonNull(paymentDetails.get("mp_order_ID"))); // Must be unique
            paymentDetails.put(MOLPayActivity.mp_currency, Objects.requireNonNull(paymentDetails.get("mp_currency"))); // Must matched mp_country
            paymentDetails.put(MOLPayActivity.mp_country, Objects.requireNonNull(paymentDetails.get("mp_country"))); // Must matched mp_currency
            paymentDetails.put(MOLPayActivity.mp_bill_description, Objects.requireNonNull(paymentDetails.get("mp_bill_description")));
            paymentDetails.put(MOLPayActivity.mp_bill_name, Objects.requireNonNull(paymentDetails.get("mp_bill_name")));
            paymentDetails.put(MOLPayActivity.mp_bill_email, Objects.requireNonNull(paymentDetails.get("mp_bill_email")));
            paymentDetails.put(MOLPayActivity.mp_bill_mobile, Objects.requireNonNull(paymentDetails.get("mp_bill_mobile")));

            Gson gson = new Gson();
            String paymentDetailsString = gson.toJson(paymentDetails);

            // Call your logging method in a background thread
            Executors.newSingleThreadExecutor().execute(() -> {
                // sendXDKLogs(String reference, String type, String process, String details)
                sendXDKLogs("paymentDetails = " + paymentDetailsString , "response MOLPayActivity.java" , "MPMainUIWebClient shouldOverrideUrlLoading" , "url = " + url);
            });

            if (url != null) {
                if (url.startsWith(mpopenmolpaywindow)) {
                    String base64String = url.replace(mpopenmolpaywindow, "");
                    //Log.d(MOLPAY, "MPMainUIWebClient mpopenmolpaywindow base64String = " + base64String);

                    // Decode base64
                    byte[] data = Base64.decode(base64String, Base64.DEFAULT);
                    String dataString = new String(data);
                    //Log.d(MOLPAY, "MPMainUIWebClient mpopenmolpaywindow dataString = " + dataString);

                    Pattern pattern = Pattern.compile("name=\"payment_gateway\"\\s+value=\"([^\"]+)\"");
                    Matcher matcher = pattern.matcher(dataString);

                    if (matcher.find()) {
                        String value = matcher.group(1);
                        Log.d(MOLPAY, "payment_gateway value = " + value);
                        channel = value;
                    }

                    if (!dataString.isEmpty()) {
                        //Log.d(MOLPAY, "MPMainUIWebClient mpopenmolpaywindow success");
                        if (mpMOLPayUI != null) {
                            //Log.d(MOLPAY, "mpMOLPayUI not NULL update UI");
                            mpMOLPayUI.loadDataWithBaseURL("", dataString, "text/html", "UTF-8", "");
                            mpMOLPayUI.setVisibility(View.VISIBLE);
                        } else {
                            //Log.d(MOLPAY, "mpMOLPayUI NULL avoid crash");
                        }
                    } else {
                        //Log.d(MOLPAY, "MPMainUIWebClient mpopenmolpaywindow empty dataString");
                    }

                } else if (url.startsWith(mpcloseallwindows)) {
                    if (mpBankUI != null) {
                        mpBankUI.loadUrl("about:blank");
                        mpBankUI.setVisibility(View.GONE);
                        mpBankUI.clearCache(true);
                        mpBankUI.clearHistory();
                        mpBankUI.removeAllViews();
                        mpBankUI.destroy();
                        mpBankUI = null;
                    }

                    Log.d(MOLPAY, "if channel = " + channel);

                    if (mpMOLPayUI != null && !channel.equalsIgnoreCase("PayNow")) {
                        Log.d(MOLPAY, "Close mpMOLPayUI");
                        mpMOLPayUI.loadUrl("about:blank");
                        mpMOLPayUI.setVisibility(View.GONE);
                        mpMOLPayUI.clearCache(true);
                        mpMOLPayUI.clearHistory();
                        mpMOLPayUI.removeAllViews();
                        mpMOLPayUI.destroy();
                        mpMOLPayUI = null;
                    } else {
                        Log.d(MOLPAY, "Not Close mpMOLPayUI");
                    }
                } else if (url.startsWith(mptransactionresults)) {
                    String base64String = url.replace(mptransactionresults, "");
                    //Log.d(MOLPAY, "MPMainUIWebClient mptransactionresults base64String = " + base64String);

                    // Decode base64
                    byte[] data = Base64.decode(base64String, Base64.DEFAULT);
                    String dataString = new String(data);
                    //Log.d(MOLPAY, "MPMainUIWebClient mptransactionresults dataString = " + dataString);

                    Intent result = new Intent();
                    result.putExtra(MOLPayTransactionResult, dataString);

                    if (isJSONValid(dataString)) {
                        //Log.d(MOLPAY, "isJSONValid setResult");
                        setResult(RESULT_OK, result);

                        // Check if mp_request_type is "Receipt", if it is, don't finish()
                        try {
                            JSONObject jsonResult = new JSONObject(dataString);

                            //Log.d(MOLPAY, "MPMainUIWebClient jsonResult = " + jsonResult);

                            if (!jsonResult.has("mp_request_type") || !jsonResult.getString("mp_request_type").equals("Receipt") || jsonResult.has("error_code")) {
                                finish();
                            } else {
                                // Next close button click will finish() the activity
                                isClosingReceipt = true;
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                            }
                        } catch (Throwable t) {
                            finish();
                        }
                    } else {
                        //Log.d(MOLPAY, "json not valid dont setResult");
//                    setResult(RESULT_CANCELED, result);
                    }

                } else if (url.startsWith(mprunscriptonpopup)) {
                    String base64String = url.replace(mprunscriptonpopup, "");
                    //Log.d(MOLPAY, "MPMainUIWebClient mprunscriptonpopup base64String = " + base64String);

                    // Decode base64
                    byte[] data = Base64.decode(base64String, Base64.DEFAULT);
                    String jsString = new String(data);
                    //Log.d(MOLPAY, "MPMainUIWebClient mprunscriptonpopup jsString = " + jsString);

                    if (mpBankUI != null) {
                        mpBankUI.loadUrl("javascript:" + jsString);
                        //Log.d(MOLPAY, "mpBankUI loadUrl = " + "javascript:" + jsString);
                    }

                } else if (url.startsWith(mppinstructioncapture)) {
                    String base64String = url.replace(mppinstructioncapture, "");
                    //Log.d(MOLPAY, "MPMainUIWebClient mppinstructioncapture base64String = " + base64String);

                    // Decode base64
                    byte[] data = Base64.decode(base64String, Base64.DEFAULT);
                    String dataString = new String(data);
                    //Log.d(MOLPAY, "MPMainUIWebClient mppinstructioncapture dataString = " + dataString);

                    try {
                        JSONObject jsonResult = new JSONObject(dataString);

                        String base64Img = jsonResult.getString("base64ImageUrlData");
                        filename = jsonResult.getString("filename");
                        //Log.d(MOLPAY, "MPMainUIWebClient jsonResult = " + jsonResult);

                        byte[] decodedBytes = Base64.decode(base64Img, 0);
                        imgBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                        isStoragePermissionGranted();

                    } catch (Throwable t) {
                        //Log.d(MOLPAY, "MPMainUIWebClient jsonResult error = " + t);
                    }

                } else if (url.startsWith(mpclickgpbutton)) {

                    // Extended VCode setting
                    if (paymentDetails.get("mp_extended_vcode") == null) {
                        paymentDetails.put(MOLPayActivity.mp_extended_vcode, false);
                    } else {
                        paymentDetails.put(MOLPayActivity.mp_extended_vcode, Objects.requireNonNull(paymentDetails.get("mp_extended_vcode")));
                    }

                    if (paymentDetails.get("mp_sandbox_mode") == null) {
                        paymentDetails.put(MOLPayActivity.mp_sandbox_mode, false);
                    } else {
                        paymentDetails.put(MOLPayActivity.mp_sandbox_mode, Objects.requireNonNull(paymentDetails.get("mp_sandbox_mode")));
                    }

                    paymentDetails.put(MOLPayActivity.mp_merchant_ID, Objects.requireNonNull(paymentDetails.get("mp_merchant_ID"))); // Your sandbox / production merchant ID
                    paymentDetails.put(MOLPayActivity.mp_verification_key, Objects.requireNonNull(paymentDetails.get("mp_verification_key"))); // Your sandbox / production verification key
                    paymentDetails.put(MOLPayActivity.mp_amount, Objects.requireNonNull(paymentDetails.get("mp_amount"))); // Must be in 2 decimal points format
                    paymentDetails.put(MOLPayActivity.mp_order_ID, Objects.requireNonNull(paymentDetails.get("mp_order_ID"))); // Must be unique
                    paymentDetails.put(MOLPayActivity.mp_currency, Objects.requireNonNull(paymentDetails.get("mp_currency"))); // Must matched mp_country
                    paymentDetails.put(MOLPayActivity.mp_country, Objects.requireNonNull(paymentDetails.get("mp_country"))); // Must matched mp_currency
                    paymentDetails.put(MOLPayActivity.mp_bill_description, Objects.requireNonNull(paymentDetails.get("mp_bill_description")));
                    paymentDetails.put(MOLPayActivity.mp_bill_name, Objects.requireNonNull(paymentDetails.get("mp_bill_name")));
                    paymentDetails.put(MOLPayActivity.mp_bill_email, Objects.requireNonNull(paymentDetails.get("mp_bill_email")));
                    paymentDetails.put(MOLPayActivity.mp_bill_mobile, Objects.requireNonNull(paymentDetails.get("mp_bill_mobile")));

                    openGPActivityWithResult();
                }
            }

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!isMainUILoaded && !url.equals("about:blank")) {

                isMainUILoaded = true;

                // Create JSON object for Payment details
                JSONObject json = new JSONObject(paymentDetails);
                //Log.d(MOLPAY, "MPMainUIWebClient onPageFinished paymentDetails = " + json);

                // Init javascript
                mpMainUI.loadUrl("javascript:updateSdkData(" + json + ")");

            }
        }

    }

    private void openGPActivityWithResult() {
        //Log.d(MOLPAY, "openGPActivityWithResult paymentDetails = " + paymentDetails);

        Intent intent = new Intent(this, ActivityGP.class);
        intent.putExtra(MOLPayActivity.MOLPayPaymentDetails, paymentDetails);
        gpActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> gpActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //Log.d("MOLPAYXDKLibrary", "result: " + result);
                //Log.d("MOLPAYXDKLibrary", "result: " + result.getResultCode());

                if (result.getResultCode() == MOLPayActivity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String transactionResult = data.getStringExtra(MOLPayActivity.MOLPayTransactionResult);

                    if (transactionResult != null) {
                        //Log.e("logGooglePay", "MOLPAY result = " + transactionResult);

                        Intent intent = new Intent();
                        intent.putExtra(MOLPayTransactionResult, transactionResult);
                        setResult(result.getResultCode(), intent);
                        finish();
                    }
                }
            }
    );

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // in case JSONArray is valid as well
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private void storeImage(Bitmap image) {
        String fullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

        try {

            FileOutputStream fOut;
            File file = new File(fullPath, filename);
            file.createNewFile();
            fOut = new FileOutputStream(file);

            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null, null);

            Toast toast = Toast.makeText(this, "Image saved", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

        } catch (Exception e) {
            //Log.d(MOLPAY, "MPMainUIWebClient storeImage error = " + e.getMessage());
            Toast toast = Toast.makeText(this, "Image not saved", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @TargetApi(23)
    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Log.d(MOLPAY, "isStoragePermissionGranted Permission granted");
                storeImage(imgBitmap);
            } else {
                //Log.d(MOLPAY, "isStoragePermissionGranted Permission revoked");
                ActivityCompat.requestPermissions(MOLPayActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            }

        } else { //permission is automatically granted on sdk<23 upon installation
            //Log.d(MOLPAY, "isStoragePermissionGranted Permission granted on sdk<23");
            storeImage(imgBitmap);
        }
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Log.d(MOLPAY, "onRequestPermissionsResult Permission: " + permissions[0] + "was " + grantResults[0]);
                //resume tasks needing this permission

                storeImage(imgBitmap);

            } else {
                //Log.d(MOLPAY, "onRequestPermissionsResult EXTERNAL_STORAGE permission was NOT granted.");
                Toast.makeText(this, "Image not saved", Toast.LENGTH_LONG).show();
            }
        }
    }
}
