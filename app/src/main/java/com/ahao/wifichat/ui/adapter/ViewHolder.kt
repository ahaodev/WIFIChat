package com.ahao.wifichat.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahao.wifichat.utlis.GlideLoder;
import com.ahao.wifichat.utlis.ImageLoader;


public class ViewHolder {
	private final SparseArray<View> mViews;
	private int mPosition;
	private View mConvertView;
	private Context context;
	private ImageLoader imageLoader;
	private ViewHolder(Context context, ViewGroup parent, int layoutId,
			int position) {
		this.mPosition = position;
		this.mViews = new SparseArray<View>();
		this.context = context;
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
				false);
		// setTag

		mConvertView.setTag(this);
		imageLoader=new GlideLoder(context);
	}

	/**
	 * 拿到一个ViewHolder对象
	 * 
	 * @param context
	 * @param convertView
	 * @param parent
	 * @param layoutId
	 * @param position
	 * @return
	 */
	public static ViewHolder get(Context context, View convertView,
			ViewGroup parent, int layoutId, int position) {
		ViewHolder holder = null;
		if (null==convertView) {
			holder = new ViewHolder(context, parent, layoutId, position);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.mPosition = position;
		}
		return holder;
	}

	public View getConvertView() {
		return mConvertView;
	}

	/**
	 * 通过控件的Id获取对于的控件，如果没有则加入views
	 * 
	 * @param viewId
	 * @return
	 */
	public <T extends View> T getView(int viewId) {
		View view = mViews.get(viewId);
		if (view == null) {
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}

	public int getPosition() {
		return mPosition;
	}

	public View getConverView() {
		return mConvertView;
	}

	/**
	 * 为TextView设置字符串
	 * 
	 * @param viewId
	 * @param text
	 * @return
	 */
	public ViewHolder setText(int viewId, String text) {
		TextView view = getView(viewId);
		view.setText(text);
		return this;
	}
	/**
	 * 为TextView设置字符串
	 * 
	 * @param viewId
	 * @return
	 */
	public ViewHolder setText(int viewId, int resStrID) {
		TextView view = getView(viewId);
		view.setText(resStrID);
		return this;
	}
	/**
	 * 为TextView设置字符串
	 * 
	 * @param viewId
	 * @return
	 */
	public ViewHolder setText(int viewId, CharSequence text) {
		TextView view = getView(viewId);
		view.setText(text);
		return this;
	}
	public  ViewHolder setCompoundDrawables(int viewId,Drawable left, Drawable top, Drawable right, Drawable bottom){
		TextView view = getView(viewId);
		view.setCompoundDrawables(left, top, right, bottom);  
		return this;
	}

	
	/**
	 * 为ImageView设置图片
	 * 
	 * @param viewId
	 * @param drawableId
	 * @return
	 */
	public ViewHolder setImageResource(int viewId, int drawableId) {
		ImageView view = getView(viewId);
		view.setImageResource(drawableId);

		return this;
	}

	/**
	 * 为ImageView设置图片
	 * 
	 * @param viewId
	 * @return
	 */
	public ViewHolder setImageBitmap(int viewId, Bitmap bm) {
		ImageView view = getView(viewId);
		view.setImageBitmap(bm);
		return this;
	}

	/**
	 * 为ImageView设置图片
	 * 
	 * @param viewId
	 * @return
	 */
	public ViewHolder setImageByUrl(int viewId, String url) {
		ImageView view = getView(viewId);
		imageLoader.loadImage(view,url);
		return this;
	}

	/**
	 * 为ImageView设置图片
	 * 
	 * @param viewId
	 * @return
	 */
	public ViewHolder setImageBySDcred(int viewId, String path) {
		ImageView view = getView(viewId);
		imageLoader.loadImage(view,path);
		return this;
	}

	public String getText(int viewId) {
		TextView view = getView(viewId);
		return view.getText().toString();
	}

	public ViewHolder setApha(int viewId, int apha) {
		View v = getView(viewId);
		v.getBackground().setAlpha(apha);// 0~255閫忔槑搴﹀��
		return this;
	}

	public ViewHolder setTag(int viewId, String text) {
		TextView view = getView(viewId);
		view.setTag(text);
		return this;
	}

	public ViewHolder setTextDrawable(int viewId, int imgL, int imgT, int imgR,
			int imgB) {
		TextView view = getView(viewId);
		Drawable drawableL = null, drawableT = null, drawableR = null, drawableB = null;
		if (imgL != 0) {
			drawableL = context.getResources().getDrawable(imgL);
			drawableL.setBounds(0, 0, drawableL.getMinimumWidth(),
					drawableL.getMinimumHeight());
		}
		if (imgT != 0) {
			drawableT = context.getResources().getDrawable(imgT);
			drawableT.setBounds(0, 0, drawableT.getMinimumWidth(),
					drawableT.getMinimumHeight());
		}
		if (imgR != 0) {
			drawableR = context.getResources().getDrawable(imgR);
			drawableR.setBounds(0, 0, drawableR.getMinimumWidth(),
					drawableR.getMinimumHeight());
		}
		if (imgB != 0) {
			drawableB = context.getResources().getDrawable(imgB);
			drawableB.setBounds(0, 0, drawableB.getMinimumWidth(),
					drawableB.getMinimumHeight());
		}
		view.setCompoundDrawables(drawableL, drawableT, drawableR, drawableB);
		return this;
	}

	public ViewHolder setTextfromHtml(int viewId, Spanned text) {
		TextView view = getView(viewId);
		view.setText(text);
		return this;
	}

	public void setImageDrawable(int activitysStatus, Drawable drawable) {
		ImageView view = getView(activitysStatus);
		view.setImageDrawable(drawable);

	}

	public ViewHolder setBackgroundResource(int viewId, int drawable) {
		View view = getView(viewId);
		view.setBackgroundResource(drawable);
		return this;
	}

	public ViewHolder setBackgroundColor(int viewId, int color) {
		View view = getView(viewId);
		view.setBackgroundColor(color);
		return this;
	}

	public ViewHolder goNone(int viewId) {
		View rl = getView(viewId);
		rl.setVisibility(View.GONE);
		return this;
	}

	public ViewHolder goShow(int viewId) {
		View rl = getView(viewId);
		rl.setVisibility(View.VISIBLE);
		return this;
	}

	public ViewHolder setView(int viewId, LayoutParams params) {
		View fview = getView(viewId);
		fview.setLayoutParams(params);
		return this;

	}

	public ViewHolder setFlags(int viewId) {
		TextView view = getView(viewId);
		view.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		return this;
	}

	public ViewHolder SetWidth(int viewId, int width) {
		View view = getView(viewId);
		WindowManager wManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wManager.getDefaultDisplay().getMetrics(outMetrics);
		int mMaxItemWith = (int) (outMetrics.widthPixels * 0.7f);
		int mMinItemWith = (int) (outMetrics.widthPixels * 0.15f);
		LayoutParams lParams = view.getLayoutParams();
		lParams.width = (int) (mMinItemWith + mMaxItemWith / 60f * width);
		view.setLayoutParams(lParams);
		return this;
	}

	public ViewHolder setHightWidth(int viewId, int width, int higth) {
		View view = getView(viewId);
		view.setLayoutParams(new LayoutParams(width, higth));
		return this;
	}

	public ViewHolder setOnclick(int viewId, OnClickListener listener) {
		View view = getView(viewId);
		view.setOnClickListener(listener);
		return this;
	}

	public ViewHolder setOnCheckedChangeListener(int viewId,
			OnCheckedChangeListener listener) {
		CheckBox view = getView(viewId);
		view.setOnCheckedChangeListener(listener);
		return this;
	}

	public ViewHolder setTextColor(int viewId, int pink) {
		TextView view = getView(viewId);
		view.setTextColor(pink);
		return this;
	}

	public ViewHolder setVisibility(int viewId, int visiable) {
		View view = getView(viewId);
		view.setVisibility(visiable);
		return this;
	}

	public int getVisibility(int viewId) {
		View view = getView(viewId);
		return view.getVisibility();
	}

	public ViewHolder setTextGravity(int viewId, int gravity) {
		TextView view = getView(viewId);
		view.setGravity(gravity);
		return this;
	}

	public <T> ViewHolder setGVColumns(int item_id, int numColumns) {
		GridView gv = getView(item_id);
		gv.setNumColumns(numColumns);
		return this;

	}

}
