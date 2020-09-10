package com.haha;


import com.haha.entity.Stock;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedissionLockTest {

    @Autowired
    private Redisson redisson;

    static class StockThread implements Runnable{

        private RLock mylock;

        public StockThread(RLock lock){
            this.mylock = lock;
        }

        public void run() {

            mylock.lock();
            //调用减少库存的方法
            boolean b = new Stock().reduceStock();

            mylock.unlock();
            if (b) {
                System.out.println(Thread.currentThread().getName()+"减少库存成功");
            }else {
                System.out.println(Thread.currentThread().getName()+"减少库存失败");
            }
        }
    }

    @Test
    public void test() throws InterruptedException {

        RLock lock = redisson.getLock("redisson_lock");

        new Thread(new StockThread(lock),"线程1").start();
        new Thread(new StockThread(lock),"线程2").start();

        Thread.sleep(4000);

    }
}
