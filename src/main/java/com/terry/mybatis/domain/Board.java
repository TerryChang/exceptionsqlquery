package com.terry.mybatis.domain;

public class Board {
    private long idx;
    private String title;
    private String contents;
    private int readCount;

    public Board(){

    }

    public Board(long idx, String title, String contents, int readCount) {
        this.idx = idx;
        this.title = title;
        this.contents = contents;
        this.readCount = readCount;
    }

    public long getIdx() {
        return idx;
    }

    public void setIdx(long idx) {
        this.idx = idx;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }
}
