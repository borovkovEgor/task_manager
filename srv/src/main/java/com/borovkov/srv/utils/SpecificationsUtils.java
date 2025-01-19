package com.borovkov.srv.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationsUtils {

    public static <T> void ilike(List<Predicate> predicates,
                             CriteriaBuilder cb,
                             Root<T> root,
                             String filedName,
                             String value,
                             boolean strictComparison)
    {
        if (StringUtils.hasText(value)) {
            String pattern;
            if (strictComparison) {
                pattern = value.toLowerCase();
            } else {
                pattern = "%" + value.toLowerCase() + "%";
            }
            predicates.add(cb.like(cb.lower(root.get(filedName)), pattern));
        }
    }

    public static <T> void equal(List<Predicate> predicates,
                              CriteriaBuilder cb,
                              Root<T> root,
                              String filedName,
                              Object value)
    {
        if (value != null) {
            predicates.add(cb.equal(root.get(filedName), value));
        }
    }

    public static <T> void dateFrom(List<Predicate> predicates,
                                CriteriaBuilder cb,
                                Root<T> root,
                                String filedName,
                                OffsetDateTime value)
    {
        if (value != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(filedName), value));
        }
    }

    public static <T> void dateTo(List<Predicate> predicates,
                                  CriteriaBuilder cb,
                                  Root<T> root,
                                  String filedName,
                                  OffsetDateTime value)
    {
        if (value != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(filedName), value));
        }
    }
}
