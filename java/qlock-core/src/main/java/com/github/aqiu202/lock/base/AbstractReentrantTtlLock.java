package com.github.aqiu202.lock.base;

import com.github.aqiu202.id.IdGenerator;
import com.github.aqiu202.lock.centralize.LocaleTtlLock;
import com.github.aqiu202.ttl.data.StringTtlCache;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.lang.Nullable;

/**
 * <pre>AbstractReentrantLock</pre>
 *
 * @author aqiu 2020/12/2 15:57
 **/
public abstract class AbstractReentrantTtlLock extends LocaleTtlLock {

    protected final IdGenerator<?> idGenerator;

//    public AbstractReentrantTtlLock(StringTtlCache cacheable) {
//        this(cacheable, new SimpleUUIDGenerator());
//    }

    public AbstractReentrantTtlLock(IdGenerator<?> idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public Boolean release(String key, long expired, TimeUnit timeUnit) {
        final String value = this.cache.get(key);
        if (Objects.equals(value, LockValueHolder.getValue())) {
            return this.doRelease(key, expired, timeUnit);
        }
        return false;
    }

    @Override
    public Boolean release(String key) {
        final String value = this.cache.get(key);
        if (Objects.equals(value, LockValueHolder.getValue())) {
            return this.doRelease(key);
        }
        return false;
    }

    @Override
    public Boolean acquire(String key, long expired, TimeUnit timeUnit) {
        String value = String.valueOf(this.idGenerator.nextId());
        LockValueHolder.setValue(value);
        final Boolean result = this.doAcquire(key, value, expired, timeUnit);
        if (result != null && !result) {
            return Objects.equals(this.cache.get(key), value);
        }
        return result;
    }

    @Override
    public Boolean acquire(String key) {
        String value = String.valueOf(this.idGenerator.nextId());
        LockValueHolder.setValue(value);
        final Boolean result = this.doAcquire(key, value);
        if (result != null && !result) {
            return Objects.equals(this.cache.get(key), value);
        }
        return result;
    }

    @Nullable
    public Boolean doAcquire(String key, String value) {
        return this.cache.setIfAbsent(key, value);
    }

    @Nullable
    public Boolean doAcquire(String key, String value, long expired, TimeUnit timeUnit) {
        return this.cache.setIfAbsent(key, value, expired, timeUnit);
    }

    @Nullable
    public Boolean doRelease(String key) {
        return super.acquire(key);
    }

    @Nullable
    public Boolean doRelease(String key, long expired, TimeUnit timeUnit) {
        return super.acquire(key, expired, timeUnit);
    }
}