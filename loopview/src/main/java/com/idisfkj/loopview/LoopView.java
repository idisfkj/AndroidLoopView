package com.idisfkj.loopview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.idisfkj.loopview.adapter.LoopViewAdapter;
import com.idisfkj.loopview.entity.LoopViewEntity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author idisfkj
 * @date 16/8/8
 * Email : idisfkj@qq.com.
 */
public class LoopView extends FrameLayout implements View.OnTouchListener {
    protected ViewPager viewPager;
    protected LinearLayout linearCircle;
    protected TextView description;
    protected RelativeLayout bottomLayout;
    protected List<LoopViewEntity> list = new ArrayList<>();
    protected LoopViewAdapter adapter;
    protected int mCurrentPos;
    protected OnItemClickListener listener;
    protected static final int DEF_RATE = 3;
    protected static final int DEF_BOTTOM_STYLE = 1;
    protected int defaultImageView;
    protected int errorImageView;

    private int mBottomStyle;
    private int mBottomBackground;
    private int mBottomHeight;
    private int mRate;
    private int mSelectedIndicator;
    private int mUnSelectedIndicator;
    private int mIndicatorSpace;
    private int mIndicatorMargin;
    private ScheduledFuture<?> mScheduledFuture;
    private ScheduledExecutorService mSes;
    private InnerHandler mHandler = new InnerHandler(this);

    public LoopView(Context context) {
        this(context, null);
    }

    public LoopView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LoopView);
        mRate = typedArray.getInteger(R.styleable.LoopView_rate, DEF_RATE);
        mBottomStyle = typedArray.getInteger(R.styleable.LoopView_bottom_style, DEF_BOTTOM_STYLE);
        mBottomBackground = typedArray.getColor(R.styleable.LoopView_bottom_background, ContextCompat.getColor(context, R.color.description_color));
        mBottomHeight = typedArray.getDimensionPixelSize(R.styleable.LoopView_bottom_height, getResources().getDimensionPixelSize(R.dimen.bottom_height));
        mSelectedIndicator = typedArray.getResourceId(R.styleable.LoopView_selected_indicator, R.drawable.circler_hover);
        mUnSelectedIndicator = typedArray.getResourceId(R.styleable.LoopView_un_selected_indicator, R.drawable.circler);
        mIndicatorSpace = typedArray.getDimensionPixelSize(R.styleable.LoopView_indicator_space, 2);
        mIndicatorMargin = typedArray.getDimensionPixelSize(R.styleable.LoopView_indicator_margin, 10);
        typedArray.recycle();
        init(context);
    }

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.loopview_layout, this, true);
        viewPager = (ViewPager) findViewById(R.id.loopView);
        linearCircle = (LinearLayout) findViewById(R.id.linear_circle);
        description = (TextView) findViewById(R.id.description);
        bottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        bottomLayout.setBackgroundColor(mBottomBackground);
        bottomLayout.getLayoutParams().height = mBottomHeight;
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPos = position;
                if (mBottomStyle == getResources().getInteger(R.integer.loop_have_description)) {
                    description.setText(list.get(position % list.size()).getDescript());
                }
                for (int i = 0; i < linearCircle.getChildCount(); i++) {
                    if (i == position % list.size()) {
                        linearCircle.getChildAt(i).setSelected(true);
                    } else {
                        linearCircle.getChildAt(i).setSelected(false);
                    }
                }
            }
        });
        viewPager.setOnTouchListener(this);
    }

    public void setDefaultImageView(int defaultImageView) {
        this.defaultImageView = defaultImageView;
    }

    public void setErrorImageView(int errorImageView) {
        this.errorImageView = errorImageView;
    }

    public void setLoopData(List<LoopViewEntity> loopData) {
        list = loopData;
        linearCircle.removeAllViews();
        mCurrentPos = 0;
        shutdown();
        for (int i = 0; i < loopData.size(); i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageDrawable(createDrawableSelector());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = mIndicatorSpace;
            imageView.setLayoutParams(params);
            refreshBottomStyle();
            linearCircle.addView(imageView);
        }
        adapter = new LoopViewAdapter(getContext(), list);
        adapter.setOnItemClickListener(new LoopViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                listener.onItemClick(position);
            }
        });
        viewPager.setAdapter(adapter);
        linearCircle.getChildAt(0).setSelected(true);
        description.setText(list.get(0).getDescript());

        if (defaultImageView != 0) {
            adapter.setDefaultImageView(defaultImageView);
        }
        if (errorImageView != 0) {
            adapter.setErrorImageView(errorImageView);
        }
        viewPager.setCurrentItem(list.size() * (Integer.MAX_VALUE / 1000));
        startLoop();
    }

    private StateListDrawable createDrawableSelector() {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, ContextCompat.getDrawable(getContext(), mSelectedIndicator));
        stateListDrawable.addState(new int[]{-android.R.attr.state_selected}, ContextCompat.getDrawable(getContext(), mUnSelectedIndicator));
        return stateListDrawable;
    }

    private void refreshBottomStyle() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linearCircle.getLayoutParams();
        if (mBottomStyle != DEF_BOTTOM_STYLE) {
            description.setVisibility(GONE);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            if (mBottomStyle == getResources().getInteger(R.integer.loop_no_description_center)) {
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            } else if (mBottomStyle == getResources().getInteger(R.integer.loop_no_description_left)) {
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                params.leftMargin = mIndicatorMargin;
            } else if (mBottomStyle == getResources().getInteger(R.integer.loop_no_description_right)) {
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                params.rightMargin = mIndicatorMargin;
            }
        } else {
            params.rightMargin = mIndicatorMargin;
        }
        linearCircle.setLayoutParams(params);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                pauseLoop();
                break;
            case MotionEvent.ACTION_UP:
                startLoop();
                break;
            default:
                return super.onTouchEvent(motionEvent);
        }
        return false;
    }

    protected void startLoop() {
        mSes = createExecutor();
        mScheduledFuture = mSes.scheduleAtFixedRate(new AutoRunnable(), mRate, mRate, TimeUnit.SECONDS);
    }

    protected void pauseLoop() {
        mScheduledFuture.cancel(true);
    }

    protected class AutoRunnable implements Runnable {

        @Override
        public void run() {
            synchronized (this) {
                mCurrentPos++;
                mHandler.obtainMessage().sendToTarget();
            }
        }
    }


    private static class InnerHandler extends Handler {
        private WeakReference<LoopView> mTarget;

        InnerHandler(LoopView loopView) {
            mTarget = new WeakReference<>(loopView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LoopView instance = mTarget.get();
            if (instance != null) {
                instance.viewPager.setCurrentItem(instance.mCurrentPos);
            }
        }
    }

    private ScheduledExecutorService createExecutor() {
        if (mSes == null || mSes.isShutdown()) {
            mSes = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                int count;

                @Override
                public Thread newThread(@NonNull Runnable r) {
                    final Thread thread = new Thread(r, "loopView-thread-" + count) {
                        @Override
                        public void run() {
                            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                            super.run();
                        }
                    };
                    count++;
                    return thread;
                }
            });
        }
        return mSes;
    }

    private void shutdown() {
        if (mSes != null && !mSes.isShutdown()) {
            pauseLoop();
            mSes.shutdown();
            mSes = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        shutdown();
    }

    public interface OnItemClickListener {
        /**
         * item click event
         *
         * @param position position of loopView
         */
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setBottomStyle(int bottomStyle) {
        this.mBottomStyle = bottomStyle;
    }

    public void setBottomBackground(@ColorInt int bottomBackground) {
        this.mBottomBackground = bottomBackground;
    }

    public void setBottomHeight(int bottomHeight) {
        this.mBottomHeight = bottomHeight;
    }

    public void setRate(int rate) {
        this.mRate = rate;
    }

    public void setSelectedIndicator(@IdRes int selectedIndicator) {
        this.mSelectedIndicator = selectedIndicator;
    }

    public void setUnSelectedIndicator(@IdRes int unSelectedIndicator) {
        this.mUnSelectedIndicator = unSelectedIndicator;
    }

    public void setIndicatorSpace(int indicatorSpace) {
        this.mIndicatorSpace = indicatorSpace;
    }

    public void setIndicatorMargin(int indicatorMargin) {
        this.mIndicatorMargin = indicatorMargin;
    }
}
