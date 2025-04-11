package dev.jdtech.jellyfin.customer.danmu;

import dev.jdtech.jellyfin.models.PlayerItem;

public interface DanmuController {

    void changeItem(PlayerItem playerItem);

    void stop();

    void start();
}
