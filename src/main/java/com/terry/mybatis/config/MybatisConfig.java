package com.terry.mybatis.config;

import com.terry.mybatis.mybatis.CustomSqlSessionFactoryBean;
import com.terry.mybatis.mybatis.CustomSqlSessionTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class MybatisConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource){
        SqlSessionFactory sqlSessionFactory = null;

        try{
            CustomSqlSessionFactoryBean sqlSessionFactoryBean = new CustomSqlSessionFactoryBean();
            sqlSessionFactoryBean.setDataSource(dataSource);
            // sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("com/terry/springconfig/resources/mybatis/config/mybatis-config.xml"));
            // sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/com/terry/springconfig/resources/mybatis/mapper/**/*.xml"));
            sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("mybatis/mybatis-config.xml"));
            sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/mapper/**/*.xml"));
            sqlSessionFactoryBean.setCheckInterval(60000);
            sqlSessionFactory = sqlSessionFactoryBean.getObject();
        }catch(IOException ioe){
            logger.error("sqlSessionFactory.setMapperLocations Mapper Path Not Found", ioe);
        }catch(Exception e){
            logger.error("sqlSessionFactory Exception", e);
        }

        return sqlSessionFactory;
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setAnnotationClass(Mapper.class);
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        mapperScannerConfigurer.setSqlSessionTemplateBeanName("customSqlSessionTemplate");
        mapperScannerConfigurer.setBasePackage("com.terry.mybatis.mapper");
        return mapperScannerConfigurer;
    }

    @Bean(destroyMethod="clearCache")
    public CustomSqlSessionTemplate customSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) throws IllegalAccessException{
        return new CustomSqlSessionTemplate(sqlSessionFactory);
    }

    /*
    @Bean(destroyMethod="clearCache")
    public SqlSessionTemplate sqlSession(){
        SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactory());
        return template;
    }
    */
}
