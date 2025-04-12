package dev.jdtech.jellyfin.customer.danmu;

import android.os.Handler;

import androidx.media3.common.Player;

import org.jellyfin.sdk.api.client.Response;
import org.jellyfin.sdk.api.client.exception.InvalidStatusException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import dev.jdtech.jellyfin.customer.CustomerCommonUtils;
import dev.jdtech.jellyfin.data.BuildConfig;
import dev.jdtech.jellyfin.models.PlayerItem;
import dev.jdtech.jellyfin.repository.JellyfinRepository;
import dev.jdtech.jellyfin.DanmuPreferences;
import io.ktor.utils.io.ByteReadChannel;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import timber.log.Timber;

public class SimpleDanmuController extends AbstractDanmuControllerListener {
    private Player player;

    private final Map<String, PlayerItem> playerItemMap;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        // 日志打印
    }

    /**
     * 弹幕view
     */
    private final IDanmakuView danmakuView;
    private final JellyfinRepository repository;
    private DanmakuContext mDanmakuContext;
    private final Handler mHandler;
    private final DanmuPreferences danmuPreferences;

    private BaseDanmakuParser mParser;//解析器对象

    private UUID danmuItemId;
    private float videoSpeed = 1.0f;

    private PlayerItem playerItem;

    private long startSeekPosition;

    public SimpleDanmuController(IDanmakuView danmakuView, DanmuPreferences danmuPreferences, Player player, JellyfinRepository repository) {
        this.danmakuView = danmakuView;
        this.mHandler = new Handler();
        this.danmuPreferences = danmuPreferences;
        this.repository = repository;

        playerItemMap = new HashMap<>();
        initDanmaku();

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
            }
        });
    }

    public void setItems(PlayerItem[] items) {
        playerItemMap.clear();
        for (PlayerItem item : items) {
            playerItemMap.put(item.getItemId().toString(), item);
        }
    }

    @Override
    protected void pause() {
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
    }

    @Override
    protected void resume() {
        if (danmakuView != null && danmakuView.isPrepared() && danmakuView.isPaused()) {
            danmakuView.resume();
        }
    }

    @Override
    protected void setVideoSpeed(float speed) {
        danmakuView.setVideoSpeed(speed);
    }

    @Override
    protected PlayerItem getItemById(String itemId) {
        return playerItemMap.get(itemId);
    }


    public void release() {
        this.danmakuView.release();
    }


    @Override
    protected void changePlayerItem(PlayerItem playerItem) {
        if (playerItem == null || playerItem == this.playerItem) {
            return;
        }
        this.playerItem = playerItem;

        if (danmakuView.isPrepared()) {
            danmakuView.stop();
        }
        onPrepareDanmaku();

//        this.countDownLatch = new CountDownLatch(2);
//        new Thread(() -> {
//            try {
//                countDownLatch.await();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }).start();
    }


    @Override
    protected void seekTo(long newPosition) {
        this.danmakuView.seekTo(newPosition);
    }

    private void initDanmaku() {
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, danmuPreferences.getDanmuRows()); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        mDanmakuContext = DanmakuContext.create();
        mDanmakuContext.setDanmakuStyle(danmuPreferences.getDanmuStyle(), 3)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(danmuPreferences.getDanmuSpeed())
                .setScaleTextSize(danmuPreferences.getDanmuFontSize())
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair);

        DanmakuTimer.debug = BuildConfig.DEBUG;
        IDanmakuView mDanmakuView = danmakuView;
        if (mDanmakuView != null) {
            mDanmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {
                }

                @Override
                public void drawingFinished() {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
                }

                @Override
                public void prepared() {
                    IDanmakuView danmakuView = getDanmakuView();
                    if (danmakuView != null) {
                        long danmakuStartSeekPosition = startSeekPosition;
                        danmakuView.start();
                        if (danmakuStartSeekPosition > 0) {
                            danmakuView.seekTo(danmakuStartSeekPosition);
                        }

                        if (videoSpeed != 1.0f) {
                            danmakuView.setVideoSpeed(videoSpeed);
                        }
                        resolveDanmakuShow();
                    }
                }
            });
            mDanmakuView.enableDanmakuDrawingCache(true);
            DanamakuAdapter danamakuAdapter = new DanamakuAdapter(mDanmakuView);
            mDanmakuContext.setCacheStuffer(new SpannedCacheStuffer(), danamakuAdapter); // 图文混排使用SpannedCacheStuffer
        }
    }

    private IDanmakuView getDanmakuView() {
        return this.danmakuView;
    }

    /**
     * 弹幕的显示与关闭
     */
    public void resolveDanmakuShow() {
        mHandler.post(() -> {
            if (danmuPreferences.isDanmuController()) {
                onPrepareDanmaku();
                if (!getDanmakuView().isShown())
                    getDanmakuView().show();
            } else {
                if (getDanmakuView().isShown()) {
                    getDanmakuView().hide();
                }
            }
        });
    }

    /**
     * 开始播放弹幕
     */
    protected void onPrepareDanmaku() {
        if (!danmuPreferences.isDanmuController()) {
            // 未开启不加载
            return;
        }

        IDanmakuView danmakuView = getDanmakuView();
        if (danmakuView != null && !danmakuView.isPrepared() && getParser() != null) {
            danmakuView.prepare(getParser(), mDanmakuContext);
        }

        if (danmakuView != null && playerItem.getSeasonId() != null) {
            // 设置时间偏移
            danmakuView.setOffsetTime(danmuPreferences.getSeasonDanmuOffset(playerItem.getSeasonId().toString()));
        }
    }

    private BaseDanmakuParser getParser() {
        UUID currentlyPlayingItemId = getCurrentlyPlayingItemId();
        if (currentlyPlayingItemId == null) {
            return null;
        }

        if (mParser == null || danmuItemId == null || !Objects.equals(currentlyPlayingItemId.toString(), danmuItemId.toString())) {
            mParser = createParser(getIsStream());
        }
        return mParser;
    }


    /**
     * 创建解析器对象，解析输入流
     *
     * @param stream
     * @return
     */
    private BaseDanmakuParser createParser(InputStream stream) {

        if (stream == null) {
            return new BaseDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }

        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);
        try {
            loader.load(stream);
        } catch (IllegalDataException e) {
            Timber.e(e, "加载弹幕失败");
        }
        BaseDanmakuParser parser = new BiliDanmukuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;
    }

    private UUID getCurrentlyPlayingItemId() {
        if (playerItem == null) {
            return null;
        }

        return playerItem.getItemId();
    }

    private InputStream getIsStream() {
        UUID currentUUID = getCurrentlyPlayingItemId();
        if (currentUUID == null) {
            return null;
        }

        Response<ByteReadChannel> danmuXmlFileById = null;
        try {
            danmuXmlFileById = repository.getDanmuXmlFileById(currentUUID, new HashSet<>());
        } catch (Exception e) {
            InvalidStatusException invalidStatusException = null;
            if (e instanceof InvalidStatusException) {
                invalidStatusException = (InvalidStatusException) e;
            }

            if (e.getCause() instanceof InvalidStatusException) {
                invalidStatusException = (InvalidStatusException) e.getCause();
            }

            if (invalidStatusException != null) {
                int status = invalidStatusException.getStatus();
                if (status >= 400 && status < 500) {
                    Timber.i("当前视频没有弹幕，忽略");
//                    CustomerCommonUtils.show(danmakuView.getView().getContext(), "该视频没有弹幕");
                }
            } else {
                CustomerCommonUtils.show(danmakuView.getView().getContext(), "获取弹幕失败:" + e.getMessage());
            }
        }

        if (danmuXmlFileById == null) {
            return null;
        }

        danmuItemId = currentUUID;
        ByteReadChannel content = danmuXmlFileById.getContent();
        return new ByteReadChannelInputStream(content);
    }
}
