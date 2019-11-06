package retrofit;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtils {
	private Toast mToast = null;
	private static ToastUtils mInstance = null;

	private ToastUtils() {
	}

	public synchronized static ToastUtils getInstance() {
		if(mInstance == null){
			mInstance = new ToastUtils();
		}
		return mInstance;
	}

	public void makeText(Context context, String text) {
		if (!TextUtils.isEmpty(text)) {
			makeText(context, text, Toast.LENGTH_SHORT);
		}
	}

	public void makeText(Context context, String text, int duration) {
		if (mToast == null) {
			mToast = Toast.makeText(context, text, duration);
		}
		mToast.setText(text);
		mToast.setGravity(Gravity.CENTER, 0, 0);
		mToast.setDuration(duration);
		mToast.show();
	}
}
