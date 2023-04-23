package com.jcomp.browser.parser.model;

public class ScriptModelName {
    public static String Jable = "Array.from(document.querySelectorAll('.model')).map(e=>{return e.children[0]?.getAttribute('title')||e.children[0]?.getAttribute('data-original-title')})";
}
