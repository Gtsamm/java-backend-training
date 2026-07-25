package com.lab;

/**
 * 线程创建方式实验 —— 对比 Thread / Runnable / Callable
 *
 * <p>你的任务：
 * <ol>
 *   <li>先运行看效果，观察三种方式的区别</li>
 *   <li><b>找坑：</b>为什么有个线程的 run() 方法没在新线程里执行？看日志输出</li>
 *   <li>TODO: 补全 Callable 的实现 —— 让它返回 "任务完成时间戳"</li>
 * </ol>
 */
public class ThreadCreateLab {

    // ==================== 方式 1：继承 Thread（已写好） ====================
    static class MyThread extends Thread {
        MyThread(String name) { super(name); }

        @Override
        public void run() {
            System.out.println("[" + getName() + "] 执行中... (Thread 方式)");
        }
    }

    // ==================== 方式 2：实现 Runnable（已写好） ====================
    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println("[" + Thread.currentThread().getName() + "] 执行中... (Runnable 方式)");
        }
    }

    // ==================== 方式 3：TODO 你来写 ====================
    // 提示：实现 Callable<String>，在 call() 里返回 "任务完成时间戳: " + System.currentTimeMillis()
    // 然后用 FutureTask 包装，交给 Thread 执行，最后 futureTask.get() 拿结果

    public static void main(String[] args) throws Exception {
        System.out.println("===== 三种线程创建方式 =====\n");

        // --- 方式 1 ---
        Thread t1 = new MyThread("Thread-继承");
        t1.start();

        // --- 方式 2 ---
        Thread t2 = new Thread(new MyRunnable(), "Thread-Runnable");
        t2.start();

        // ⚠️ 找坑：下面这行有什么问题？观察输出
        Thread t3 = new Thread(() -> {
            System.out.println("[" + Thread.currentThread().getName() + "] Lambda 方式");
        });
        t3.start(); // ← 这是故意的，看看和 start() 有什么区别

        // --- 方式 3：TODO 你来写 ---
        // 1. 创建 Callable 实例（可以 Lambda）
        // 2. 用 FutureTask 包装
        // 3. 创建 Thread 并 start
        // 4. 用 futureTask.get() 获取返回值并打印

        // TODO: 你的代码写在这里
        java.util.concurrent.Callable<String> callable = () -> {
            return "任务完成时间戳: " + System.currentTimeMillis();
        };

        java.util.concurrent.FutureTask<String> futureTask = new java.util.concurrent.FutureTask<>(callable);

        Thread t4 = new Thread(futureTask, "Thread-Callable");
        t4.start();

        String result = futureTask.get();
        System.out.println("[" + t4.getName() + "] " + result);

        // ---- 等待前面的线程结束 ----
        t1.join();
        t2.join();

        System.out.println("\n===== 实验结束 =====");
    }
}
