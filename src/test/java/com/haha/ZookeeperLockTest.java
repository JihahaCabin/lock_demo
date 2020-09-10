package com.haha;

import com.haha.entity.Stock;
import com.haha.utils.ZkLock;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZookeeperLockTest {

    @Autowired
    private ZooKeeper zooKeeper;

    private static final String LOCK_NAME = "zk_lock";

    static class StockThread implements Runnable{

        private ZkLock zkLock;

        public StockThread(ZkLock zkLock){
            this.zkLock= zkLock;
        }

        public void run() {

            zkLock.lock();
            //调用减少库存的方法
            boolean b = new Stock().reduceStock();

            zkLock.unlock();
            if (b) {
                System.out.println(Thread.currentThread().getName()+"减少库存成功");
            }else {
                System.out.println(Thread.currentThread().getName()+"减少库存失败");
            }
        }
    }

    @Test
    public void test() throws InterruptedException {

        ZkLock zkLock = new ZkLock(zooKeeper,LOCK_NAME);

        new Thread(new ZookeeperLockTest.StockThread(zkLock),"线程1").start();
        new Thread(new ZookeeperLockTest.StockThread(zkLock),"线程2").start();

        Thread.sleep(4000);
    }
}
