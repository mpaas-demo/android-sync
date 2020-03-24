package com.mpaas.demo.sync;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;

import com.alipay.mobile.antui.basic.AUAssistLabelView;
import com.alipay.mobile.antui.basic.AUButton;
import com.alipay.mobile.antui.basic.AUTitleBar;
import com.alipay.mobile.antui.input.AUInputBox;
import com.alipay.mobile.common.logging.api.LoggerFactory;
import com.alipay.mobile.rome.longlinkservice.ConnectionEvent;
import com.alipay.mobile.rome.longlinkservice.ConnectionListener;
import com.alipay.mobile.rome.longlinkservice.ISyncCallback;
import com.alipay.mobile.rome.longlinkservice.syncmodel.SyncMessage;
import com.mpaas.demo.R;
import com.mpaas.mss.adapter.api.MPSync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * Created by larry.cl.
 * 支持基于deviceid和uid的消息同步
 */
public abstract class BaseSyncActivity extends FragmentActivity implements View.OnClickListener {

    protected AUButton mBtnBind;
    protected AUButton mBtnUnBind;
    protected AUInputBox mTvDeviceId;
    protected AUAssistLabelView mBindTips;
    protected AUAssistLabelView mUnBindTips;
    protected AUAssistLabelView mDeviceIdTips;
    //输入用户id
    protected AUInputBox mInputUid;
    //输入sessionId,配合业务端用户一致性验证，非必须（详见：https://www.cloud.alipay.com/docs/2/50971#h2-u7528u6237u4E00u81F4u6027u9A8Cu8BC1）
    protected AUInputBox mInputSessionId;
    public AUAssistLabelView mTvStatus;

    public static final String TAG = "syncTag";
    private AUTitleBar mTitle;

    private ConnectionListener mConnectionListener =
            new ConnectionListener() {
                @Override
                public void onConnectionStateChanged(final ConnectionEvent connectionEvent) {
                    LoggerFactory.getTraceLogger().debug(TAG, connectionEvent.getConnectionStateName());
                    BaseSyncActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvStatus.setText("连接状态 : " + connectionEvent.getConnectionStateName());
                        }
                    });
                }
            };

    protected ISyncCallback mSyncCallback = new ISyncCallback() {
        @Override
        public void onReceiveMessage(SyncMessage syncMessage) {
            //最终的数据是以JSONArray放在 msgData 字段中
            LoggerFactory.getTraceLogger().debug(TAG, "SyncCallback onReceiveMessage:" + syncMessage.msgData + "/" + syncMessage.userId + "/" + syncMessage.biz);
            if (syncMessage != null && !TextUtils.isEmpty(syncMessage.msgData)) {
                try {
                    JSONArray jsonArray = new JSONArray(syncMessage.msgData);
                    int count = jsonArray.length();
                    final StringBuffer dataBuffer = new StringBuffer();
                    for (int index = 0; index < count; index++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(index);
                        //最终需要的数据就在下面字段finalData中。
                        String finalData = jsonObject.optString("pl");
                        //对于isB标志,需要base64解码
                        if ("1".equals(jsonObject.optString("isB"))) {
                            finalData = new String(Base64.decode(finalData, Base64.DEFAULT), "UTF-8");
                        }
                        //解析finalData
                        dataBuffer.append(index + " : " + finalData + "|");
                    }
                    LoggerFactory.getTraceLogger().debug(TAG, "SyncCallback onReceiveMessage dataBuffer : " + dataBuffer.toString());
                    //通知数据已经接收，如果不通知，我们将会重试，上限到6次之后数据就被删除。
                    MPSync.reportMsgReceived(syncMessage);
                    fetchSyncData(dataBuffer.toString());
                } catch (JSONException e) {
                    LoggerFactory.getTraceLogger().debug(TAG, "SyncCallback onReceiveMessage JSONException" + syncMessage.msgData + "/" + syncMessage.userId + "/" + syncMessage.biz);
                } catch (Exception e) {
                    LoggerFactory.getTraceLogger().debug(TAG, "SyncCallback onReceiveMessage : " + e);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        bindViews();
        setListeners();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MPSync.appToForeground();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MPSync.appToBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MPSync.removeConnectionListener(mConnectionListener);

    }

    public void init() {
        MPSync.initialize(this);
        mTitle.setTitleText(getString(R.string.sync));
        mTvStatus.setText(getString(R.string.sync_unconnected));
        MPSync.addConnectionListener(mConnectionListener);
    }

    public void setListeners() {
        mBtnBind.setOnClickListener(this);
        mBtnUnBind.setOnClickListener(this);
    }

    public void bindViews() {
        mTitle = ((AUTitleBar) findViewById(R.id.title_atb));
        mBtnBind = (AUButton) findViewById(R.id.btn_bind);
        mBtnUnBind = (AUButton) findViewById(R.id.btn_unbind);
        mTvDeviceId = findViewById(R.id.tv_devId);
        mTvStatus = (AUAssistLabelView) findViewById(R.id.tv_status);
        mInputSessionId = (AUInputBox) findViewById(R.id.inputboxSession);
        mInputUid = (AUInputBox) findViewById(R.id.inputboxUid);
        mBindTips = (AUAssistLabelView)findViewById(R.id.tvBindTips);
        mUnBindTips = (AUAssistLabelView)findViewById(R.id.tvUnBindTips);
        mDeviceIdTips = (AUAssistLabelView)findViewById(R.id.tvDeviceIdTips);
    }

    protected int getLayoutId() {
        return R.layout.layout_sync;
    }

    /**
     * 获取服务端下发的同步消息
     * @param data 具体的同步消息
     */
    protected abstract void fetchSyncData(String data);

}
