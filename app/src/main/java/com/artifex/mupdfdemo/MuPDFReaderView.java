package com.artifex.mupdfdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

public class MuPDFReaderView extends AKReaderView {
	public enum Mode {Viewing, Selecting, Drawing}
	private final Context mContext;
	private boolean mLinksEnabled = false;
	private Mode mMode = Mode.Viewing;
	private boolean tapDisabled = false;
	private int tapPageMargin;
	int width=120;

	protected void onTapMainDocArea() {}
	protected void onDocMotion() {}
	protected void onHit(Hit item) {};

	public void setLinksEnabled(boolean b) {
		mLinksEnabled = b;
		resetupChildren();
	}

	public void setMode(Mode m) {
		mMode = m;
	}

    private void setup()
    {
        // Get the screen size etc to customise tap margins.
        // We calculate the size of 1 inch of the screen for tapping.
        // On some devices the dpi values returned are wrong, so we
        // sanity check it: we first restrict it so that we are never
        // less than 100 pixels (the smallest Android device screen
        // dimension I've seen is 480 pixels or so). Then we check
        // to ensure we are never more than 1/5 of the screen width.
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        tapPageMargin = (int)dm.xdpi;
        if (tapPageMargin < 100)
            tapPageMargin = 100;
        if (tapPageMargin > dm.widthPixels/5)
            tapPageMargin = dm.widthPixels/5;
        width=dm.widthPixels/5;
		//Log.d(VIEW_LOG_TAG, "height:"+height+" y:"+e.getY()+" mMargin:"+mMargin);
    }

    public MuPDFReaderView(Context context) {
        super(context);
        mContext = context;
        setup();
    }

	public MuPDFReaderView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
		setup();
	}

	public void setTapPageMargin(int tapPageMargin) {
		this.tapPageMargin = tapPageMargin;
	}

	public boolean onSingleTapUp(MotionEvent e) {
		LinkInfo link = null;

		if (mMode == Mode.Viewing && !tapDisabled) {
			MuPDFView pageView = (MuPDFView) getDisplayedView();
			Hit item = pageView.passClickEvent(e.getX(), e.getY());
			onHit(item);
			if (item == Hit.Nothing) {
				/*else if (e.getX() < tapPageMargin) {
					super.smartMoveBackwards();
				} else if (e.getX() > super.getWidth() - tapPageMargin) {
					super.smartMoveForwards();
				}*/  if (e.getX() < tapPageMargin) {
					super.smartMoveBackwards();
				} else if (e.getX() > super.getWidth() - tapPageMargin) {
					super.smartMoveForwards();
				} else {
					onTapMainDocArea();
				}
			}
		}
		return super.onSingleTapUp(e);
	}

	@Override
	public boolean onDown(MotionEvent e) {

		return super.onDown(e);
	}





	public boolean onScaleBegin(ScaleGestureDetector d) {
		// Disabled showing the pdf_home_screen until next touch.
		// Not sure why this is needed, but without it
		// pinch zoom can make the pdf_home_screen appear
		tapDisabled = true;
		return super.onScaleBegin(d);
	}

	public boolean onTouchEvent(MotionEvent event) {

		if ( mMode == Mode.Drawing )
		{
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					touch_start(x, y);
					break;
				case MotionEvent.ACTION_MOVE:
					touch_move(x, y);
					break;
				case MotionEvent.ACTION_UP:
					touch_up();
					break;
			}
		}

		if ((event.getAction() & event.getActionMasked()) == MotionEvent.ACTION_DOWN)
		{
			tapDisabled = false;
		}

		return super.onTouchEvent(event);
	}

	private float mX, mY;

	private static final float TOUCH_TOLERANCE = 1.4f;

	private void touch_start(float x, float y) {

		MuPDFView pageView = (MuPDFView)getDisplayedView();
		if (pageView != null)
		{
			pageView.startDraw(x, y);
		}
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {

		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
		{
			MuPDFView pageView = (MuPDFView)getDisplayedView();
			if (pageView != null)
			{
				pageView.continueDraw(x, y);
			}
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {
	}

	protected void onChildSetup(int i, View v) {
		if (SearchTaskResult.get() != null
				&& SearchTaskResult.get().pageNumber == i)
			((MuPDFView) v).setSearchBoxes(SearchTaskResult.get().searchBoxes);
		else
			((MuPDFView) v).setSearchBoxes(null);

		((MuPDFView) v).setLinkHighlighting(mLinksEnabled);

		((MuPDFView) v).setChangeReporter(new Runnable() {
			public void run() {
				applyToChildren(new AKReaderView.ViewMapper() {
					@Override
					void applyToView(View view) {
						((MuPDFView) view).update();
					}
				});
			}
		});
	}

	protected void onMoveToChild(int i) {
		if (SearchTaskResult.get() != null
				&& SearchTaskResult.get().pageNumber != i) {
			SearchTaskResult.set(null);
			resetupChildren();
		}
	}

	@Override
	protected void onMoveOffChild(int i) {
		View v = getView(i);
		if (v != null)
			((MuPDFView)v).deselectAnnotation();
	}

	protected void onSettle(View v) {
		// When the layout has settled ask the page to render
		// in HQ
		((MuPDFView) v).updateHq(false);
	}

	protected void onUnsettle(View v) {
		// When something changes making the previous settled view
		// no longer appropriate, tell the page to remove HQ
		((MuPDFView) v).removeHq();
	}

	@Override
	protected void onNotInUse(View v) {
		((MuPDFView) v).releaseResources();
	}

	@Override
	protected void onScaleChild(View v, Float scale) {
		((MuPDFView) v).setScale(scale);
	}
}
