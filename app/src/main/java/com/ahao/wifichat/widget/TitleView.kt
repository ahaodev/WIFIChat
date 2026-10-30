package com.ahao.wifichat.widget;
import com.ahao.wifichat.R;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TitleView extends LinearLayout {
	private Button mBackBtn;
	private ImageButton mRightBtn;
	private TextView mRightTxt;
	private TextView mTitleText;
	private Context context;
	public TitleView(Context context) {
		super(context);
		initView(context);
	}

	public TitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public TitleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private void initView(Context context) {
		this.context=context;
		View.inflate(context, R.layout.p2p_title, this);
		mBackBtn = (Button) findViewById(R.id.btn_back);
		mBackBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Context context =getContext();
				if (context instanceof Activity) {
					((Activity) context).finish();
				}
			}
		});
		mRightBtn = (ImageButton) findViewById(R.id.rightBtn);
		mTitleText = (TextView) findViewById(R.id.header_text);
		mRightTxt = (TextView) findViewById(R.id.rightTxt);
	}


	public void setBackVisibility(int visibility) {
		mBackBtn.setVisibility(visibility);
	}
	public void setRightClickListener(OnClickListener listener){
		mRightBtn.setOnClickListener(listener);
	}

	public void setBackOnClickListener(OnClickListener listener) {
		mBackBtn.setOnClickListener(listener);
	}

	public void setRightResource(int resId) {
		mRightBtn.setImageResource(resId);
	}

	public void setRightText(int txtId) {
		mRightTxt.setText(txtId);
	}

	public void setRightText(String strText) {
		mRightTxt.setText(strText);
	}

	public void setRightVisibility(int visibility) {
		mRightBtn.setVisibility(visibility);
		mRightTxt.setVisibility(visibility);
	}

	public void setTitle(int resId) {
		mTitleText.setText(resId);
	}

	public void setTitle(String txt) {
		mTitleText.setText(txt);
	}

	public int getRightButtonId() {
		return mRightBtn.getId();
	}


}
