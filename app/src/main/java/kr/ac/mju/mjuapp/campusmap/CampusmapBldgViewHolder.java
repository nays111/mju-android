package kr.ac.mju.mjuapp.campusmap;

import android.widget.TextView;

/**
 * @author davidkim
 * 
 */
public class CampusmapBldgViewHolder {
	private TextView bldgNameView;
	private TextView latitudeView;
	private TextView longitudeView;

	public TextView getBldgNameView() {
		return bldgNameView;
	}

	public void setBldgNameView(TextView bldgNameView) {
		this.bldgNameView = bldgNameView;
	}

	public TextView getLatitudeView() {
		return latitudeView;
	}

	public void setLatitudeView(TextView latitudeView) {
		this.latitudeView = latitudeView;
	}

	public TextView getLongitudeView() {
		return longitudeView;
	}

	public void setLongitudeView(TextView longitudeView) {
		this.longitudeView = longitudeView;
	}

}
/* end of file */
