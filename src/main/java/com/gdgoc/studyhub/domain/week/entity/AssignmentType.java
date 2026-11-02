package com.gdgoc.studyhub.domain.week.entity;

/**
 * 주차 과제의 제출 방식. 주차를 만들 때 정하고, 이후에는 바꿀 수 없다 (이미 낸 제출물과 형식이 어긋나지 않게).
 */
public enum AssignmentType {
    LINK,
    FILE,
    LINK_AND_FILE
}