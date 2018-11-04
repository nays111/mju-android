package kr.ac.mju.mjuapp.dialog;

import kr.ac.mju.mjuapp.R;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MJUProgressDialog extends DialogFragment {

	public MJUProgressDialog() {
		setStyle(DialogFragment.STYLE_NO_FRAME,
				android.R.style.Theme_Translucent_NoTitleBar);
		setCancelable(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.loading_progress, container);
		return view;
	}
}
