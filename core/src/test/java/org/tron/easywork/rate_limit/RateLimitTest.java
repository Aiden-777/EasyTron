package org.tron.easywork.rate_limit;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.trident.proto.Response;

/**
 * @author Admin
 * @version 1.0
 * @time 2023-02-16 14:16
 */
@Slf4j
public class RateLimitTest {

    @Test
    public void test() throws InterruptedException {
        String key = "151b073fe0a76e0eb4e57b9a1cba94abd5cffb46202cb0cf6cf8b0b6296fc7ef";
        LimitApiWrapper wrapper = LimitApiWrapper.ofShasta(key);

        for (int i = 0; i < 50; i++) {
            final int x = i;
            new Thread(() -> {
                Response.Account account = wrapper.getAccount("TTTTT4YrkRB5kc3SxEReBLiBDa89B1oGda");
                log.debug("{}", x);
            }).start();
        }

        Thread.sleep(60000);
    }

    @Test
    public void testx() throws InterruptedException {
        String key = "151b073fe0a76e0eb4e57b9a1cba94abd5cffb46202cb0cf6cf8b0b6296fc7ef";
        LimitApiWrapper wrapper = LimitApiWrapper.ofShasta(key);

        for (int i = 0; i < 10; i++) {
            Response.Account account = wrapper.getAccount("TTTTT4YrkRB5kc3SxEReBLiBDa89B1oGda");
            long balance = account.getBalance();
            log.debug("{}", balance);
        }
    }
}
