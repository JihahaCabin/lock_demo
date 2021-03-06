package com.haha;

import com.haha.entity.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class SampleTest {

    static class StockThread implements Runnable{
        public void run() {
            //调用减少库存的方法
            boolean b = new Stock().reduceStock();

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

        Thread.sleep(3000);
    }
}