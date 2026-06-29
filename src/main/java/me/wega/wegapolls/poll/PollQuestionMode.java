package me.wega.wegapolls.poll;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PollQuestionMode {
    SINGLE_CHOICE("Single Choice"),
    MULTI_CHOICE("Multi Choice");

    private final String label;
}