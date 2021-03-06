package com.idisfkj.loopview.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.idisfkj.loopview.R;
import com.idisfkj.loopview.entity.LoopViewEntity;

import java.util.List;

/**
 * Created by idisfkj on 16/8/8.
 * Email : idisfkj@qq.com.
 */
public class LoopViewAdapter extends PagerAdapter {
    private List<LoopViewEntity> list;
    private Context context;
    private OnItemClickListener listener;
    private final int MAX_SIZE = Integer.MAX_VALUE;
    private int defaultImageView;
    private int errorImageView;

    public LoopViewAdapter(Context context, List<LoopViewEntity> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return MAX_SIZE;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.loopview_item, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.loop_image);
        Glide.with(context).load(list.get(position % list.size()).getImageUrl())
                .centerCrop()
                .placeholder(defaultImageView)
                .error(errorImageView)
                .into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(position % list.size());
            }
        });
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setDefaultImageView(int defaultImageView) {
        this.defaultImageView = defaultImageView;
    }

    public void setErrorImageView(int errorImageView) {
        this.errorImageView = errorImageView;
    }
}
