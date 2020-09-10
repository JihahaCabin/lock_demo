package com.haha.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.haha.entity.LockRecord;
import com.haha.mapper.LockRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@Service
public class DbLock implements Lock {

    private static final String LOCK_NAME = "db_lock";

    @Autowired
    private LockRecordMapper lockRecordMapper;

    /**
     * 上锁
     */
    public synchronized void lock() {
        while (true){
            boolean b = tryLock();
            if(b){
                //添加记录
                LockRecord lockRecord = new LockRecord();
                lockRecord.setLockName(LOCK_NAME);
                lockRecordMapper.insert(lockRecord);
                return;
            }else{
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("等待中");
            }
        }
    }

    /**
     * 尝试获取锁，根据指定的名称，在数据库表中发起查询
     * @return
     */
    public boolean tryLock() {

        QueryWrapper<LockRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("lock_name",LOCK_NAME);
        List<LockRecord> lockRecords = lockRecordMapper.selectList(queryWrapper);

        if(lockRecords.size()==0){
            return true;
        }

        return false;
    }

    /**
     * 解锁 删除指定名称的记录
     */
    public void unlock() {
        QueryWrapper<LockRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("lock_name",LOCK_NAME);
        lockRecordMapper.delete(queryWrapper);
    }



    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }


    public void lockInterruptibly() throws InterruptedException {

    }

    public Condition newCondition() {
        return null;
    }
}
