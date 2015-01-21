package com.example.message_streamer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class OptionsActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);
		EditText edip=(EditText) findViewById(R.id.editTextIP);
		edip.setImeActionLabel(getString(R.string.connect),
				KeyEvent.KEYCODE_ENTER);
		edip.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE
						|| actionId==KeyEvent.KEYCODE_ENTER
						|| ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) &&
							(event.getAction() == KeyEvent.ACTION_DOWN ))){
					Log.d("MS-OA","FOO!");
					Intent i = new Intent(MainActivity.INTENT_ACTION_CONNECT);
					i.putExtra("ip", v.getText().toString());
					i.putExtra("token", "1234");
					sendBroadcast(i);
					return true;
				}
				return false;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
