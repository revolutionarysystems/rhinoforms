package com.rhinoforms.flow;

import java.io.Serializable;
import java.util.regex.Pattern;

public class ErrorHandler implements Serializable{

    private String codeRegex;
    private Pattern compiledCodeRegex;
    private String textRegex;
    private Pattern compiledTextRegex;
    private String bodyRegex;
    private Pattern compiledBodyRegex;
    private String target;

    public ErrorHandler(String target) {
        this.target = target;
        setCodeRegex(".*");
        setTextRegex(".*");
        setBodyRegex(".*");
    }

    public String getCodeRegex() {
        return codeRegex;
    }

    public void setCodeRegex(String codeRegex) {
        this.codeRegex = codeRegex;
        compiledCodeRegex = Pattern.compile(codeRegex);
    }

    public Pattern getCompiledCodeRegex() {
        return compiledCodeRegex;
    }

    public String getTextRegex() {
        return textRegex;
    }

    public void setTextRegex(String textRegex) {
        this.textRegex = textRegex;
        compiledTextRegex = Pattern.compile(textRegex);
    }

    public String getBodyRegex() {
        return bodyRegex;
    }

    public void setBodyRegex(String bodyRegex) {
        this.bodyRegex = bodyRegex;
        compiledBodyRegex = Pattern.compile(bodyRegex, Pattern.DOTALL | Pattern.MULTILINE);
    }

    public Pattern getCompiledTextRegex() {
        return compiledTextRegex;
    }

    public Pattern getCompiledBodyRegex() {
        return compiledBodyRegex;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
    
}
