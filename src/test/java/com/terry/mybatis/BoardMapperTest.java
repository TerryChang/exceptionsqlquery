package com.terry.mybatis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.terry.mybatis.domain.Board;
import com.terry.mybatis.mapper.BoardMapper;
import com.terry.mybatis.mybatis.CustomDataAccessException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class BoardMapperTest {

    @Autowired
    BoardMapper mapper;

    @Test
    public void insertTest(){

        Board board = new Board(0, "title1", "contents1",0);
        mapper.insertBoard(board);
        List<Board> boardList = mapper.listBoard(null);
        Board selectBoard = boardList.get(0);
        assertThat(selectBoard.getTitle()).isEqualTo(board.getTitle());
        assertThat(selectBoard.getContents()).isEqualTo(board.getContents());
    }

    @Test
    public void insertExceptionTest(){
        Board board = new Board(0, "title123456789012345678901234567890", "contents1", 0);
        assertThatThrownBy(() -> mapper.insertBoard(board)).isInstanceOf(CustomDataAccessException.class);
    }
}
