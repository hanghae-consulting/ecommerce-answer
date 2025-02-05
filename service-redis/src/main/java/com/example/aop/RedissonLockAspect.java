package com.example.aop;


import com.example.annotations.RedissonLock;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RedissonLockAspect {

    private static final Logger log = LoggerFactory.getLogger(RedissonLockAspect.class);
    private final RedissonClient redissonClient;

    @Around("@annotation(com.example.annotations.RedissonLock)")
    public Object redissonLock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedissonLock annotation = method.getAnnotation(RedissonLock.class);
        String lockKey = annotation.value();

        RLock lock = redissonClient.getLock(lockKey);

        boolean lockable = false;

        try{
            lockable = lock.tryLock(annotation.waitTime(), annotation.leaseTime(), TimeUnit.MILLISECONDS);
            log.info("name: {}, locked: {}, lockable: {}", lock.getName(), lock.isLocked(), lockable);
            if (!lockable){
                throw new IllegalStateException("Could not acquire lock for key: " + lockKey);
            }
            log.info("락 휙득 성공: {}", lockKey);

            if (TransactionSynchronizationManager.isActualTransactionActive()){
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                            log.info("트랜잭션 종료 후 락 해제: {}", lockKey);
                        }
                    }
                });
            }

            return joinPoint.proceed();
        } catch (IllegalStateException e){
            log.info("락 휙득 실패: {}", lockKey);
            throw e;
        } finally {
            if (!TransactionSynchronizationManager.isSynchronizationActive() && lockable){
                lock.unlock();
                log.info("트랜잭션 외부에서 락 해제: {}", lockKey);
            }
        }
    }
}
