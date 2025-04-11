package dev.jdtech.jellyfin.customer.danmu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;

import dev.jdtech.jellyfin.models.PlayerItem;
import timber.log.Timber;

public abstract class AbstractDanmuControllerListener implements Player.Listener {
    private int[] seekEvents = new int[]{
            Player.EVENT_SEEK_BACK_INCREMENT_CHANGED
            ,Player.EVENT_SEEK_FORWARD_INCREMENT_CHANGED
            ,Player.EVENT_MAX_SEEK_TO_PREVIOUS_POSITION_CHANGED
            ,Player.EVENT_POSITION_DISCONTINUITY
    };

    private Player player;
    private boolean seek = false;
    @Override
    public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
        this.player = player;
        Timber.i("AbstractDanmuControllerListener onEvents events.size=%d", events.size());
        // 播放状态发生变化
        if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
//            player.getPlaybackState()
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
            setVideoSpeed(speed);
        }

    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        // 播放异常
        release();
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        Timber.i("AbstractDanmuControllerListener onPositionDiscontinuity newPosition, reason=%d", reason);
//        seek = true;
    }

    @Override
    public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
        // seek跳转
//        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
//
//        }
        Timber.i("AbstractDanmuControllerListener onPositionDiscontinuity newPosition contentPositionMs=%d, reason=%d", newPosition.contentPositionMs, reason);
        seekTo(newPosition.contentPositionMs);
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        if (playWhenReady) {
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
        if (playbackState == Player.STATE_READY) {
            if (seek && player != null) {
                Timber.i("AbstractDanmuControllerListener onPlaybackStateChanged seek ContentPosition=%d, CurrentPosition=%d", player.getContentPosition(), player.getCurrentPosition());
                seekTo(player.getCurrentPosition());
                seek = false;
            }
            resume();
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

    protected abstract void changePlayerItem(PlayerItem currentItem);

    protected abstract void pause();

    protected abstract void resume();

    protected abstract void setVideoSpeed(float speed);

    protected abstract PlayerItem getItemById(String itemId);

    protected abstract void release();

    protected abstract void seekTo(long newPosition);
}
