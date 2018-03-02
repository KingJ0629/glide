package com.bumptech.glide.samples.contacturi;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

/**
 * Created by Jin on 2018/2/6.
 * Description 尝试去解读源码
 */
public class SourceCodeInterpreted extends Activity {


  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 常用调用方法
    Glide
        // 绑定生命周期
        .with(this)
        /**
         * 把new String()赋值给RequestBuilder的model参数,并设置isModelSet为true
         * {@link com.bumptech.glide.RequestBuilder#loadGeneric(Object)}
         * 初始化RequestBuilder，把Drawable赋值给transcodeClass，后续主流程会用到这个参数
         */
        .load(new String())
        .into(new ImageView(this));
  }
}
