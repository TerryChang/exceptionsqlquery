package com.terry.mybatis.mybatis;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.apache.ibatis.reflection.ExceptionUtil.unwrapThrowable;
import static org.mybatis.spring.SqlSessionUtils.*;

public class CustomSqlSessionTemplate extends SqlSessionTemplate {

    public CustomSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) throws IllegalAccessException{
        this(sqlSessionFactory, sqlSessionFactory.getConfiguration().getDefaultExecutorType());
    }

    public CustomSqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType) throws IllegalAccessException{
        this(sqlSessionFactory, executorType,
                new MyBatisExceptionTranslator(
                        sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true));
    }

    public CustomSqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
                                    PersistenceExceptionTranslator exceptionTranslator) throws IllegalAccessException{

        super(sqlSessionFactory, executorType, exceptionTranslator);

        /*
        Mybatis 관련 작업을 하는데 사용되는 SqlSession 인터페이스 구현체로 이 클래스의 상위클래스인 SqlSessionTemplate
        클래스의 멤버변수인 sqlSessionProxy 멤버변수를 사용하게 된다.
        근데 문제는 이 sqlSessionProxy 변수에 기존 SqlSessionTemplate 클래스의 내부 클래스인 SqlSessionInterceptor 클래스를
        수정한 현재 클래스의 내부 클래스인 CustomSqlSessionInterceptor 클래스 객체를 사용해서 SqlSession 구현체를 설정해야
        하는데 이 sqlSessionProxy 멤버변수가 private final 타입으로 설정되어 있기 때문에 super 클래스의 생성자외엔 방법이
        없다. 그러나 super 클래스의 생성자에서 하드코딩으로 SqlSesstionInterceptor 클래스 객체를 설정하기 때문에 정상적인
        방법으로는 SqlSessionInterceptor 클래스 객체를 설정할 수 없었다.
        이를 하기 위하여 Java의 Reflection을 이용해서 sqlSessionProxy 멤버변수를 접근한뒤에 CustomSqlSessioninterceptor
        클래스가 적용된 SqlSession 구현체를 설정했다
         */
        Field field = ReflectionUtils.findField(this.getClass().getSuperclass(), "sqlSessionProxy");
        field.setAccessible(true);
        field.set(this, (SqlSession) newProxyInstance(
                SqlSessionFactory.class.getClassLoader(),
                new Class[] { SqlSession.class },
                new CustomSqlSessionInterceptor()));
        field.setAccessible(false);

    }

    private class CustomSqlSessionInterceptor implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
            ExecutorType executorType = getExecutorType();
            PersistenceExceptionTranslator exceptionTranslator = getPersistenceExceptionTranslator();

            SqlSession sqlSession = getSqlSession(
                    sqlSessionFactory,
                    executorType,
                    exceptionTranslator);
            String sqlQuery = "";
            try {
                Object result = method.invoke(sqlSession, args);
                if (!isSqlSessionTransactional(sqlSession, sqlSessionFactory)) {
                    sqlSession.commit(true);
                }
                return result;
            } catch (Throwable t) {
                sqlQuery = getQuery(sqlSession, (String)args[0], args[1]);
                Throwable unwrapped = unwrapThrowable(t);
                if (exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                    // release the connection to avoid a deadlock if the translator is no loaded. See issue #22
                    closeSqlSession(sqlSession, sqlSessionFactory);
                    sqlSession = null;
                    Throwable translated = exceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
                    if (translated != null) {
                        unwrapped = translated;
                    }
                }
                CustomDataAccessException cdae = new CustomDataAccessException(unwrapped.getMessage(), unwrapped.getCause(), sqlQuery);
                throw cdae;
            } finally {
                if (sqlSession != null) {
                    closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        }
    }

    /**
     * Mybatis Query를 파라미터가 Bounding 된 상태의 쿼리를 보고자 할때 사용
     * @param sqlSession
     * @param queryId
     * @param sqlParam
     * @return
     */
    private String getQuery(SqlSession sqlSession, String queryId , Object sqlParam) throws NoSuchFieldException, IllegalAccessException{

        BoundSql boundSql = sqlSession.getConfiguration().getMappedStatement(queryId).getBoundSql(sqlParam);
        String sql = boundSql.getSql();

        if(sqlParam == null){
            sql = sql.replaceFirst("\\?", "''");
        }else{
            if(sqlParam instanceof Number){ // Integer, Long, Float, Double 등의 숫자형 데이터 클래스는 Number 클래스를 상속받기 때문에 Number로 체크한다
                sql = sql.replaceFirst("\\?", sqlParam.toString());
            }else if(sqlParam instanceof String){	// 해당 파라미터의 클래스가 String 일 경우(이 경우는 앞뒤에 '(홑따옴표)를 붙여야해서 별도 처리
                sql = sql.replaceFirst("\\?", "'" + sqlParam + "'");
            }else if(sqlParam instanceof Map) {        // 해당 파라미터가 Map 일 경우

        	/*
        	 * 쿼리의 ?와 매핑되는 실제 값들의 정보가 들어있는 ParameterMapping 객체가 들어간 List 객체로 return이 된다.
        	 * 이때 List 객체의 0번째 순서에 있는 ParameterMapping 객체가 쿼리의 첫번째 ?와 매핑이 된다
        	 * 이런 식으로 쿼리의 ?과 ParameterMapping 객체들을 Mapping 한다
        	 */
                List<ParameterMapping> paramMapping = boundSql.getParameterMappings();

                for (ParameterMapping mapping : paramMapping) {
                    String propValue = mapping.getProperty();        // 파라미터로 넘긴 Map의 key 값이 들어오게 된다
                    Object value = ((Map) sqlParam).get(propValue);    // 넘겨받은 key 값을 이용해 실제 값을 꺼낸다
                    if (value instanceof String) {            // SQL의 ? 대신에 실제 값을 넣는다. 이때 String 일 경우는 '를 붙여야 하기땜에 별도 처리
                        sql = sql.replaceFirst("\\?", "'" + value + "'");
                    } else {
                        sql = sql.replaceFirst("\\?", value.toString());
                    }

                }
            }else{					// 해당 파라미터가 사용자 정의 클래스일 경우

        	/*
        	 * 쿼리의 ?와 매핑되는 실제 값들이 List 객체로 return이 된다.
        	 * 이때 List 객체의 0번째 순서에 있는 ParameterMapping 객체가 쿼리의 첫번째 ?와 매핑이 된다
        	 * 이런 식으로 쿼리의 ?과 ParameterMapping 객체들을 Mapping 한다
        	 */
                List<ParameterMapping> paramMapping = boundSql.getParameterMappings();

                Class<? extends Object> paramClass = sqlParam.getClass();
                // logger.debug("paramClass.getName() : {}", paramClass.getName());
                for(ParameterMapping mapping : paramMapping){

                    String propValue = mapping.getProperty();                   // 해당 파라미터로 넘겨받은 사용자 정의 클래스 객체의 멤버변수명
                    Field field = doDeclaredField(paramClass, propValue);       // 관련 멤버변수 Field 객체 얻어옴
                    if(field == null) {                                         // 최상위 클래스까지 갔는데도 필드를 찾지 못할경우엔 null이 return 되기 때문에 null을 return 하게 되면 NoSuchFieldException 예외를 던진다
                        throw new NoSuchFieldException();
                    }
                    field.setAccessible(true);                    // 멤버변수의 접근자가 private일 경우 reflection을 이용하여 값을 해당 멤버변수의 값을 가져오기 위해 별도로 셋팅
                    Class<?> javaType = mapping.getJavaType();            // 해당 파라미터로 넘겨받은 사용자 정의 클래스 객체의 멤버변수의 타입

                    if (String.class == javaType) {                // SQL의 ? 대신에 실제 값을 넣는다. 이때 String 일 경우는 '를 붙여야 하기땜에 별도 처리
                        sql = sql.replaceFirst("\\?", "'" + field.get(sqlParam) + "'");
                    } else {
                        sql = sql.replaceFirst("\\?", field.get(sqlParam).toString());
                    }
                }
            }
        }
        return sql;
    }

    /**
     * 클래스 필드 검색 재귀함수
     * @param paramClass
     * @param propValue
     * @return
     */
    private Field doDeclaredField(Class<? extends Object> paramClass, String propValue){
        Field field = null;
        try {
            /*
            * 해당 객체의 필드를 검색 한다.
            * 존재 하지 않을 경우 NoSuchFieldException 발생
            */
            field = paramClass.getDeclaredField(propValue);

        } catch ( NoSuchFieldException e ){
            // NoSuchFieldException 발생 할경우 상위 클래스를 검색 한다.
            field = doDeclaredField(paramClass.getSuperclass(), propValue);
        }

        return field;
    }

}
