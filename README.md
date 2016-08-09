# 效果图

![效果图](https://github.com/idisfkj/AndroidLoopView/raw/master/images/loopView.gif)

# 使用

## 添加依赖

### Maven

```
<dependency>
  <groupId>com.idisfkj.loopview</groupId>
  <artifactId>loopview</artifactId>
  <version>1.0.1</version>
  <type>pom</type>
</dependency>
```

### Gradle

```
compile 'com.idisfkj.loopview:loopview:1.0.1'
```

>*根据自己的需求添加依赖*

## 布局文件中引用
首先在根布局中加上自定义属性的引用

```
xmlns:loop="http://schemas.android.com/apk/res-auto"
```
引用`LoopView`控件

```
<com.idisfkj.loopview.LoopView
            android:id="@+id/loop_view"
            android:layout_width="match_parent"
            android:layout_height="200dp"
            loop:bottom_style="@integer/loop_have_descript"
            loop:rate="3">
</com.idisfkj.loopview.LoopView>
```

`bottom_style`代表`LoopView`底部样式，有三个可选值,默认为`loop_have_descript`

* loop_have_descript 代表有描述的布局
* loop_no_descript_right 代表没有描述且圆点居右的布局
* loop_no_descript_center 代表没有描述且圆点居中的布局

`rate`代表轮播的速度，`单位为s`,默认为3s

## 填充数据
填充数据需要借助`LoopViewEntity`实体类来存储,例如：

```
for (int i = 0; i < urls.length; i++) {
            LoopViewEntity entity = new LoopViewEntity();
            entity.setImageUrl(urls[i]);
            entity.setDescript(descripts[i]);
            list.add(entity);
        }
loopView.setLoopData(list);
```

## item点击监听

```
loopView.setOnItemClickListener(new LoopView.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //to do ...
            }
        });
```
# 详情

![LoopView-循环轮播控件](http://idisfkj.github.io/2016/08/09/LoopView-%E5%BE%AA%E7%8E%AF%E8%BD%AE%E6%92%AD%E6%8E%A7%E4%BB%B6/)

# License

```
Copyright (c) 2016. The Android Open Source Project
Created by idisfkj
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```



