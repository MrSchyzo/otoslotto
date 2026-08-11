package com.mrschyzo.hungarian.domain;

import com.mrschyzo.hungarian.domain.pick.Pick;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PickTest {
    @Test
    public void can_create_pick_from_5_distinct_integers_between_1_and_90() {
        Assertions.assertDoesNotThrow(() -> Pick.of(1, 2, 3, 4, 90));
    }
    @Test
    public void cannot_create_pick_if_one_element_is_less_than_1() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Pick.of(1, 2, 3, 4, 0));
    }
    @Test
    public void cannot_create_pick_if_one_element_is_more_than_90() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Pick.of(1, 2, 3, 4, 91));
    }
    @Test
    public void cannot_create_pick_all_elements_are_not_distinct() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Pick.of(1, 2, 3, 2, 4));
    }
    @Test
    public void pick_can_be_represented_into_a_3_int_bitmask_with_some_bit_set_to_0() {
        var pick = Pick.of(1, 14, 25, 64, 90);
        var expected = new int[] {
                0b1000_0000_0000_0100_0000_0000_1000_0000,
                0b0000_0000_0000_0000_0000_0000_0000_0001,
                0b0000_0000_0000_0000_0000_0000_0100_0000,
        };
        var result = pick.toBitmask();
        Assertions.assertArrayEquals(expected, result);
    }
    @Test
    public void pick_bitset_does_not_care_about_order_of_input_numbers() {
        var pick = Pick.of(1, 14, 25, 64, 90);
        var pick2 = Pick.of(14, 25, 64, 1, 90);
        Assertions.assertArrayEquals(pick.toBitmask(), pick2.toBitmask());
    }
    @Test
    public void returns_raw_data_from_pick() {
        var pick = Pick.of(1, 14, 25, 64, 90);
        Assertions.assertArrayEquals(new int[] {1,14,25,64,90}, pick.getRawData());
    }
}
