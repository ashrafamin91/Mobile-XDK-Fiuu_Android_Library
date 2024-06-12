/*
 * Copyright 2023 Razer Merchant Services.
 */

package com.molpay.molpayxdk.googlepay;

import android.content.Intent;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.molpay.molpayxdk.R;
import com.molpay.molpayxdk.googlepay.Helper.RMSGooglePay;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;

public class WebActivity extends AppCompatActivity {

    private WebView wvGateway;
    private ProgressBar pbLoading;
    private AppCompatTextView tvLoading;
    public Transaction transaction = new Transaction();

    public static boolean statCodeValueSuccess = false;

    public static String isSandbox;

    private CountDownTimer countDownTimer;
    private String requestType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("logGooglePay" , "WebActivity");

        setContentView(R.layout.activity_web);

        Intent intent = getIntent();
        String paymentInput = intent.getStringExtra("paymentInput");
        String paymentInfo = intent.getStringExtra("paymentInfo");

        Log.e("logGooglePay" , "WebActivity paymentInput = " + paymentInput);

        // Transcation model from paymentInput
        JSONObject paymentInputObj = null;
        try {
            paymentInputObj = new JSONObject(paymentInput);
            transaction.setVkey(paymentInputObj.getString("verificationKey"));
            isSandbox = paymentInputObj.getString("isSandbox");
            Log.e("logGooglePay" , "WebActivity isSandbox = " + isSandbox);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tvLoading = findViewById(R.id.tvLoading);
        pbLoading = findViewById(R.id.pbLoading);
        wvGateway = findViewById(R.id.webView);
        wvGateway.setBackgroundColor(Color.WHITE);
        wvGateway.getSettings().setDomStorageEnabled(true);
        wvGateway.getSettings().setJavaScriptEnabled(true);
        wvGateway.getSettings().setAllowUniversalAccessFromFileURLs(true);
        wvGateway.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wvGateway.getSettings().setSupportMultipleWindows(true);

        wvGateway.getSettings().setLoadWithOverviewMode(true);
        wvGateway.getSettings().setUseWideViewPort(true);
        wvGateway.getSettings().setSupportZoom(true);
        wvGateway.getSettings().setBuiltInZoomControls(true);
        wvGateway.getSettings().setDisplayZoomControls(false);

        PaymentThread paymentThread = new PaymentThread();
        paymentThread.setValue(paymentInput, paymentInfo); // set value
        Thread thread = new Thread(paymentThread);
        thread.start();

        try {
            thread.join();
            JSONObject paymentResult = new JSONObject(new JSONObject(paymentThread.getValue()).getString("responseBody"));
            onRequestData(paymentResult);
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }

    }

    private void onStartTimOut() {

        long minTimeOut = 180000; // 3 minutes
        long interval = 6000;
        final String[] queryResultStr = {null};
        final String[] trasactionJsonStr = {null};

        // Query Transaction ID for every 6 second in 3 minutes
        countDownTimer = new CountDownTimer(minTimeOut, interval) {

            // Query Transaction ID for every 6 second in 3 minutes
            @Override
            public void onTick(long millisUntilFinished) {

                JSONObject transactionObject = new JSONObject();
                try {
                    transactionObject.put("txID", transaction.getTxID());
                    transactionObject.put("amount", transaction.getAmount());
                    transactionObject.put("merchantId", transaction.getDomain());
                    transactionObject.put("verificationKey", transaction.getVkey());
                    trasactionJsonStr[0] = transactionObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.e("logGooglePay" , "onTick trasactionJsonStr = " + Arrays.toString(trasactionJsonStr));

                QueryResultThread queryResultThread = new QueryResultThread();
                queryResultThread.setValue(trasactionJsonStr[0]); // set value

                Thread thread = new Thread(queryResultThread);
                thread.start();

                try {
                    thread.join();
                    queryResultStr[0] = queryResultThread.getValue();

                    try {
                        JSONObject queryResultObj = new JSONObject(queryResultStr[0]);
                        String responseBody = queryResultObj.getString("responseBody");
                        JSONObject responseBodyObj = new JSONObject(responseBody);

                        // If StatCode
                        if (responseBodyObj.has("StatCode")){
                            String statCodeValue = responseBodyObj.getString("StatCode");

                            Intent intent = new Intent();
                            intent.putExtra("response", String.valueOf(responseBodyObj));

                            Log.e("logGooglePay" , "statCodeValue " + statCodeValue);

                            if (statCodeValue.equals("00")) {
                                if (statCodeValueSuccess) {
                                    Log.e("logGooglePay" , "statCodeValueSuccess finish");
                                    onFinish();
                                }
                            } else if (statCodeValue.equals("11")) {
                                cancel();
                                pbLoading.setVisibility(View.GONE);
                                tvLoading.setVisibility(View.GONE);

                                String errorCode = null;
                                try {
                                    errorCode = responseBodyObj.getString("ErrorCode");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                String errorDesc = null;
                                try {
                                    errorDesc = responseBodyObj.getString("ErrorDesc");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }

                                new AlertDialog.Builder(WebActivity.this)
                                        .setTitle("Payment Failed")
                                        .setMessage(errorCode + " : " + errorDesc)
                                        .setCancelable(false)
                                        .setPositiveButton("CLOSE", (dialog, which) -> {
                                            setResult(RESULT_CANCELED, intent);
                                            finish();
                                        }).show();
                            }  else if (statCodeValue.equals("22")) {
                                // Do Nothing - It will auto handle
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                try {
                    Log.e("logGooglePay" , "onFinish Arrays queryResultStr = " + Arrays.toString(queryResultStr));

                    JSONObject queryResultObj = new JSONObject(queryResultStr[0]);
                    String responseBody = queryResultObj.getString("responseBody");

                    Log.e("logGooglePay" , "onFinish responseBody = " + responseBody);

                    JSONObject responseBodyObj = new JSONObject(responseBody);

                    Intent intent = new Intent();
                    intent.putExtra("response", String.valueOf(responseBodyObj));

                    Log.e("logGooglePay" , "onFinish response = " + String.valueOf(responseBodyObj));

                    // If timeout / cancel
                    if (!responseBodyObj.has("StatCode")){
                        setResult(RESULT_CANCELED, intent);
                    } else {
                        setResult(RESULT_OK, intent);
                    }

                    countDownTimer.cancel();
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        countDownTimer.start();
    }

    private String xdkHTMLRedirection = "";
    private void onLoadHtmlWebView(String plainHtml) {

//        wvGateway.setVisibility(View.VISIBLE);
        pbLoading.setVisibility(View.VISIBLE);
        tvLoading.setVisibility(View.VISIBLE);
        statCodeValueSuccess = false;

        String encodedHtml = Base64.encodeToString(plainHtml.getBytes(), Base64.NO_PADDING);

        Log.e("logGooglePay" , "onLoadHtmlWebView encodedHtml = " + encodedHtml);
        Log.e("logGooglePay" , "onLoadHtmlWebView requestType = " + requestType);

        if (plainHtml.contains("xdkHTMLRedirection")) {
            Log.e("logGooglePay" , "dapat xdkHTMLRedirection");
            xdkHTMLRedirection = StringUtils.substringBetween(plainHtml, "xdkHTMLRedirection' value='", "'");
            wvGateway.loadData(xdkHTMLRedirection, "text/html", "base64");
        } else if (requestType.equalsIgnoreCase("REDIRECT")) {
            Log.e("logGooglePay" , "masuk requestType = " + requestType);
            wvGateway.loadData(encodedHtml, "text/html", "base64");
            pbLoading.setVisibility(View.GONE);
            tvLoading.setVisibility(View.GONE);
            wvGateway.setVisibility(View.VISIBLE);
            Log.e("logGooglePay" , "lepas loadDataWithBaseURL");
        } else {
            Log.e("logGooglePay" , "encodedHtml = " + encodedHtml);
            wvGateway.loadData(encodedHtml, "text/html", "base64");
        }

        Log.e("logGooglePay" , "lepas return");

        wvGateway.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                Log.e("logGooglePay" , "shouldOverrideUrlLoading request.getUrl = " + request.getUrl().toString());

                if (request.getUrl().toString().contains("result.php")) {
                    statCodeValueSuccess = true;
                    pbLoading.setVisibility(View.VISIBLE);
                    tvLoading.setVisibility(View.VISIBLE);
                    wvGateway.setVisibility(View.GONE);
                }

                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e("logGooglePay" , "onPageFinished url = " + url);
//                view.scrollTo(0, view.getContentHeight());
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.e("logGooglePay" , "onReceivedError request.getUrl = " + request.getUrl().toString());
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                Log.e("logGooglePay" , "onReceivedHttpError request.getUrl = " + request.getUrl().toString());
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.e("logGooglePay" , "onReceivedSslError error.getUrl = " + error.getUrl());
                super.onReceivedSslError(view, handler, error);
            }
        });
    }

    public void onRequestData(JSONObject response) {

        Log.e("logGooglePay" , "onRequestData response = " + response.toString());

        try {
            Log.e("logGooglePay" , "try");

            if (response.has("error_code") && response.has("error_desc")) {
                Intent intent = new Intent();
                String strResponse = response.toString();
                intent.putExtra("response", strResponse);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
            if (response.has("TxnID")) {
                try {
                    transaction.setTxID(response.getString("TxnID"));
                    transaction.setDomain(response.getString("MerchantID"));
                    transaction.setAmount(response.getString("TxnAmount"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (response.has("TxnData") && !response.has("pInstruction")) {

                    Log.e("logGooglePay" , "TxnData && !pInstruction");

                    onStartTimOut();

                    JSONObject txnData = response.getJSONObject("TxnData");

                    StringBuilder html = new StringBuilder();
                    html.append(String.format("<form id='prForm' action='%s' method='%s'>\n",
                            txnData.getString("RequestURL"),
                            txnData.getString("RequestMethod"))
                    );
                    if (txnData.has("AppDeepLinkURL")) {
//                        AppData.getInstance().setRedirectAppUrl(txnData.getString("AppDeepLinkURL"));
                    }
                    if (txnData.has("RequestType")) {
                        Log.e("logGooglePay" , "ada RequestType");
                        requestType = txnData.getString("RequestType");
                    } else {
                        Log.e("logGooglePay" , "no RequestType");
                    }
                    if (txnData.has("RequestData")) {

                        Log.e("logGooglePay" , "has RequestData");

                        if (txnData.get("RequestData") instanceof JSONObject) {
                            Log.e("logGooglePay" , "RequestData instanceof JSONObject");

                            JSONObject requestData = txnData.getJSONObject("RequestData");

                            Iterator<String> keys = requestData.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                Log.e("logGooglePay" , "key = " + key);

                                if (requestData.get(key) instanceof JSONObject) {
                                    // Do nothing
                                    Log.e("logGooglePay" , "key instanceof JSONObject");
                                } else {
                                    Log.e("logGooglePay" , "else");
                                    if (requestData.has("checkoutUrl")) {
                                        Log.e("logGooglePay" , "has checkoutUrl");
//                                        AppData.getInstance().setRedirectAppUrl(requestData.getString("checkoutUrl"));
                                    }
                                    html.append(String.format("<input type='hidden' name='%s' value='%s'>\n", key, requestData.getString(key)));
                                }
                            }
                        }
                    }

                    html.append("</form>");
                    html.append("<script> document.getElementById('prForm').submit();</script>");

                    Log.e("logGooglePay" , "html = " + html.toString());

                    onLoadHtmlWebView(html.toString());
                } else {
                    Intent intent = new Intent();
                    String strResponse = response.toString();
                    intent.putExtra("response", strResponse);
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            } else {
                Intent intent = new Intent();
                String strResponse = response.toString();
                intent.putExtra("response", strResponse);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        } catch (JSONException e) {
            Log.e("logGooglePay" , "catch");
            e.printStackTrace();
        }
    }

    public class PaymentThread implements Runnable {
        private volatile String resp;
        private String paymentInput;
        private String  paymentInfo;

        public String getValue() {
            return resp;
        }

        public void setValue(String paymentInput, String  paymentInfo) {
            this.paymentInput = paymentInput;
            this.paymentInfo = paymentInfo;
        }

        @Override
        public void run() {

                RMSGooglePay pay = new RMSGooglePay();
                JSONObject result = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    result = (JSONObject) pay.requestPayment(paymentInput, paymentInfo);
                }

                resp = result.toString();

        }
    }

    public class QueryResultThread implements Runnable {
        private volatile String resp;
        private String transaction;

        public String getValue() {
            return resp;
        }

        public void setValue(String transaction) {
            this.transaction = transaction;
        }

        @Override
        public void run() {

                RMSGooglePay pay = new RMSGooglePay();
                JSONObject result = (JSONObject) pay.queryPaymentResult(transaction);
                resp = result.toString();

        }
    }

}