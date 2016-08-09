package com.idisfkj.loopview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by idisfkj on 16/8/8.
 * Email : idisfkj@qq.com.
 */
public class LoopView extends FrameLayout implements ViewPager.OnPageChangeListener, View.OnTouchListener {
    protected ViewPager viewPager;
    protected LinearLayout linearCircler;
    protected LinearLayout linearCirclerNo;
    protected LinearLayout linearLayout;
    protected TextView descript;
    protected List<LoopViewEntity> list = new ArrayList<>();
    protected LoopViewAdapter adapter;
    protected boolean isChanged;
    protected int mCurrentPos;
    protected ScheduledExecutorService mSes;
    protected OnItemClickListener listener;
    protected int rate;
    protected int bottomStyle;
    protected static final int DEF_RATE = 3;
    protected static final int DEF_BOTTOM_STYLE = 1;
    protected static final int SCROLL_COMPLETELY = 0;
    protected static final int SCROLL_NO_SELECT = 1;
    protected static final int SCROLL_SELECTED = 2;

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
        init(context);
    }

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.loopview_layout, this, true);
        viewPager = (ViewPager) findViewById(R.id.loopView);
        linearCircler = (LinearLayout) findViewById(R.id.linear_circler);
        linearCirclerNo = (LinearLayout) findViewById(R.id.linear_circler_no);
        descript = (TextView) findViewById(R.id.descript);
        viewPager.addOnPageChangeListener(this);
        viewPager.setOnTouchListener(this);
    }

    public void setLoopData(List<LoopViewEntity> loopData) {
        list = loopData;
        for (int i = 0; i < loopData.size(); i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(R.drawable.loop_circler_bg);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 2;
            if (bottomStyle != DEF_BOTTOM_STYLE) {
                descript.setVisibility(GONE);
            }
            if (bottomStyle == getResources().getInteger(R.integer.loop_no_descript_center)) {
                linearCircler.setVisibility(GONE);
                linearCirclerNo.setVisibility(VISIBLE);
                linearLayout = linearCirclerNo;
            } else {
                linearLayout = linearCircler;
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
        descript.setText(list.get(0).getDescript());
        startLoop();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPos = position;
        if (bottomStyle == getResources().getInteger(R.integer.loop_have_descript))
            descript.setText(list.get(position).getDescript());
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            if (i == position)
                linearLayout.getChildAt(i).setSelected(true);
            else
                linearLayout.getChildAt(i).setSelected(false);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        switch (state) {
            case SCROLL_COMPLETELY:
                if (viewPager.getCurrentItem() == list.size() - 1 && !isChanged) {
                    viewPager.setCurrentItem(0);
                }
                if (viewPager.getCurrentItem() == 0 && !isChanged) {
                    viewPager.setCurrentItem(list.size() - 1);
                }
                break;
            case SCROLL_NO_SELECT:
                isChanged = false;
                break;
            case SCROLL_SELECTED:
                isChanged = true;
                break;
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                pauseLoop();
                break;
            case MotionEvent.ACTION_UP:
                if (mSes.isShutdown())
                    startLoop();
                break;
        }
        return false;
    }

    protected class AutoRunnable implements Runnable {

        @Override
        public void run() {
            synchronized (this) {
                mCurrentPos = (mCurrentPos + 1) % list.size();
                mHandler.obtainMessage().sendToTarget();
            }
        }
    }

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            viewPager.setCurrentItem(mCurrentPos);
        }
    };

    protected void startLoop() {
        mSes = Executors.newSingleThreadScheduledExecutor();
        mSes.scheduleAtFixedRate(new AutoRunnable(), rate, rate, TimeUnit.SECONDS);
    }

    protected void pauseLoop() {
        mSes.shutdown();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
