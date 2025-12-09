package com.project.ultils;

import java.util.List;

public interface TopicMapper {
    List<String> getTopicsFromEventType(String type);
}
