package kr.ac.mju.mjuapp.community;

public class CommunityOptionValueManager {
	public static String getOption2Value(String optionValue1,
			int subSpinnerValue, int whichArticle) {
		String optionValue2 = "";

		if (optionValue1.equals("")) {
			optionValue2 = "";
		}

		if (whichArticle == 4) { // 동아리
			if (optionValue1.equals("0002")) {
				optionValue2 = "0002";
			} else if (optionValue1.equals("0003")) {
				switch (subSpinnerValue) {
				case 0:
					optionValue2 = "0003";
					break;
				case 1:
					optionValue2 = "00030001";
					break;
				case 2:
					optionValue2 = "00030002";
					break;
				}
			} else if (optionValue1.equals("0004")) {
				switch (subSpinnerValue) {
				case 0:
					optionValue2 = "0004";
					break;
				case 1:
					optionValue2 = "00040001";
					break;
				case 2:
					optionValue2 = "00040002";
					break;
				case 3:
					optionValue2 = "00040003";
					break;
				}
			} else if (optionValue1.equals("0005")) {
				switch (subSpinnerValue) {
				case 0:
					optionValue2 = "0005";
					break;
				case 1:
					optionValue2 = "00050001";
					break;
				case 2:
					optionValue2 = "00050002";
					break;
				case 3:
					optionValue2 = "00050003";
					break;
				}
			} else if (optionValue1.equals("0006")) {
				switch (subSpinnerValue) {
				case 0:
					optionValue2 = "0006";
					break;
				case 1:
					optionValue2 = "00060001";
					break;
				case 2:
					optionValue2 = "00060002";
					break;
				}
			} else if (optionValue1.equals("0007")) {
				optionValue2 = "0007";
			} else if (optionValue1.equals("0008")) {
				switch (subSpinnerValue) {
				case 0:
					optionValue2 = "0008";
					break;
				case 1:
					optionValue2 = "00080001";
					break;
				case 2:
					optionValue2 = "00080001";
					break;
				case 3:
					optionValue2 = "00080003";
					break;
				case 4:
					optionValue2 = "00080004";
					break;
				}
			} else if (optionValue1.equals("0009")) {
				switch (subSpinnerValue) {
				case 0:
					optionValue2 = "0009";
					break;
				case 1:
					optionValue2 = "00090001";
					break;
				case 2:
					optionValue2 = "00090002";
					break;
				}
			} else if (optionValue1.equals("0010")) {
				switch (subSpinnerValue) {
				case 0:
					optionValue2 = "0010";
					break;
				case 1:
					optionValue2 = "00100001";
					break;
				}
			}
		} else if (whichArticle == 6) { // 명지식인
			optionValue2 = optionValue1;
		}
		return optionValue2;
	}

	public static String getMcategory(int whichArticle, int subSpinnerValue) {
		String mcategoryId = "";

		if (whichArticle == 6) {
			switch (subSpinnerValue) {
			case 0:
				mcategoryId = "";
				break;
			case 1:
				mcategoryId = "1965";
				break;
			case 2:
				mcategoryId = "1966";
				break;
			case 3:
				mcategoryId = "1967";
				break;
			case 4:
				mcategoryId = "10588";
				break;
			case 5:
				mcategoryId = "10589";
				break;
			case 6:
				mcategoryId = "10590";
				break;
			}
		} else if (whichArticle == 9) {
			switch (subSpinnerValue) {
			case 0:
				mcategoryId = "";
				break;
			case 1:
				mcategoryId = "10624";
				break;
			case 2:
				mcategoryId = "10626";
				break;
			case 3:
				mcategoryId = "10627";
				break;
			case 4:
				mcategoryId = "10628";
				break;
			case 5:
				mcategoryId = "10629";
				break;
			}
		} else if (whichArticle == 12) {
			switch (subSpinnerValue) {
			case 0:
				mcategoryId = "";
				break;
			case 1:
				mcategoryId = "10695";
				break;
			case 2:
				mcategoryId = "10696";
				break;
			case 3:
				mcategoryId = "10701";
				break;
			case 4:
				mcategoryId = "10703";
				break;
			case 5:
				mcategoryId = "10704";
				break;
			}
		} else if (whichArticle == 13) {
			switch (subSpinnerValue) {
			case 0:
				mcategoryId = "";
				break;
			case 1:
				mcategoryId = "10719";
				break;
			case 2:
				mcategoryId = "10720";
				break;
			}
		} else if (whichArticle == 14) {
			switch (subSpinnerValue) {
			case 0:
				mcategoryId = "";
				break;
			case 1:
				mcategoryId = "6158634";
				break;
			case 2:
				mcategoryId = "6158644";
				break;
			case 3:
				mcategoryId = "6158677";
				break;
			case 4:
				mcategoryId = "6158682";
				break;
			case 5:
				mcategoryId = "6158695";
				break;
			case 6:
				mcategoryId = "6158708";
				break;
			case 7:
				mcategoryId = "28488";
				break;
			case 8:
				mcategoryId = "28489";
				break;
			}
		}

		return mcategoryId;
	}
}
