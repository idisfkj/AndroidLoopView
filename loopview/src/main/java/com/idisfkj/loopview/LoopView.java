package com.idisfkj.loopview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    protected LinearLayout linearCircleNo;
    protected LinearLayout linearLayout;
    protected TextView description;
    protected List<LoopViewEntity> list = new ArrayList<>();
    protected LoopViewAdapter adapter;
    protected int mCurrentPos;
    private ScheduledExecutorService mSes;
    private ScheduledFuture<?> mScheduledFuture;
    protected OnItemClickListener listener;
    protected int rate;
    protected int bottomStyle;
    protected static final int DEF_RATE = 3;
    protected static final int DEF_BOTTOM_STYLE = 1;
    protected int defaultImageView;
    protected int errorImageView;
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
        rate = typedArray.getInteger(R.styleable.LoopView_rate, DEF_RATE);
        bottomStyle = typedArray.getInteger(R.styleable.LoopView_bottom_style, DEF_BOTTOM_STYLE);
        typedArray.recycle();
        init(context);
    }

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.loopview_layout, this, true);
        viewPager = (ViewPager) findViewById(R.id.loopView);
        linearCircle = (LinearLayout) findViewById(R.id.linear_circle);
        linearCircleNo = (LinearLayout) findViewById(R.id.linear_circle_no);
        description = (TextView) findViewById(R.id.description);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                mCurrentPos = position;
                if (bottomStyle == getResources().getInteger(R.integer.loop_have_description)) {
                    description.setText(list.get(position % list.size()).getDescript());
                }
                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    if (i == position % list.size()) {
                        linearLayout.getChildAt(i).setSelected(true);
                    } else {
                        linearLayout.getChildAt(i).setSelected(false);
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
        linearCircleNo.removeAllViews();
        mCurrentPos = 0;
        shutdown();
        for (int i = 0; i < loopData.size(); i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(R.drawable.loop_circler_bg);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 2;
            if (bottomStyle != DEF_BOTTOM_STYLE) {
                description.setVisibility(GONE);
            }
            if (bottomStyle == getResources().getInteger(R.integer.loop_no_description_center)) {
                linearCircle.setVisibility(GONE);
                linearCircleNo.setVisibility(VISIBLE);
                linearLayout = linearCircleNo;
            } else {
                linearLayout = linearCircle;
            }
            imageView.setLayoutParams(params);
            linearLayout.addView(imageView);
        }
        adapter = new LoopViewAdapter(getContext(), list);
        adapter.setOnItemClickListener(new LoopViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                listener.onItemClick(position);
            }
        });
        viewPager.setAdapter(adapter);
        linearLayout.getChildAt(0).setSelected(true);
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
        mScheduledFuture = mSes.scheduleAtFixedRate(new AutoRunnable(), rate, rate, TimeUnit.SECONDS);
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
}
