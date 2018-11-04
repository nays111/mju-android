package kr.ac.mju.mjuapp.common;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author davidkim
 *
 */
public class CustomTextView extends TextView {
	private int mAvailableWidth = 0;
	private Paint mPaint;
	private List<String> mCutStr = new ArrayList<String>();

	public CustomTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub 
	}

	private int setTextInfo(String text, int textWidth, int textHeight) {
		// 洹몃┫ �섏씤���명똿
		mPaint = getPaint();
		mPaint.setColor(getTextColors().getDefaultColor());
		mPaint.setTextSize(getTextSize());

		int mTextHeight = textHeight;

		if (textWidth > 0) {
			  // 媛��명똿
			  mAvailableWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();

			  mCutStr.clear();
			  int end = 0;
			  String[] textArr = text.split("\n");
			  for(int i=0; i<textArr.length; i++) {
			    if(textArr[i].length() == 0) textArr[i] = " ";
			      do {
			        // 湲�옄媛�width 蹂대떎 �섏뼱媛�뒗吏�泥댄겕
			        end = mPaint.breakText(textArr[i], true, mAvailableWidth, null);
			        if (end > 0) {
			          // �먮Ⅸ 臾몄옄�댁쓣 臾몄옄��諛곗뿴���댁븘 �볥뒗��
			          mCutStr.add(textArr[i].substring(0, end));
			          // �섏뼱媛�湲�옄 紐⑤몢 �섎씪 �ㅼ쓬���ъ슜�섎룄濡��명똿
			          textArr[i] = textArr[i].substring(end);
			          // �ㅼ쓬�쇱씤 �믪씠 吏�젙
			          if (textHeight == 0) mTextHeight += getLineHeight();
			        }
			      } while (end > 0);
			    }
			}
		mTextHeight += getPaddingTop() + getPaddingBottom();
		return mTextHeight;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// 湲�옄 �믪씠 吏�젙
		float height = getPaddingTop() + getLineHeight();
		for (String text : mCutStr) {
			// 罹붾쾭�ㅼ뿉 �쇱씤 �믪씠 留뚰겙 湲�옄 洹몃━湲�			
			canvas.drawText(text, getPaddingLeft(), height, mPaint);
			height += getLineHeight();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		int height = setTextInfo(this.getText().toString(), parentWidth,
				parentHeight);
		// 遺�え �믪씠媛�0�멸꼍���ㅼ젣 洹몃젮以��믪씠留뚰겮 �ъ씠利덈� ��젮以�..
		if (parentHeight == 0)
			parentHeight = height;
		this.setMeasuredDimension(parentWidth, parentHeight);
	}

	@Override
	protected void onTextChanged(final CharSequence text, final int start,
			final int before, final int after) {
		// 湲�옄媛�蹂�꼍�섏뿀�꾨븣 �ㅼ떆 �명똿
		setTextInfo(text.toString(), this.getWidth(), this.getHeight());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// �ъ씠利덇� 蹂�꼍�섏뿀�꾨븣 �ㅼ떆 �명똿(媛�줈 �ъ씠利덈쭔...)
		if (w != oldw) {
			setTextInfo(this.getText().toString(), w, h);
		}
	}
}
/* end of file */
