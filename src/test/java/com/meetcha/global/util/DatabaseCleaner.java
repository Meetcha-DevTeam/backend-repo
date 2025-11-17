package com.meetcha.global.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseCleaner implements InitializingBean {

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> tableNames;

    @Override
    public void afterPropertiesSet() {
        @SuppressWarnings("unchecked")
        List<Object> rawList = entityManager.createNativeQuery("SHOW TABLES").getResultList();

        tableNames = new ArrayList<>();
        for (Object tableInfo : rawList) {
            if (tableInfo instanceof Object[]) {
                tableNames.add((String)((Object[]) tableInfo)[0]);
            } else {
                tableNames.add((String) tableInfo);
            }
        }
    }

    @Transactional
    public void clear() {
        entityManager.flush();
        //외래 키 제약 조건을 무시
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        //모든 테이블의 데이터를 삭제
        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }

        //외래 키 제약 조건을 다시 활성화합니다.
        //(ID를 1로 리셋하는 코드를 제거 -> UUID PK와 호환)
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }
}