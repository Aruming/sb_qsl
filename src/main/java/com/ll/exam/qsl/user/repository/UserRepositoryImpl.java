package com.ll.exam.qsl.user.repository;

import com.ll.exam.qsl.user.entity.QSiteUser;
import com.ll.exam.qsl.user.entity.SiteUser;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.ll.exam.qsl.interestKeyword.entity.QInterestKeyword.interestKeyword;
import static com.ll.exam.qsl.user.entity.QSiteUser.siteUser;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public SiteUser getQslUser(Long id) {
        /*
        SELECT *
        FROM site_user
        WHERE id = 1
        */

        return jpaQueryFactory
                .select(siteUser)
                .from(siteUser)
                .where(siteUser.id.eq(id))
                .fetchOne();
    }

    @Override
    public long getQslCount() {
        return jpaQueryFactory
                .select(siteUser.count())
                .from(siteUser)
                .fetchOne();
    }

    @Override
    public SiteUser getQslUserOrderByIdAscOne() {
        return jpaQueryFactory
                .select(siteUser)
                .from(siteUser)
                .orderBy(siteUser.id.asc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public List<SiteUser> getQslUsersOrderByIdAsc() {
        return jpaQueryFactory
                .select(siteUser)
                .from(siteUser)
                .orderBy(siteUser.id.asc())
                .fetch();

    }

    @Override
    public List<SiteUser> searchQsl(String keyword) {
        return jpaQueryFactory
                .select(siteUser)
                .from(siteUser)
                .where(siteUser.username.contains(keyword)
                        .or(siteUser.email.contains(keyword))
                )
                .orderBy(siteUser.id.desc())
                .fetch();
    }

    @Override
    public Page<SiteUser> searchQsl(String keyword, Pageable pageable) {
        JPAQuery<SiteUser> usersQuery = jpaQueryFactory
                .select(siteUser)
                .from(siteUser)
                .where(
                        siteUser.username.contains(keyword)
                                .or(siteUser.email.contains(keyword))
                )
                .offset(pageable.getOffset()) // 몇개를 건너 뛰어야 하는지 LIMIT {1}, ?
                .limit(pageable.getPageSize()); // 한페이지에 몇개가 보여야 하는지 LIMIT ?, {1}

        for (Sort.Order o : pageable.getSort()) {
            PathBuilder pathBuilder = new PathBuilder(siteUser.getType(), siteUser.getMetadata());
            usersQuery.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty())));
        }

        List<SiteUser> users = usersQuery.fetch();

        JPAQuery<Long> usersCountQuery = jpaQueryFactory
                .select(siteUser.count())
                .from(siteUser)
                .where(
                        siteUser.username.contains(keyword)
                                .or(siteUser.email.contains(keyword))
                );

        return PageableExecutionUtils.getPage(users, pageable, usersCountQuery::fetchOne);
    }

    @Override
    public List<SiteUser> getQslUsersByInterestKeyword(String keyword) {
        /*
        SELECT SU.*
        FROM site_user AS SU
        INNER JOIN site_user_interest_keywords AS SUIK
        ON SU.id = SUIK.site_user_id
        INNER JOIN interest_keyword AS IK
        ON IK.content = SUIK.interest_keywords_content
        WHERE IK.content = "축구";
        */

        return jpaQueryFactory
                .selectFrom(siteUser)
                .innerJoin(siteUser.interestKeywords, interestKeyword)
                .where(
                        interestKeyword.content.eq(keyword)
                )
                .fetch();
    }

    @Override
    public List<String> getKeywordContentsByFollowingsOf(SiteUser user) {
        QSiteUser siteUser2 = new QSiteUser("SU2");

        List<Long> ids = jpaQueryFactory.select(siteUser.id)
                .from(siteUser)
                .innerJoin(siteUser.followers, siteUser2)
                .where(siteUser2.id.eq(user.getId()))
                .fetch();

        return jpaQueryFactory.select(interestKeyword.content).distinct()
                .from(interestKeyword)
                .where(interestKeyword.user.id.in(ids))
                .fetch();
    }
}