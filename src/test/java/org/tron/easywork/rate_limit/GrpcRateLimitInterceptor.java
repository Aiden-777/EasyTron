package org.tron.easywork.rate_limit;

import com.google.common.util.concurrent.RateLimiter;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class GrpcRateLimitInterceptor implements ClientInterceptor {
    private final RateLimiter rateLimiter;
    private final ReentrantLock lock;
    private final Condition condition;
    private final int maxRequests;
    private final int periodSeconds;
    private int currentRequests;

    public GrpcRateLimitInterceptor(int maxRequests, int periodSeconds) {
        log.warn("创建");
        this.maxRequests = maxRequests;
        this.periodSeconds = periodSeconds;
        this.currentRequests = 0;
        this.rateLimiter = RateLimiter.create(maxRequests / (double) periodSeconds);
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions, Channel next) {
        ClientCall<ReqT, RespT> call;
        try {
            lock.lock();
            while (currentRequests >= maxRequests) {
                condition.await();
            }
            currentRequests++;
            rateLimiter.acquire();

            call = next.newCall(method, callOptions);

            call = new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                        @Override
                        public void onClose(Status status, Metadata trailers) {
                            lock.lock();
                            currentRequests--;
                            condition.signal();
                            lock.unlock();
                            super.onClose(status, trailers);
                        }
                    }, headers);
                }
            };
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return call;
    }
}