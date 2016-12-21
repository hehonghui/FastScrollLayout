package com.simple.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.nolanlawson.supersaiyan.R;


/**
 * Fast Scroll View that allows for arbitrary sizing of the overlay.  Based on
 * http://nolanlawson.com/2012/03/19/spruce-up-your-listview-by-dividing-it-into-sections/
 * <p>
 * The origin library is https://github.com/nolanlawson/SuperSaiyanScrollView .
 *
 * @author Mr.Simple
 */
public class FastScrollLayout extends RelativeLayout implements OnScrollListener, OnHierarchyChangeListener {
    // how much transparency to use for the fast scroll thumb
    private static final int ALPHA_MAX = 255;

    // how long before the fast scroll thumb disappears
    private static final long FADE_DURATION = 200;

    private static final int THUMB_DRAWABLE = R.drawable.fastscroll_thumb_holo;
    private static final int THUMB_DRAWABLE_PRESSED = R.drawable.scroll_thumb_pressed;

    private static final int[] STATE_PRESSED = new int[]{android.R.attr.state_pressed};
    private static final int[] STATE_UNPRESSED = new int[]{};

    private Drawable mCurrentThumb;

    private int mThumbH;
    private int mThumbW;
    private int mThumbY;

    private boolean mDragging;
    private ListView mList;
    private boolean mScrollCompleted;
    private boolean mThumbVisible;
    private int mVisibleItem;
    private ScrollFade mScrollFade;

    private Handler mHandler = new Handler();


    private boolean mChangedBounds;

    private boolean shouldRedrawThumb;


    public FastScrollLayout(Context context) {
        this(context, null);
    }


    public FastScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastScrollLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    private void init(Context context) {
        setupScrollThumb(context);

        mScrollCompleted = true;
        setWillNotDraw(false);

        // Need to know when the ListView is added
        setOnHierarchyChangeListener(this);
        mScrollFade = new ScrollFade();
    }


    private void setupScrollThumb(Context context) {
        // Get both the scrollbar states drawables
        final Resources res = context.getResources();
        StateListDrawable thumbDrawable = new StateListDrawable();
        //This for pressed true
        thumbDrawable.addState(STATE_PRESSED, res.getDrawable(THUMB_DRAWABLE_PRESSED));
        //This for pressed false
        thumbDrawable.addState(STATE_UNPRESSED, res.getDrawable(THUMB_DRAWABLE));
        useThumbDrawable(thumbDrawable);
    }

    private void useThumbDrawable(Drawable drawable) {
        mCurrentThumb = drawable;
        mThumbW = mCurrentThumb.getIntrinsicWidth();
        mThumbH = mCurrentThumb.getIntrinsicHeight();
        mChangedBounds = true;
    }

    private void removeThumb() {
        mThumbVisible = false;
        // Draw one last time to remove thumb
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (!mThumbVisible) {
            // No need to draw the rest
            return;
        }

        final int y = mThumbY;
        final int viewWidth = getWidth();
        final FastScrollLayout.ScrollFade scrollFade = mScrollFade;

        int alpha = -1;
        if (scrollFade.mStarted) {
            alpha = scrollFade.getAlpha();
            if (alpha < ALPHA_MAX / 2) {
                mCurrentThumb.setAlpha(alpha * 2);
            }
            int left = viewWidth - (mThumbW * alpha) / ALPHA_MAX;
            mCurrentThumb.setBounds(left, 0, viewWidth, mThumbH);
            mChangedBounds = true;
        }

        canvas.translate(0, y);
        mCurrentThumb.draw(canvas);
        canvas.translate(0, -y);

        if (alpha == 0) {
            mCurrentThumb.setState(STATE_UNPRESSED);
            scrollFade.mStarted = false;
            removeThumb();
        } else {
            mCurrentThumb.setState(STATE_UNPRESSED);
            invalidate(viewWidth - mThumbW, y, viewWidth, y + mThumbH);
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mCurrentThumb != null) {
            mCurrentThumb.setBounds(w - mThumbW, 0, w, mThumbH);
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            // ensure that the thumb gets redrawn, even if the user is only fling/touch scrolling
            shouldRedrawThumb = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (shouldRedrawThumb) {
            super.invalidate(); // force a redraw of the thumb
            shouldRedrawThumb = false;
        }

        if (totalItemCount - visibleItemCount > 0 && !mDragging) {
            mThumbY = ((getHeight() - mThumbH) * firstVisibleItem) / (totalItemCount - visibleItemCount);
            if (mChangedBounds) {
                final int viewWidth = getWidth();
                mCurrentThumb.setBounds(viewWidth - mThumbW, 0, viewWidth, mThumbH);
                mChangedBounds = false;
            }
        }
        mScrollCompleted = true;
        if (firstVisibleItem == mVisibleItem) {
            return;
        }
        mVisibleItem = firstVisibleItem;
        if (!mThumbVisible || mScrollFade.mStarted) {
            mThumbVisible = true;
            mCurrentThumb.setAlpha(ALPHA_MAX);
        }
        mHandler.removeCallbacks(mScrollFade);
        mScrollFade.mStarted = false;
        if (!mDragging) {
            mHandler.postDelayed(mScrollFade, 1500);
        }
    }


    @Override
    public void onChildViewAdded(View parent, View child) {
        if (child instanceof ListView) {
            mList = (ListView) child;
            // ListView Scroll Listener
            mList.setOnScrollListener(this);
        }
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
        if (child == mList) {
            mList = null;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mThumbVisible && ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (ev.getX() > getWidth() - mThumbW && ev.getY() >= mThumbY &&
                    ev.getY() <= mThumbY + mThumbH) {
                mDragging = true;
                return true;
            }
        }
        return false;
    }


    private void cancelFling() {
        // Cancel the list fling
        MotionEvent cancelFling = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mList.onTouchEvent(cancelFling);
        cancelFling.recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            if (me.getX() > getWidth() - mThumbW && me.getY() >= mThumbY && me.getY() <= mThumbY + mThumbH) {
                mDragging = true;
                // start drag
                mHandler.removeCallbacks(mScrollFade);
                cancelFling();
                return true;
            }
        } else if (me.getAction() == MotionEvent.ACTION_UP) {
            if (mDragging) {
                mDragging = false;
                mHandler.removeCallbacks(mScrollFade);
                mHandler.postDelayed(mScrollFade, 1000);
                return true;
            }
        } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
            if (mDragging) {
                final int viewHeight = getHeight();
                mThumbY = (int) me.getY() - mThumbH + 10;
                if (mThumbY < 0) {
                    mThumbY = 0;
                } else if (mThumbY + mThumbH > viewHeight) {
                    mThumbY = viewHeight - mThumbH;
                }
                // If the previous scrollTo is still pending
                if (mScrollCompleted) {
                    scrollTo((float) mThumbY / (viewHeight - mThumbH));
                }
                return true;
            }
        }
        return super.onTouchEvent(me);
    }


    private int getListOffset() {
        Adapter adapter = mList.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
             return ((HeaderViewListAdapter)adapter).getHeadersCount();
        }
        return 0;
    }

    private void scrollTo(float position) {
        int count = mList.getCount();
        int index = (int) (position * count);
        mList.setSelectionFromTop(index + getListOffset(), 0);
    }

    /**
     * dismiss scrollbar
     */
    public class ScrollFade implements Runnable {

        long mStartTime;
        long mFadeDuration;
        boolean mStarted;

        void startFade() {
            mFadeDuration = FADE_DURATION;
            mStartTime = SystemClock.uptimeMillis();
            mStarted = true;
        }

        int getAlpha() {
            if (!mStarted) {
                return ALPHA_MAX;
            }
            int alpha;
            long now = SystemClock.uptimeMillis();
            if (now > mStartTime + mFadeDuration) {
                alpha = 0;
            } else {
                alpha = (int) (ALPHA_MAX - ((now - mStartTime) * ALPHA_MAX) / mFadeDuration);
            }
            return alpha;
        }

        public void run() {
            if (!mStarted) {
                startFade();
                invalidate();
            }

            if (getAlpha() > 0) {
                final int y = mThumbY;
                final int viewWidth = getWidth();
                invalidate(viewWidth - mThumbW, y, viewWidth, y + mThumbH);
            } else {
                mStarted = false;
                removeThumb();
            }
        }
    }
}