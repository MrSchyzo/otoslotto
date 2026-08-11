package com.mrschyzo.hungarian.application.service;

import com.mrschyzo.hungarian.domain.*;
import com.mrschyzo.hungarian.domain.pick.Pick;
import com.mrschyzo.hungarian.domain.pick.PickParser;
import com.mrschyzo.hungarian.domain.pick.SimplePickParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class LotteryAppServiceTest {
    private PickParser parser = new SimplePickParser();
    private LotteryLoader dummyLoader = BitmaskLottery::new;

    @Test
    public void load_service_command_returns_success_if_no_issue_happen() {
        LotteryAppService service = new LotteryAppService(dummyLoader, parser);
        Assertions.assertDoesNotThrow(service::load);
    }
    @Test
    public void load_service_throws_dedicated_exception_if_loading_fails() {
        LotteryLoader faulty = () -> {
            throw new RuntimeException();
        };
        LotteryAppService service = new LotteryAppService(faulty, parser);
        Assertions.assertThrows(LotteryCannotBeLoadedException.class, service::load);
    }
    @Test
    public void query_service_command_returns_zeroed_histogram_if_it_was_not_loaded() {
        LotteryAppService service = new LotteryAppService(dummyLoader, parser);
        Map<Integer, Integer> matches = service.query("1 2 3 4 5");
        var expected = Map.of(
                2, 0,
                3, 0,
                4, 0,
                5, 0
        );
        Assertions.assertEquals(expected, matches);
    }
    @Test
    public void query_service_command_returns_zeroed_histogram_if_winning_ticket_has_not_5_elements() throws ApplicationException {
        LotteryAppService service = new LotteryAppService(dummyLoader, parser);
        service.load();
        Map<Integer, Integer> matches = service.query("1 2 3 4");
        var expected = Map.of(
                2, 0,
                3, 0,
                4, 0,
                5, 0
        );
        Assertions.assertEquals(expected, matches);
    }
    @Test
    public void query_service_command_returns_zeroed_histogram_if_winning_ticket_has_duplicates() throws ApplicationException {
        LotteryAppService service = new LotteryAppService(dummyLoader, parser);
        service.load();
        Map<Integer, Integer> matches = service.query("1 2 2 3 4");
        var expected = Map.of(
                2, 0,
                3, 0,
                4, 0,
                5, 0
        );
        Assertions.assertEquals(expected, matches);
    }
    @Test
    public void query_service_command_returns_zeroed_histogram_if_winning_ticket_has_invalid_values() throws ApplicationException {
        LotteryAppService service = new LotteryAppService(dummyLoader, parser);
        service.load();
        Map<Integer, Integer> matches = service.query("0 1 2 3 4");
        var expected = Map.of(
                2, 0,
                3, 0,
                4, 0,
                5, 0
        );
        Assertions.assertEquals(expected, matches);
    }
    @Test
    public void query_service_command_returns_correct_histogram() throws ApplicationException {
        LotteryLoader ok = () -> {
            var lottery = new BitmaskLottery();
            lottery.acceptPick(Pick.of(1,2,3,4,5));
            lottery.acceptPick(Pick.of(2,1,3,4,5));
            lottery.acceptPick(Pick.of(1,2,6,4,5));
            lottery.acceptPick(Pick.of(7,3,6,4,5));
            return lottery;
        };
        LotteryAppService service = new LotteryAppService(ok, parser);
        service.load();
        Map<Integer, Integer> matches = service.query("1 2 3 4 5");
        var expected = Map.of(
                2, 0,
                3, 1,
                4, 1,
                5, 2
        );
        Assertions.assertEquals(expected, matches);
    }
}
