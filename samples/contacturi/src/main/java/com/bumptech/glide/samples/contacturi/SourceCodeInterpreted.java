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
    Glide.with(this)
        .load(new String())
        .into(new ImageView(this));
  }
}
