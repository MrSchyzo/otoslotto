package com.mrschyzo.hungarian.domain;

import com.mrschyzo.hungarian.domain.pick.Pick;
import com.mrschyzo.hungarian.domain.pick.PickParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

abstract class PickParserTest {
    protected PickParser parser;
    protected abstract PickParser getInstance();

    @BeforeEach
    public final void setUp() {
        this.parser = getInstance();
    }

    @Test
    public void pick_is_correctly_parsed_from_string_with_no_newline() {
        var result = parser.parse("1 2 3 4 5");
        var expected = Pick.of(1, 2, 3, 4, 5);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void pick_is_correctly_parsed_even_when_spaces_are_more() {
        var result = parser.parse("1  2 3   4 5");
        var expected = Pick.of(1, 2, 3, 4, 5);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void pick_parser_ignores_number_after_the_fifth_one() {
        var result = parser.parse("1  2 3   4 5 600 ");
        var expected = Pick.of(1, 2, 3, 4, 5);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void pick_parser_fails_if_there_arent_at_least_5_numbers() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2 3 4"));
    }
    @Test
    public void pick_parser_ignores_strings_that_are_not_numbers() {
        var result = parser.parse("1 s2 3s 4 5 6 7");
        var expected = Pick.of(1, 4, 5, 6, 7);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void pick_parser_ignores_strings_that_are_not_int_strings() {
        var result = parser.parse("1 2.3 3.3 4 5 6 7");
        var expected = Pick.of(1, 4, 5, 6, 7);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }

    @Test
    public void mutable_pick_is_correctly_parsed_from_string_with_no_newline() {
        var result = Pick.of(10, 20, 30, 40, 50);
        parser.parse("1 2 3 4 5", result);
        var expected = Pick.of(1, 2, 3, 4, 5);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void mutable_pick_is_correctly_parsed_even_when_spaces_are_more() {
        var result = Pick.of(10, 20, 30, 40, 50);
        parser.parse("1  2 3   4 5", result);
        var expected = Pick.of(1, 2, 3, 4, 5);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void mutable_pick_parser_ignores_number_after_the_fifth_one() {
        var result = Pick.of(10, 20, 30, 40, 50);
        parser.parse("1  2 3   4 5 600 ", result);
        var expected = Pick.of(1, 2, 3, 4, 5);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void mutable_pick_parser_fails_if_there_arent_at_least_5_numbers() {
        var result = Pick.of(10, 20, 30, 40, 50);
        Assertions.assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2 3 4", result));
    }
    @Test
    public void mutable_pick_parser_ignores_strings_that_are_not_numbers() {
        var result = Pick.of(10, 20, 30, 40, 50);
        parser.parse("1 s2 3s 4 5 6 7", result);
        var expected = Pick.of(1, 4, 5, 6, 7);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void mutable_pick_parser_ignores_strings_that_are_not_int_strings() {
        var result = Pick.of(10, 20, 30, 40, 50);
        parser.parse("1 2.3 3.3 4 5 6 7", result);
        var expected = Pick.of(1, 4, 5, 6, 7);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
}
