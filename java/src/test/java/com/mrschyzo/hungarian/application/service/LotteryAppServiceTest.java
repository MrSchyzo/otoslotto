package com.mrschyzo.hungarian.application.service;

import com.mrschyzo.hungarian.domain.Lottery;
import com.mrschyzo.hungarian.domain.LotteryLoader;
import com.mrschyzo.hungarian.domain.Pick;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class LotteryAppServiceTest {
    @Test
    public void load_service_command_returns_success_if_no_issue_happen() {
        LotteryLoader ok = Lottery::new;
        LotteryAppService service = new LotteryAppService(ok);
        Assertions.assertDoesNotThrow(service::load);
    }
    @Test
    public void load_service_throws_dedicated_exception_if_loading_fails() {
        LotteryLoader faulty = () -> {
            throw new RuntimeException();
        };
        LotteryAppService service = new LotteryAppService(faulty);
        Assertions.assertThrows(LotteryCannotBeLoadedException.class, service::load);
    }
    @Test
    public void query_service_command_returns_zeroed_histogram_if_it_was_not_loaded() {
        LotteryLoader ok = Lottery::new;
        LotteryAppService service = new LotteryAppService(ok);
        Map<Integer, Integer> matches = service.query(new int[]{1,2,3,4,5});
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
        LotteryLoader ok = Lottery::new;
        LotteryAppService service = new LotteryAppService(ok);
        service.load();
        Map<Integer, Integer> matches = service.query(new int[]{1,2,3,4});
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
        LotteryLoader ok = Lottery::new;
        LotteryAppService service = new LotteryAppService(ok);
        service.load();
        Map<Integer, Integer> matches = service.query(new int[]{1,2,2,3,4});
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
        LotteryLoader ok = Lottery::new;
        LotteryAppService service = new LotteryAppService(ok);
        service.load();
        Map<Integer, Integer> matches = service.query(new int[]{0,1,2,3,4});
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
            var lottery = new Lottery();
            lottery.acceptPick(Pick.of(1,2,3,4,5));
            lottery.acceptPick(Pick.of(2,1,3,4,5));
            lottery.acceptPick(Pick.of(1,2,6,4,5));
            lottery.acceptPick(Pick.of(7,3,6,4,5));
            return lottery;
        };
        LotteryAppService service = new LotteryAppService(ok);
        service.load();
        Map<Integer, Integer> matches = service.query(new int[]{1,2,3,4,5});
        var expected = Map.of(
                2, 0,
                3, 1,
                4, 1,
                5, 2
        );
        Assertions.assertEquals(expected, matches);
    }
}
