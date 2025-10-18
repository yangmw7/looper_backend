package com.example.game_backend.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnnouncementCategory {
    NOTICE("공지"),
    EVENT("이벤트"),
    UPDATE("업데이트"),
    MAINTENANCE("점검");

    private final String description;
}