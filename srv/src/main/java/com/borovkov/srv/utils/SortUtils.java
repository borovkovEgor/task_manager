package com.borovkov.srv.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
public class SortUtils {

    public static Sort buildSort(String sort, Class<?> clazz) {
        Sort sortOrder = Sort.by(Sort.Order.asc("id"));

        if (!StringUtils.isBlank(sort)) {
            String[] sortParams = sort.split(",");
            if (sortParams.length == 2) {
                String field = sortParams[0];
                String direction = sortParams[1];

                Set<String> allowedFields = new HashSet<>();
                Field[] fields = clazz.getDeclaredFields();

                for (Field f : fields) {
                    allowedFields.add(f.getName());
                }

                if (allowedFields.contains(field)) {
                    Sort.Direction sortDirection = "desk".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
                    return Sort.by(new Sort.Order(sortDirection, field));
                } else {
                    throw new IllegalArgumentException("Invalid sorting field: " + field);
                }
            } else {
                throw new IllegalArgumentException("Sort parameter should contain exactly two values: field and direction (e.g., createdAt,asc)");
            }
        }

        return sortOrder;
    }
}