package kr.ac.mju.mjuapp.network;

import kr.ac.mju.mjuapp.R;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * @author kyg
 *
 */
public class NetworkManager {
	/** 
	 * @return
	 */
	public static boolean checkNetwork(Context context) {
		// check network connection
		// 안드로이드 네트워크 연결상태 확인 (Mobile/Wifi)
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 3G(모바일 네트워크) 연결 상태 확인
		boolean isMobile = manager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
		// Wifi 네트워크 연결 상태 확인
		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnectedOrConnecting();
		// WIMAX 네트워크 연결상태
		NetworkInfo wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
		boolean isWIMAX = false;
		if(wimax != null)
			isWIMAX = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX).isConnectedOrConnecting();
		// 네트워크 연결이 비활성화상태일때
		if (!isMobile && !isWifi && !isWIMAX)
			Toast.makeText(context, context.getString(R.string.msg_network_error_not_connection),
					Toast.LENGTH_SHORT).show();
		return isMobile || isWifi || isWIMAX;
	}
}
/* end of file */
