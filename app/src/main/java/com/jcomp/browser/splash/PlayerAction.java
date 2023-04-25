package com.jcomp.browser.splash;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.player.Player;
import com.jcomp.browser.welcome.Welcome;

public class PlayerAction extends BaseAction {
    String info;

    public PlayerAction(String info, AppCompatActivity activity) {
        super(activity);
        this.info = info;
    }

    @Override
    public void run() {
        Intent intent = new Intent(activity, Player.class);
        intent.putExtra(Player.PLAYER_INFO_KEY, info);
        startIntent(intent);
    }
}
