package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.model.Member;
import com.ifortex.bookservice.service.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private static final String ROMANCE_GENRE = "Romance";
    LocalDateTime START_DATE_OF_REGISTRATION = LocalDate.of(2023, 1, 1).atStartOfDay();
    LocalDateTime END_DATE_OF_REGISTRATION = LocalDate.of(2023, 12, 31).atTime(23, 59, 59);

    @PersistenceContext
    private final EntityManager em;

    @Transactional(readOnly = true)
    @Override
    public Member findMember() {
        return (Member) em.createNativeQuery("""
                        SELECT * FROM members
                        JOIN member_books ON members.id = member_books.member_id
                        WHERE member_books.member_id IN (SELECT id FROM books
                            WHERE ?1 = ANY (genre)
                            ORDER BY publication_date
                            LIMIT 1)
                        ORDER BY membership_date
                """, Member.class)
                .setMaxResults(1)
                .setParameter(1, ROMANCE_GENRE)
                .getSingleResult();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Member> findMembers() {
        return em.createQuery("""
                    SELECT m FROM Member m
                    LEFT JOIN FETCH m.borrowedBooks
                    WHERE SIZE(m.borrowedBooks) = 0 
                      AND m.membershipDate BETWEEN :startDate AND :endDate
                    """, Member.class)
                .setParameter("startDate", START_DATE_OF_REGISTRATION)
                .setParameter("endDate", END_DATE_OF_REGISTRATION)
                .getResultList();
    }
}
