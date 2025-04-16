package dev.jdtech.jellyfin.customer.danmu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import dev.jdtech.jellyfin.models.PlayerItem;
import timber.log.Timber;

public abstract class AbstractDanmuControllerListener implements Player.Listener {
    private int[] seekEvents = new int[]{
            Player.EVENT_SEEK_BACK_INCREMENT_CHANGED
            ,Player.EVENT_SEEK_FORWARD_INCREMENT_CHANGED
            ,Player.EVENT_MAX_SEEK_TO_PREVIOUS_POSITION_CHANGED
            ,Player.EVENT_POSITION_DISCONTINUITY
            ,Player.EVENT_MEDIA_METADATA_CHANGED
    };

    private boolean seek = false;
    @Override
    public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
        List<Integer> eventList = new ArrayList<>(events.size());
        for (int i = 0; i < events.size(); i++) {
            eventList.add(events.get(i));
        }
        Timber.i("AbstractDanmuControllerListener onEvents events.size=%d, eventList=%s", events.size(), JSON.toJSONString(eventList));
        // 播放状态发生变化
        if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
//            player.getPlaybackState()
        }

        // 触发第一帧渲染
        if (events.contains(Player.EVENT_RENDERED_FIRST_FRAME)) {
            MediaItem currentMediaItem = player.getCurrentMediaItem();
            if (currentMediaItem != null) {
                changePlayerItem(getItemById(currentMediaItem.mediaId));
                seek = true;
            }
        }

        if (events.containsAny(seekEvents)) {
            Timber.i("AbstractDanmuControllerListener onEvents seek ContentPosition=%d, CurrentPosition=%d", player.getContentPosition(), player.getCurrentPosition());
            seek = true;
        }

//        if (seek && events.containsAny(Player.EVENT_PLAY_WHEN_READY_CHANGED) && player.getPlayWhenReady()) {
//            seekTo(player.getCurrentPosition());
//            seek = false;
//        }

        // 播放速度变化
        if (events.contains(Player.EVENT_PLAYBACK_PARAMETERS_CHANGED)) {
            float speed = player.getPlaybackParameters().speed;
//            Timber.i("AbstractDanmuControllerListener onEvents 修改播放速度 speed=%f", speed);
            setVideoSpeed(speed);
        }

    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        // 播放异常
        release();
    }

//    @Override
//    public void onPositionDiscontinuity(int reason) {
//        Timber.i("AbstractDanmuControllerListener onPositionDiscontinuity newPosition, reason=%d", reason);
////        seek = true;
//    }
//
//    @Override
//    public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
//        // seek跳转
////        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
////
////        }
//        Timber.i("AbstractDanmuControllerListener onPositionDiscontinuity newPosition contentPositionMs=%d, reason=%d", newPosition.contentPositionMs, reason);
////        seekTo(newPosition.contentPositionMs);
//    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        Timber.i("AbstractDanmuControllerListener onPlayWhenReadyChanged playWhenReady=%s, reason=%d", playWhenReady, reason);
        if (playWhenReady) {
            if (seek) {
                Player player = getPlayer();
                Timber.i("AbstractDanmuControllerListener onPlaybackStateChanged seek ContentPosition=%d, CurrentPosition=%d", player.getContentPosition(), player.getCurrentPosition());
                seekTo(player.getCurrentPosition());
                seek = false;
            }
            resume();
        } else {
            pause();
        }
    }

    @Override
    public void onPlaybackStateChanged(@Player.State int playbackState) {
        Timber.i("AbstractDanmuControllerListener onPlaybackStateChanged playbackState %d", playbackState);
        // 播放完成
        if (playbackState == Player.STATE_ENDED) {
            release();
            return;
        }

        // 缓冲中
        if (playbackState == Player.STATE_BUFFERING) {
            pause();
        }

        // 开始播放
        Player player = getPlayer();
        if (playbackState == Player.STATE_READY) {
            if (player != null && player.isPlaying()) {
                if (seek) {
                    Timber.i("AbstractDanmuControllerListener onPlaybackStateChanged seek ContentPosition=%d, CurrentPosition=%d", player.getContentPosition(), player.getCurrentPosition());
                    seekTo(player.getCurrentPosition());
                    seek = false;
                }
                resume();
            }
        }
    }

    @Override
    public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
        if (mediaItem == null) {
            Timber.i("AbstractDanmuControllerListener onMediaItemTransition playbackSmediaItem=null, reason=%d", reason);
            return;
        }
        Timber.i("AbstractDanmuControllerListener onMediaItemTransition playbackSmediaItem=%s, reason=%d", mediaItem.mediaId, reason);

        PlayerItem currentItem = getItemById(mediaItem.mediaId);
        if (currentItem == null) {
            return;
        }
        changePlayerItem(currentItem);
    }

//    protected abstract void playReady();

    protected abstract Player getPlayer();

    protected abstract void changePlayerItem(PlayerItem currentItem);

    protected abstract void pause();

    protected abstract void resume();

    protected abstract void setVideoSpeed(float speed);

    protected abstract PlayerItem getItemById(String itemId);

    protected abstract void release();

    protected abstract void seekTo(long newPosition);
}
