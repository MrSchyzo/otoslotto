package com.mrschyzo.hungarian.application.service;

import com.mrschyzo.hungarian.domain.Lottery;
import com.mrschyzo.hungarian.domain.LotteryLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
