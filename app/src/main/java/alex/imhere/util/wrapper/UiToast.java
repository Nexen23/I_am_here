package alex.imhere.util.wrapper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class UiToast {
	static public void Show(final Context context, final String text) {
		final Handler uiHandler = new Handler(Looper.getMainLooper());
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
			}
		});
	}

	static public void Show(final Context context, final String text, final String error) {
		Show(context, text + String.format("  [%s]", error));
	}
}
