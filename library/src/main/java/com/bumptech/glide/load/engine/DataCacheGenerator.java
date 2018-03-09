package com.bumptech.glide.load.engine;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoader.LoadData;
import java.io.File;
import java.util.List;

/**
 * Generates {@link com.bumptech.glide.load.data.DataFetcher DataFetchers} from cache files
 * containing original unmodified source data.
 */
class DataCacheGenerator implements DataFetcherGenerator,
    DataFetcher.DataCallback<Object> {

  private final List<Key> cacheKeys;
  private final DecodeHelper<?> helper;
  private final FetcherReadyCallback cb;

  private int sourceIdIndex = -1;
  private Key sourceKey;
  private List<ModelLoader<File, ?>> modelLoaders;
  private int modelLoaderIndex;
  private volatile LoadData<?> loadData;
  // PMD is wrong here, this File must be an instance variable because it may be used across
  // multiple calls to startNext.
  @SuppressWarnings("PMD.SingularField")
  private File cacheFile;

  DataCacheGenerator(DecodeHelper<?> helper, FetcherReadyCallback cb) {
    this(helper.getCacheKeys(), helper, cb);
  }

  // In some cases we may want to load a specific cache key (when loading from source written to
  // cache), so we accept a list of keys rather than just obtain the list from the helper.
  DataCacheGenerator(List<Key> cacheKeys, DecodeHelper<?> helper, FetcherReadyCallback cb) {
    this.cacheKeys = cacheKeys;
    this.helper = helper;
    this.cb = cb;
  }

  /**
   *  缓存策略拉取数据
   */
  @Override
  public boolean startNext() {
    while (modelLoaders == null || !hasNextModelLoader()) {
      sourceIdIndex++;
      if (sourceIdIndex >= cacheKeys.size()) {
        return false;
      }

      Key sourceId = cacheKeys.get(sourceIdIndex);
      Key originalKey = new DataCacheKey(sourceId, helper.getSignature());
      cacheFile = helper.getDiskCache().get(originalKey);
      if (cacheFile != null) {
        this.sourceKey = sourceId;

        // modelLoaders 这个modelLoaders是拉取数据的方法
        modelLoaders = helper.getModelLoaders(cacheFile);
        modelLoaderIndex = 0;
      }
    }

    loadData = null;
    boolean started = false;
    while (!started && hasNextModelLoader()) {
      ModelLoader<File, ?> modelLoader = modelLoaders.get(modelLoaderIndex++);
      loadData =
          modelLoader.buildLoadData(cacheFile, helper.getWidth(), helper.getHeight(),
              helper.getOptions());
      if (loadData != null && helper.hasLoadPath(loadData.fetcher.getDataClass())) {
        started = true;
        // 根据给定的数据类型的DataFetcher尝试获取数据，然后尝试通过一个或多个decodePath进行decode。
        loadData.fetcher.loadData(helper.getPriority(), this);
      }
    }
    return started;
  }

  private boolean hasNextModelLoader() {
    return modelLoaderIndex < modelLoaders.size();
  }

  @Override
  public void cancel() {
    LoadData<?> local = loadData;
    if (local != null) {
      local.fetcher.cancel();
    }
  }

  @Override
  public void onDataReady(Object data) {
    cb.onDataFetcherReady(sourceKey, data, loadData.fetcher, DataSource.DATA_DISK_CACHE, sourceKey);
  }

  @Override
  public void onLoadFailed(Exception e) {
    cb.onDataFetcherFailed(sourceKey, e, loadData.fetcher, DataSource.DATA_DISK_CACHE);
  }
}
