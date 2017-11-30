package com.terry.mybatis.controller;

import com.terry.mybatis.domain.Board;
import com.terry.mybatis.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    BoardService service;

    @RequestMapping(value="/errortest")
    public Map<String, String> sendError(){
        Map<String, String> result = new HashMap<String,String>();
        Board board = new Board(0, "title123456789012345678901234567890", "contents1", 0);
        service.insertBoard(board);
        return result;
    }
}
