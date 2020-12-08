package com.github.aqiu202.limit.aop;

import com.github.aqiu202.aop.pointcut.AbstractKeyAnnotationInterceptor;
import com.github.aqiu202.aop.spel.EvaluationFiller;
import com.github.aqiu202.limit.anno.RepeatLimiting;
import com.github.aqiu202.lock.base.Lock;
import java.util.concurrent.TimeUnit;

/**
 * <pre>防重复提交-方法拦截</pre>
 *
 * @author aqiu 2020/11/30 0:01
 **/
public class RepeatMethodInterceptor extends AbstractKeyAnnotationInterceptor<RepeatLimiting> {

    private final Lock lock;

    public RepeatMethodInterceptor(Lock lock) {
        this.lock = lock;
    }

    public RepeatMethodInterceptor(Lock lock, EvaluationFiller evaluationFiller) {
        super(evaluationFiller);
        this.lock = lock;
    }

    @Override
    public String getKey(RepeatLimiting annotation) {
        return annotation.key();
    }

    @Override
    public String getKeyGeneratorName(RepeatLimiting annotation) {
        return annotation.keyGenerator();
    }

    @Override
    protected void beforeIntercept(RepeatLimiting repeatLimiting, String key) {
        long timeout = repeatLimiting.timeout();
        TimeUnit timeUnit = repeatLimiting.timeUnit();
        final Boolean getLock = this.lock.acquire(key, timeout, timeUnit);
        if (!getLock) {
            throw new IllegalArgumentException(repeatLimiting.message());
        }
    }

    @Override
    protected void afterIntercept(RepeatLimiting repeatLimiting, String key, Throwable throwable) {
        //只有等到过期或者出现异常才释放锁，不会主动释放锁
        if (throwable != null) {
            this.lock.release(key, repeatLimiting.timeout(), repeatLimiting.timeUnit());
        }
    }
}