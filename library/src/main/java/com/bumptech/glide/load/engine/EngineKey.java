package com.bumptech.glide.load.engine;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.util.Preconditions;
import java.security.MessageDigest;
import java.util.Map;

/**
 * An in memory only cache key used to multiplex loads.
 */
class EngineKey implements Key {
  private final Object model;
  private final int width;
  private final int height;
  private final Class<?> resourceClass;
  private final Class<?> transcodeClass;
  private final Key signature;
  private final Map<Class<?>, Transformation<?>> transformations;
  private final Options options;
  private int hashCode;

  EngineKey(
      Object model,
      Key signature,
      int width,
      int height,
      Map<Class<?>, Transformation<?>> transformations,
      Class<?> resourceClass,
      Class<?> transcodeClass,
      Options options) {
    this.model = Preconditions.checkNotNull(model);
    this.signature = Preconditions.checkNotNull(signature, "Signature must not be null");
    this.width = width;
    this.height = height;
    this.transformations = Preconditions.checkNotNull(transformations);
    this.resourceClass =
        Preconditions.checkNotNull(resourceClass, "Resource class must not be null");
    this.transcodeClass =
        Preconditions.checkNotNull(transcodeClass, "Transcode class must not be null");
    this.options = Preconditions.checkNotNull(options);
  }

  /**
   * 整个处理流程是：
   * 1、判断两个对象的 hashcode 是否相等，若不等，则认为两个对象不等，完毕，若相等，则比较 equals。
   * 2、若两个对象的 equals 不等，则可以认为两个对象不等，否则认为他们相等
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof EngineKey) {
      EngineKey other = (EngineKey) o;
      return model.equals(other.model)
          && signature.equals(other.signature)
          && height == other.height
          && width == other.width
          && transformations.equals(other.transformations)
          && resourceClass.equals(other.resourceClass)
          && transcodeClass.equals(other.transcodeClass)
          && options.equals(other.options);
    }
    return false;
  }

  /**
   * 原因一：更少的乘积结果冲突
   * 31是质子数中一个“不大不小”的存在，如果你使用的是一个如2的较小质数，那么得出的乘积会在一个很小的范围，
   * 很容易造成哈希值的冲突。而如果选择一个100以上的质数，得出的哈希值会超出int的最大范围，这两种都不合适。
   * 而如果对超过 50,000 个英文单词（由两个不同版本的 Unix 字典合并而成）进行 hash code 运算，
   * 并使用常数 31, 33, 37, 39 和 41 作为乘子，每个常数算出的哈希值冲突数都小于7个（国外大神做的测试），
   * 那么这几个数就被作为生成hashCode值得备选乘数了。
   *
   * 原因二：31可以被JVM优化
   * JVM里最有效的计算方式就是进行位运算了：
   * 左移 << : 左边的最高位丢弃，右边补全0（把 << 左边的数据*2的移动次幂）。
   * 右移 >> : 把>>左边的数据/2的移动次幂。
   * 无符号右移 >>> : 无论最高位是0还是1，左边补齐0。 　　
   * 所以 ： 31 * i = (i << 5) - i（左边  31*2=62,右边   2*2^5-2=62） - 两边相等，JVM就可以高效的进行计算啦
   */
  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = model.hashCode();
      hashCode = 31 * hashCode + signature.hashCode();
      hashCode = 31 * hashCode + width;
      hashCode = 31 * hashCode + height;
      hashCode = 31 * hashCode + transformations.hashCode();
      hashCode = 31 * hashCode + resourceClass.hashCode();
      hashCode = 31 * hashCode + transcodeClass.hashCode();
      hashCode = 31 * hashCode + options.hashCode();
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return "EngineKey{"
        + "model=" + model
        + ", width=" + width
        + ", height=" + height
        + ", resourceClass=" + resourceClass
        + ", transcodeClass=" + transcodeClass
        + ", signature=" + signature
        + ", hashCode=" + hashCode
        + ", transformations=" + transformations
        + ", options=" + options
        + '}';
  }

  @Override
  public void updateDiskCacheKey(MessageDigest messageDigest) {
    throw new UnsupportedOperationException();
  }
}
