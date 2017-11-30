package com.terry.mybatis.mapper;

import com.terry.mybatis.domain.Board;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BoardMapper {
    public List<Board> listBoard(String searchValue);
    public Board viewBoard(long idx);
    public void insertBoard(Board board);
    public void updateBoard(Board board);
    public void updateBoardReadCount(long idx);
    public void deleteBoard(long idx);
}
