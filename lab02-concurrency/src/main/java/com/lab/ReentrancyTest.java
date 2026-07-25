package com.lab;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 可重入性验证 —— synchronized vs ReentrantLock
 *
 * <p>什么是可重入？同一个线程在持有锁的情况下，再次获取<b>同一个锁</b>不会被阻塞。
 *
 * <p>你的任务：
 * <ol>
 *   <li>运行实验 1，验证 synchronized 是可重入的</li>
 *   <li><b>TODO: 补全实验 2</b> —— 用 ReentrantLock 验证可重入</li>
 *   <li><b>找坑：</b>ReentrantLock 的 unlock 有什么问题？锁了几次？</li>
 * </ol>
 */
public class ReentrancyTest {

    // ==================== 实验 1: synchronized 可重入（已写好，直接运行） ====================
    static class SyncReentrant {
        public synchronized void outer() {
            System.out.println("  → 进入 outer()，持有锁");
            inner(); // ← 同一个线程调用另一个 synchronized 方法
            System.out.println("  ← 离开 outer()");
        }

        public synchronized void inner() {
            System.out.println("    → 进入 inner()（再次获取同一个锁——如果可以重入就不会阻塞）");
            System.out.println("    ← 离开 inner()");
        }
    }

    // ==================== 实验 2: TODO 你来写 ====================
    // 用 ReentrantLock 实现和上面一样的效果
    // 提示：
    //   1. 创建 ReentrantLock 实例
    //   2. outer() 里 lock.lock() → 打印 holdCount → 调 inner() → unlock()
    //   3. inner() 里 lock.lock() → 打印 holdCount → unlock()
    //   ⚠️ 注意：lock() 和 unlock() 必须成对！lock 几次就要 unlock 几次
    static class LockReentrant {
        // TODO: 创建 ReentrantLock 实例
        private final ReentrantLock lock = new ReentrantLock();

        // TODO: 写 outer() 方法
    public void outer() {
      lock.lock();
      try {
          System.out.println("  → 进入 outer()，holdCount=" + lock.getHoldCount());
          inner();
          System.out.println("  ← 离开 outer()");
      } finally {
          lock.unlock();  // ← 无论如何都会执行
      }
  }

        // TODO: 写 inner() 方法
        public void inner() {
      lock.lock();
      try {
          System.out.println("    → 进入 inner()，holdCount=" + lock.getHoldCount());
          System.out.println("    ← 离开 inner()");
      } finally {
          lock.unlock();
      }
  }

    // ==================== main ====================
    public static void main(String[] args) throws Exception {
        // --- 实验 1: synchronized ---
        System.out.println("===== 实验 1: synchronized 可重入 =====\n");
        SyncReentrant sync = new SyncReentrant();
        sync.outer();
        System.out.println("✅ 如果能正常走完 → synchronized 是可重入的\n");

        // --- 实验 2: ReentrantLock ---
        System.out.println("===== 实验 2: ReentrantLock 可重入 =====\n");
        // TODO: 取消下面注释，运行你的实现
         LockReentrant lockReentrant = new LockReentrant();
         lockReentrant.outer();
         System.out.println("观察 holdCount 的变化\n");

        System.out.println("===== 实验结束 =====");
    }

    private static void sleep(long millis) {
        try { TimeUnit.MILLISECONDS.sleep(millis); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

}
