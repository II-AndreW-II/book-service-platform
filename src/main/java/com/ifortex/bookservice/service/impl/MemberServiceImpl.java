package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.model.Member;
import com.ifortex.bookservice.service.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private static final String ROMANCE_GENRE = "Romance";
    private static final Integer YEAR_OF_REGISTRATION = 2023;

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
                        SELECT Member FROM Member
                        LEFT JOIN FETCH Member.borrowedBooks
                        WHERE Member.borrowedBooks IS NULL AND YEAR(Member.membershipDate) = ?1
                        """, Member.class)
                .setParameter(1, YEAR_OF_REGISTRATION)
                .getResultList();
    }
}
