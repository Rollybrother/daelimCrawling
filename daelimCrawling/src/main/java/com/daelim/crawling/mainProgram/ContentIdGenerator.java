package com.daelim.crawling.mainProgram;

import java.util.UUID;

public class ContentIdGenerator {

    public static String getContentId() {
        return UUID.randomUUID().toString();
    }
}
