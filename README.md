# exceptionsqlquery
Spring과 Mybatis 연동시 SQL 실행 관련 에러가 발생될 경우 던저지는 예외에 실행할 당시의 SQL문이 같이 포함되어 던져지는 예제<br/>
Spring Boot 로 제작되었으며 Spring Boot의 Web Module이 설정되어 있기 때문에 브라우저에서도 테스트가 가능하다<br/>

웹브라우저에서 테스트할 때는 다음과 같이 한다

http://localhost:8080/errortest

그러면 웹브라우저에 예외가 발생된 Query문(예제로 보이는 것은 insert 문이다)이 보인다<br/>
refresh 할때마다 key 값이 증가하기 때문에 실시간으로의 확인이 가능하다

