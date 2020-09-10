package com.haha.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


@Component
public class RedisLock implements Lock {

    private static final String LOCK_NAME = "redis_lock";

    @Autowired
    private RedisTemplate redisTemplate;


    public void lock() {
        while (true){
            Boolean b = redisTemplate.opsForValue().setIfAbsent("lockName", LOCK_NAME,10,TimeUnit.SECONDS);

            if (b) {
                return;
            }else{
                System.out.println("循环等待中");
            }
        }
    }

    public void lockInterruptibly() throws InterruptedException {

    }

    public boolean tryLock() {
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    public void unlock() {
        redisTemplate.delete("lockName");
    }

    public Condition newCondition() {
        return null;
    }
}
