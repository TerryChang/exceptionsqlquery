package com.terry.mybatis.service.impl;

import com.terry.mybatis.domain.Board;
import com.terry.mybatis.mapper.BoardMapper;
import com.terry.mybatis.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BoardServiceImpl implements BoardService {

    @Autowired
    BoardMapper mapper;

    @Override
    public List<Board> listBoard(String searchValue) {
        return mapper.listBoard(searchValue);
    }

    @Override
    public Board viewBoard(long idx) {
        return mapper.viewBoard(idx);
    }

    @Override
    public void insertBoard(Board board) {
        mapper.insertBoard(board);
    }

    @Override
    public void updateBoard(Board board) {
        mapper.updateBoard(board);
    }

    @Override
    public void deleteBoard(long idx) {
        mapper.deleteBoard(idx);
    }

}
