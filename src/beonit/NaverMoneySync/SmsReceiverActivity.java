package beonit.NaverMoneySync;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

public class SmsReceiverActivity extends Activity {

	QuickWriter writer = null;
	ProgressThread progressThread = null;
	
	private Context getContext(){ return this; }
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		SharedPreferences prefs = this.getSharedPreferences("NaverMoneySync", Context.MODE_PRIVATE);
		String items = prefs.getString("items", "");
		String id = prefs.getString("naverID", null);
		String passwd = null;
		try {
			passwd = SimpleCrypto.decrypt("SECGAL", prefs.getString("naverPasswd", null));
		} catch (Exception e) {
			Log.e("beonit", "simple crypto decrypt fail");
			e.printStackTrace();
		}
		// ����
		Log.i("beonit", "recv to remote service");
	    writer = new QuickWriterNaver(id, passwd, this);
		writer.setFailSave(true);
		writer.setResultNoti(true);
		Log.i("beonit", "ProgressThread" + items);
	    progressThread = new ProgressThread(mHandler, writer, items);
		progressThread.start();
		DialogInterface.OnCancelListener listenerCancel = new DialogInterface.OnCancelListener (){
			@Override
			public void onCancel(DialogInterface dialog){
				// notify
				Log.i("beonit", "user cancel writing to naver");
		    	Notification notification = new Notification(R.drawable.icon, "����� �Է� ���", 0);
		    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
		    	Intent cancelIntent = new Intent();
		    	PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, cancelIntent, 0);
		    	notification.setLatestEventInfo(getContext(), "����� �Է� ���", "����� ���̹� �Է��� ����߽��ϴ�", pendingIntent);
				NotificationManager nm = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		    	nm.notify(ViewMain.NOTI_ID, notification);
				
				// close dialog
				mProgressDialog.dismiss();
				activity.finish();
			}
		};
		mProgressDialog = ProgressDialog.show(this, "����� ����", "3G�� �� ��ٷ� �ּ���\nâ�� ���ַ��� �ڷΰ��� ��ư\n����ص� �Է��� ��� ����˴ϴ�.", false, true , listenerCancel);
	}
	
	@Override
	protected void onDestroy (){
		super.onDestroy();
		if( writer != null )
			writer.stop();
	}
	
	// send
	Activity activity = this;
	public ProgressDialog mProgressDialog;
	private Handler mHandler = new SyncHandler(); 
	public class SyncHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case QuickWriterNaver.WRITE_READY:
				mProgressDialog.setMessage("3G�� �� ��ٷ� �ּ���\n���� ��...");
				break;
			case QuickWriterNaver.WRITE_LOGIN_ATTEMPT:
				mProgressDialog.setMessage("3G�� �� ��ٷ� �ּ���\n�α��� ������ �ε�");
				break;
			case QuickWriterNaver.WRITE_LOGIN_SUCCESS:
				mProgressDialog.setMessage("3G�� �� ��ٷ� �ּ���\n�Է� ������ �ε�");
				break;
			case QuickWriterNaver.WRITE_WRITING:
				mProgressDialog.setMessage("3G�� �� ��ٷ� �ּ���\n����� ���� �Է� ");
				break;
			case QuickWriterNaver.WRITE_SUCCESS:
				mProgressDialog.dismiss(); // ProgressDialog ����
				activity.finish();
				break;
			case QuickWriterNaver.WRITE_LOGIN_FAIL:
				mProgressDialog.dismiss(); // ProgressDialog ����
				activity.finish();
				break;
			case QuickWriterNaver.WRITE_FAIL:
				mProgressDialog.dismiss(); // ProgressDialog ����
				activity.finish();
				break;
			case QuickWriterNaver.WRITE_FAIL_REGISTER:
				mProgressDialog.dismiss(); // ProgressDialog ����
				activity.finish();
				break;
			case QuickWriterNaver.TIME_OUT:
				mProgressDialog.dismiss(); // ProgressDialog ����
				activity.finish();
				break;
			default:
				break;
			}
		}
	};
    
}
