package com.mrschyzo.hungarian.domain;

import com.mrschyzo.hungarian.domain.pick.PickParser;
import com.mrschyzo.hungarian.domain.pick.SimplePickParser;

public class SimplePickParserTest extends PickParserTest {
    @Override
    protected PickParser getInstance() {
        return new SimplePickParser();
    }
}
