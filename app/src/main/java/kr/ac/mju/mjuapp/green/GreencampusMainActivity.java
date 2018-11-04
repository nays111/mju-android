package kr.ac.mju.mjuapp.green;

import kr.ac.mju.mjuapp.*;
import kr.ac.mju.mjuapp.network.*;
import kr.ac.mju.mjuapp.util.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

/**
 * @author davidkim
 * 
 */
public class GreencampusMainActivity extends Activity implements OnClickListener {

	/* �명뀗�몃줈 �섍꺼以��곸닔媛��뺤쓽 */
	private final int WRITE_SELF = 32; // 蹂몄씤議곗튂 寃곌낵 �좉퀬��
	private final int WRITE_SUGGEST = 33; // �쒖븞�⑸땲��
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.greencampus_main_layout);
		// TODO Auto-generated method stub

		((Button) findViewById(R.id.btn_green_write_self)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_green_write_suggest)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.green_list_btn)).setOnClickListener(this);
		initLayout();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (NetworkManager.checkNetwork(GreencampusMainActivity.this)) {
			Intent intent = null;
			switch (v.getId()) {
			case R.id.btn_green_write_self:
				intent = new Intent(GreencampusMainActivity.this, GreencampusWriteActivity.class);
				intent.putExtra("write_which", WRITE_SELF);
				break;
			case R.id.btn_green_write_suggest:
				intent = new Intent(GreencampusMainActivity.this, GreencampusWriteActivity.class);
				intent.putExtra("write_which", WRITE_SUGGEST);
				break;
			case R.id.green_list_btn:
				intent = new Intent(GreencampusMainActivity.this, GreencampusListActivity.class);
			default:
				break;
			}

			if (intent != null) {
				startActivity(intent);
			}
		}
	}
	
	private void initLayout() {
		PixelConverter pixelConveter = new PixelConverter(this);
		LinearLayout.LayoutParams lParams = null;
		RelativeLayout.LayoutParams rParams = null;
		
		//green header layout
		rParams = (RelativeLayout.LayoutParams)findViewById(R.id.green_main_header_icon).
				getLayoutParams();
		rParams.width = pixelConveter.getWidth(30);
		rParams.height = pixelConveter.getHeight(30);
		rParams.setMargins(0, 0, pixelConveter.getWidth(15), 0);
		
		rParams = (RelativeLayout.LayoutParams)findViewById(R.id.green_list_btn).getLayoutParams();
		rParams.width = pixelConveter.getWidth(50);
		rParams.height = pixelConveter.getHeight(50);
		
		// green btn layout
		lParams = (LayoutParams) findViewById(R.id.green_write_bottom_layout).getLayoutParams();
		lParams.topMargin = pixelConveter.getHeight(150); 
		findViewById(R.id.green_write_bottom_layout).setLayoutParams(lParams);
		
		// btn green self
		lParams = (LayoutParams) findViewById(R.id.btn_green_write_self).getLayoutParams();
		lParams.width = pixelConveter.getWidth(198);
		lParams.height = pixelConveter.getHeight(318);
		lParams.rightMargin = pixelConveter.getWidth(10);
		findViewById(R.id.btn_green_write_self).setLayoutParams(lParams);
		// btn green suggest
		lParams = (LayoutParams) findViewById(R.id.btn_green_write_suggest).getLayoutParams();
		lParams.width = pixelConveter.getWidth(198);
		lParams.height = pixelConveter.getHeight(318);
		findViewById(R.id.btn_green_write_suggest).setLayoutParams(lParams);
	}
}
/* end of file */
