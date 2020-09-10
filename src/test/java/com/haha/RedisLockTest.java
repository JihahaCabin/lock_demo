package com.haha;


import com.haha.entity.Stock;
import com.haha.utils.RedisLock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisLockTest {

    @Autowired
    private RedisLock redisLock;

    static class StockThread implements Runnable{

        private RedisLock redisLock;

        public StockThread(RedisLock redisLock){
            this.redisLock= redisLock;
        }

        public void run() {

            redisLock.lock();
            //调用减少库存的方法
            boolean b = new Stock().reduceStock();

            redisLock.unlock();
            if (b) {
                System.out.println(Thread.currentThread().getName()+"减少库存成功");
            }else {
                System.out.println(Thread.currentThread().getName()+"减少库存失败");
            }
        }
    }

    @Test
    public void test() throws InterruptedException {
        new Thread(new StockThread(redisLock),"线程1").start();
        new Thread(new StockThread(redisLock),"线程2").start();

        Thread.sleep(4000);
    }

}
