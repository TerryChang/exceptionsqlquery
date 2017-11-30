package com.terry.mybatis.service;

import com.terry.mybatis.domain.Board;

import java.util.List;

public interface BoardService {
    public List<Board> listBoard(String searchValue);
    public Board viewBoard(long idx);
    public void insertBoard(Board board);
    public void updateBoard(Board board);
    public void deleteBoard(long idx);
}
