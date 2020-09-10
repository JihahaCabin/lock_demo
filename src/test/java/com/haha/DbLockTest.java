package com.haha;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.haha.entity.LockRecord;
import com.haha.entity.Stock;
import com.haha.mapper.LockRecordMapper;
import com.haha.utils.DbLock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class DbLockTest {

    @Autowired
    private DbLock dbLock;

    static class StockThread implements Runnable{

        private DbLock dbLock;

        public StockThread(DbLock dbLock){
            this.dbLock = dbLock;
        }

        public void run() {

            dbLock.lock();
            //调用减少库存的方法
            boolean b = new Stock().reduceStock();

            dbLock.unlock();
            if (b) {
                System.out.println(Thread.currentThread().getName()+"减少库存成功");
            }else {
                System.out.println(Thread.currentThread().getName()+"减少库存失败");
            }
        }
    }

    @Test
    public void test() throws InterruptedException {
        new Thread(new StockThread(this.dbLock),"线程1").start();
        new Thread(new StockThread(this.dbLock),"线程2").start();

        Thread.sleep(4000);
    }
}
