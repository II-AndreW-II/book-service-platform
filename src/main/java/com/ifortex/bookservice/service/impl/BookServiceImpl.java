package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.service.BookService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    @PersistenceContext
    private final EntityManager em;

    @Transactional(readOnly = true)
    @Override
    public Map<String, Long> getBooks() {
        List<Object[]> results = em.createNativeQuery("""
                    SELECT g.genre, COUNT(*) AS books_amount
                    FROM (SELECT UNNEST(genre) AS genre FROM books) AS g
                    GROUP BY g.genre
                    ORDER BY books_amount DESC
                """).getResultList();

        return results.stream()
                .collect(Collectors.toMap(result -> (String) result[0],
                        result -> ((Number) result[1]).longValue(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new));
    }
}
