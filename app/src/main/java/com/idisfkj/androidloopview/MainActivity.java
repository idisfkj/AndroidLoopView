package com.idisfkj.androidloopview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.idisfkj.loopview.LoopView;
import com.idisfkj.loopview.entity.LoopViewEntity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public LoopView loopView;
    public LoopView loopViewNoDesR;
    public LoopView loopViewNoDesC;
    public List<LoopViewEntity> list = new ArrayList<>();
    public List<LoopViewEntity> list_no_r = new ArrayList<>();
    public List<LoopViewEntity> list_no_c = new ArrayList<>();
    public String[] urls = new String[]{"http://upload.cankaoxiaoxi.com/2016/0808/1470616024923.jpg"
            , "http://upload.cankaoxiaoxi.com/2016/0808/1470632912173.jpg"
            , "http://upload.cankaoxiaoxi.com/2016/0808/1470638161683.jpg"
            , "http://img01.youxiaoshuo.com/portal/201608/08/005317sz36gs7sv69hhkv1.jpg"};
    public String[] descripts = new String[]{"中国泳协致电澳大利亚泳协 要求霍顿向孙杨道歉"
            , "里约泳池里竟然捞上来一套表情包"
            , "姐弟恋！吴敏霞男友身份曝光 就职于田径协会"
            , "里约奥组委承认中国国旗错误：正在解决"};
    public String[] detailUrls = new String[]{"http://www.cankaoxiaoxi.com/roll10/bd/20160808/1260093.shtml?bdnews"
            , "http://www.cankaoxiaoxi.com/roll10/bd/20160808/1260518.shtml?bdnews"
            , "http://www.cankaoxiaoxi.com/roll10/bd/20160808/1260643.shtml?bdnews"
            , "http://wenhua.cjn.cn/dxxl/it/201608/00069631.html"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loopView = (LoopView) findViewById(R.id.loop_view);
        loopViewNoDesR = (LoopView) findViewById(R.id.loop_view_no_des_r);
        loopViewNoDesC = (LoopView) findViewById(R.id.loop_view_no_des_c);
        loopView.setDefaultImageView(R.mipmap.ic_launcher);
        loopView.setErrorImageView(R.mipmap.ic_launcher);
        for (int i = 0; i < urls.length; i++) {
            LoopViewEntity entity = new LoopViewEntity();
            entity.setImageUrl(urls[i]);
            entity.setDescript(descripts[i]);
            list.add(entity);
        }
        loopView.setLoopData(list);
        loopView.setOnItemClickListener(new LoopView.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
                intent.putExtra("url", detailUrls[position]);
                startActivity(intent);
            }
        });
        for (int i = 0; i < urls.length - 1; i++) {
            LoopViewEntity entity = new LoopViewEntity();
            entity.setImageUrl(urls[i]);
            entity.setDescript(descripts[i]);
            list_no_r.add(entity);
        }
        loopViewNoDesR.setLoopData(list_no_r);
        loopViewNoDesR.setOnItemClickListener(new LoopView.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
                intent.putExtra("url", detailUrls[position]);
                startActivity(intent);
            }
        });
        for (int i = 0; i < urls.length - 2; i++) {
            LoopViewEntity entity = new LoopViewEntity();
            entity.setImageUrl(urls[i]);
            entity.setDescript(descripts[i]);
            list_no_c.add(entity);
        }
        loopViewNoDesC.setLoopData(list_no_c);
        loopViewNoDesC.setOnItemClickListener(new LoopView.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
                intent.putExtra("url", detailUrls[position]);
                startActivity(intent);
            }
        });
    }
}
