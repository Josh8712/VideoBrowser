package com.jcomp.browser.parser;

public class ScriptHLS {
    public static String Jable = "window?.hlsUrl, window?.vttUrl";
    public static String Avple = "window?.hls?.userConfig ? undefined : window?.hls?.url";
    public static String MISS = "window?.hls?.userConfig ? (window?.player?.config?.thumbnail ? window?.hls?.url : undefined) : undefined, JSON.stringify(window?.player?.config?.thumbnail)";
    public static String Tube = "window?.flashvars?.video_alt_url2 || window?.flashvars?.video_alt_url || window?.flashvars?.video_url || undefined, JSON.stringify(window?.flashvars)";
    public static String CC = "window?.player?.src ? (typeof(window.player.src) == 'function' ? window.player.src() : undefined) : undefined, undefined";

    public static String NET_AV_WW_MM = "window?.clientSide?.pl?.sources?.at(-1)?.file";
    public static String NET_ST = "document.getElementById('robotlink')?.innerHTML";
    public static String NET_FI = "window?.jwplayer?.()?.getPlaylist?.()?.[0]?.sources?.[0]?.file";
    public static String NET_DO = "window?.dsplayer?.getMedia?.()?.src?.at(-1)?.src || window?.dsplayer?.currentSource()?.src";
    public static String NET_EMBBED = "window.document.querySelector('#my-video source')?.src";

    public static String II159 = "window?.player?.options?.source";

    public static final String[] SCRIPT_SET = {
            Jable, Avple, MISS, Tube, CC, NET_AV_WW_MM, NET_ST, NET_FI, NET_DO, II159, NET_EMBBED
    };

}


