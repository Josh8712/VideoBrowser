package com.jcomp.browser.parser.player;

public class ScriptPlayer {
    public static String CC = "Array.from(document.querySelectorAll('iframe[class]')).filter(e=> {return e.classList[0].includes('player')}).map(e=> {return e.src;})";
    public static String Net = "(document.querySelector('#__NEXT_DATA__')?.text && JSON.parse(document.querySelector('#__NEXT_DATA__')?.text)?.props?.initialState?.video?.data?.srcs.filter(function (value, index, self) {  return self.indexOf(value) === index;}))";
    public static String TWDVD = "[document.querySelector('body > iframe')].map(e=> {return e.src;})";
}
