package com.mrschyzo.hungarian.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PickParserTest {
    @Test
    public void pick_is_correctly_parsed_from_string_with_no_newline() {
        PickParser parser = new PickParser();
        var result = parser.parse("1 2 3 4 5");
        var expected = Pick.of(1, 2, 3, 4, 5);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void pick_is_correctly_parsed_even_when_spaces_are_more() {
        PickParser parser = new PickParser();
        var result = parser.parse("1  2 3   4 5");
        var expected = Pick.of(1, 2, 3, 4, 5);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void pick_parser_ignores_number_after_the_fifth_one() {
        PickParser parser = new PickParser();
        var result = parser.parse("1  2 3   4 5 600 ");
        var expected = Pick.of(1, 2, 3, 4, 5);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void pick_parser_fails_if_there_arent_at_least_5_numbers() {
        PickParser parser = new PickParser();
        Assertions.assertThrows(IllegalArgumentException.class, () -> parser.parse("1 2 3 4"));
    }
    @Test
    public void pick_parser_ignores_strings_that_are_not_numbers() {
        PickParser parser = new PickParser();
        var result = parser.parse("1 s2 3s 4 5 6 7");
        var expected = Pick.of(1, 4, 5, 6, 7);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
    @Test
    public void pick_parser_ignores_strings_that_are_not_int_strings() {
        PickParser parser = new PickParser();
        var result = parser.parse("1 2.3 3.3 4 5 6 7");
        var expected = Pick.of(1, 4, 5, 6, 7);
        Assertions.assertArrayEquals(expected.toBitmask(), result.toBitmask());
    }
}
