package com.mrschyzo.hungarian.domain.pick;

public interface PickParser {
    Pick parse(String str);
    void parse(String str, Pick pick);
}
