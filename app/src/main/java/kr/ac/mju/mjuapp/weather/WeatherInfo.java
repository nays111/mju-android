package kr.ac.mju.mjuapp.weather;

/**
 * @author davidkim
 *
 */
public class WeatherInfo {
	private String text;
	private String temp;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		
		if (temp.contains(".")) {
			this.temp = temp.substring(0, temp.indexOf("."));
		} else {
			this.temp = temp;
		}
	}
}
/* end of file */
