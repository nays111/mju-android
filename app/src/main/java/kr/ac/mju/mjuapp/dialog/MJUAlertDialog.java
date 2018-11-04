package kr.ac.mju.mjuapp.dialog;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.community.CommunityImageWriteActivity;
import kr.ac.mju.mjuapp.community.CommunityListActivity;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.green.GreencampusWriteActivity;
import kr.ac.mju.mjuapp.main.MainActivity;
import kr.ac.mju.mjuapp.photosns.PhotoSNSWriteActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

public class MJUAlertDialog extends DialogFragment {

	public static MJUAlertDialog newInstance(int flag, int title, int msg,
			int strArray) {
		MJUAlertDialog aDailog = new MJUAlertDialog();
		Bundle args = new Bundle();
		args.putInt("flag", flag);
		args.putInt("title", title);
		args.putInt("msg", msg);
		args.putInt("strArr", strArray);
		aDailog.setArguments(args);
		return aDailog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int title = getArguments().getInt("title");
		int msg = getArguments().getInt("msg");
		int flag = getArguments().getInt("flag");
		int strArr = getArguments().getInt("strArr");

		View checkBoxDialogView = null;
		TextView tv = null;

		switch (flag) {
		case MJUConstants.NORMAL_ALERT_DIALOG:
			return new AlertDialog.Builder(getActivity()).setTitle(title)
					.setCancelable(true).setMessage(msg).create();
		case MJUConstants.PHOTO_PICTURE_SELECT_DIALOG:
			return new AlertDialog.Builder(getActivity())
					.setTitle(title)
					.setCancelable(false)
					.setSingleChoiceItems(strArr, -1,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									PhotoSNSWriteActivity activity = (PhotoSNSWriteActivity) getActivity();
									switch (which) {
									case 0:
										activity.pickUpPicture();
										break;
									case 1:
										activity.takePicture();
										break;
									}
									getDialog().dismiss();
								}
							}).create();
		case MJUConstants.GREEN_PICTURE_SELECT_DIALOG:
			return new AlertDialog.Builder(getActivity())
					.setTitle(title)
					.setCancelable(false)
					.setSingleChoiceItems(strArr, -1,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									GreencampusWriteActivity activity = (GreencampusWriteActivity) getActivity();
									switch (which) {
									case 0:
										activity.pickUpPicture();
										break;
									case 1:
										activity.takePicture();
										break;
									}
									getDialog().dismiss();
								}
							}).create();
		case MJUConstants.COMMUNITY_IMG_PICTURE_SELECT_DIALOG:
			return new AlertDialog.Builder(getActivity())
					.setTitle(title)
					.setCancelable(false)
					.setSingleChoiceItems(strArr, -1,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									CommunityImageWriteActivity activity = (CommunityImageWriteActivity) getActivity();
									switch (which) {
									case 0:
										activity.pickUpPicture();
										break;
									case 1:
										activity.takePicture();
										break;
									}
									getDialog().dismiss();
								}
							}).create();
		case MJUConstants.SELECT_CAMPUS:
			return new AlertDialog.Builder(getActivity()).setTitle(title)
					.setItems(strArr, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							CommunityListActivity activity = (CommunityListActivity) getActivity();
							switch (which) {
							case 0:
								activity.showHumanityCampusSTDAssociation();
								break;
							case 1:
								activity.showScienceCampusSTDAssociation();
								break;
							}
							getDialog().dismiss();
						}
					}).create();
		case MJUConstants.APP_NOTICE_DIALOG:
			checkBoxDialogView = View.inflate(getActivity(),
					R.layout.app_notice_checkbox_layout, null);
			final CheckBox checkbox1 = (CheckBox) checkBoxDialogView
					.findViewById(R.id.app_notice_dialog_checkbox);
			tv = (TextView) checkBoxDialogView
					.findViewById(R.id.app_notice_dialog_checkbox_tv);
			tv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (checkbox1.isChecked()) {
						checkbox1.setChecked(false);
					} else {
						checkbox1.setChecked(true);
					}
				}
			});

			return new AlertDialog.Builder(getActivity())
					.setTitle(title)
					.setMessage(msg)
					.setView(checkBoxDialogView)
					.setPositiveButton(R.string.check,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									MainActivity activity = (MainActivity) getActivity();
									activity.saveNoticeCheckBoxState(checkbox1
											.isChecked());
								}
							}).create();
			// case MJUConstants.PHOTO_NOTICE_DIALOG:
			// checkBoxDialogView = View.inflate(getActivity(),
			// R.layout.app_notice_checkbox_layout,
			// null);
			// final CheckBox checkbox2 =
			// (CheckBox)checkBoxDialogView.findViewById(R.id.
			// app_notice_dialog_checkbox);
			// tv =
			// (TextView)checkBoxDialogView.findViewById(R.id.app_notice_dialog_checkbox_tv);
			// tv.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// // TODO Auto-generated method stub
			// if (checkbox2.isChecked()) {
			// checkbox2.setChecked(false);
			// } else {
			// checkbox2.setChecked(true);
			// }
			// }
			// });
			//
			// return new AlertDialog.Builder(getActivity())
			// .setMessage(msg)
			// .setView(checkBoxDialogView)
			// .setPositiveButton(R.string.check, new
			// DialogInterface.OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// // TODO Auto-generated method stub
			// PhotoActivity activity = (PhotoActivity)getActivity();
			// activity.saveNoticeCheckBoxState(checkbox2.isChecked());
			// }
			// })
			// .create();
		default:
			return null;
		}
	}
}