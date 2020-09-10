package com.haha;

import com.haha.entity.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootTest
public class ReentrantLockTest {

    private static Lock lock = new ReentrantLock();

    static class StockThread implements Runnable{

        public void run() {
            //减少库存前加锁
            lock.lock();
            //调用减少库存的方法
            boolean b = new Stock().reduceStock();
            //减少库存后释放锁
            lock.unlock();
            if (b) {
                System.out.println(Thread.currentThread().getName()+"减少库存成功");
            }else {
                System.out.println(Thread.currentThread().getName()+"减少库存失败");
            }
        }
    }

    @Test
    public void test() throws InterruptedException {
        new Thread(new StockThread(),"线程1").start();
        new Thread(new StockThread(),"线程2").start();

        Thread.sleep(4000);
    }

}
