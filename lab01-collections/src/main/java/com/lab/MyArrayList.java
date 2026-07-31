package com.lab;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * 手写简易 ArrayList
 * <p>
 * 【核心数据结构】
 * - Object[] elementData: 存元素的数组
 * - int size: 当前实际元素个数（注意不是数组长度！）
 * <p>
 * 【关键设计】
 * - 默认容量 10
 * - 扩容倍数 1.5（oldCapacity + oldCapacity >> 1）
 * - 中间插入/删除用 System.arraycopy 搬移元素
 * - 删除后末尾置 null，帮助 GC 回收
 *
 * @param <E> 元素类型
 */
public class MyArrayList<E> {

    // ============ 常量 ============

    /** 默认初始容量 */
    private static final int DEFAULT_CAPACITY = 10;

    /** 空数组实例（JDK 源码里也是这么写的，避免创建多个空数组） */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    // ============ 字段 ============

    /** 存元素的数组 */
    private Object[] elementData;

    /** 当前元素个数（注意：不是数组长度！） */
    private int size;

    // ============ 构造方法 ============

    public MyArrayList() {
        this.elementData = new Object[DEFAULT_CAPACITY];
    }

    public MyArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("容量不能为负数: " + initialCapacity);
        }
    }

    // ============ 核心方法 ============

    /**
     * 尾部追加元素。
     * <p>
     * 【流程】
     * 1. 检查容量：size+1 > 数组长度？→ 扩容
     * 2. elementData[size] = e
     * 3. size++
     * <p>
     * 【时间复杂度】均摊 O(1)（偶尔扩容，大部分时候直接追加）
     */
    public boolean add(E e) {
        ensureCapacity(size + 1);       // ① 确保能装下
        elementData[size++] = e;        // ② 放到末尾，size 自增
        return true;
    }

    /**
     * 在指定位置插入元素。
     * <p>
     * 【流程】
     * 1. 检查 index 是否越界
     * 2. 检查容量
     * 3. 把 [index, size-1] 的元素整体后移一位 → System.arraycopy
     * 4. 把新元素放到 index 位置
     * 5. size++
     * <p>
     * 【时间复杂度】O(n) —— 最坏情况 index=0，后面元素全要搬
     *
     * @param index 插入位置
     * @param e     要插入的元素
     */
    public void add(int index, E e) {
        checkIndexForAdd(index);                           // ① 越界检查
        ensureCapacity(size + 1);                          // ② 确保容量
        // ③ 把 [index, size-1] 整体后移一位
        System.arraycopy(elementData, index,
                         elementData, index + 1,
                         size - index);
        elementData[index] = e;                            // ④ 放入新元素
        size++;                                            // ⑤ 计数+1
    }

    /**
     * 获取指定位置的元素。
     * <p>
     * 【时间复杂度】O(1) —— 数组随机访问
     */
    @SuppressWarnings("unchecked")
    public E get(int index) {
        checkIndex(index);
        return (E) elementData[index];
    }

    /**
     * 设置指定位置的元素（返回旧值）。
     */
    @SuppressWarnings("unchecked")
    public E set(int index, E e) {
        checkIndex(index);
        E oldValue = (E) elementData[index];
        elementData[index] = e;
        return oldValue;
    }

    /**
     * 删除指定位置的元素。
     * <p>
     * 【流程】
     * 1. 检查 index 越界
     * 2. 取出旧值
     * 3. 把 [index+1, size-1] 整体前移一位
     * 4. elementData[--size] = null  ← 注意这两步！
     * <p>
     * 【为什么 --size 后要置 null？】
     * 数组仍然持有那个位置的引用，GC 不会回收它 → 内存泄漏。
     * 置 null 后 GC 可以回收。
     * <p>
     * 【时间复杂度】O(n)
     */
    @SuppressWarnings("unchecked")
    public E remove(int index) {
        checkIndex(index);                                 // ① 越界检查
        E oldValue = (E) elementData[index];               // ② 保存旧值
        int moveNum = size - index - 1;                    // ③ 需要搬移的元素个数
        if (moveNum > 0) {
            System.arraycopy(elementData, index + 1,       // ④ 整体前移
                             elementData, index,
                             moveNum);
        }
        elementData[--size] = null;                        // ⑤ 末尾置 null → 帮助 GC
        return oldValue;
    }

    /**
     * 删除第一个匹配的元素（按 equals 判断）。
     *
     * @return true 删除成功，false 没找到
     */
    public boolean remove(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (elementData[i] == null) {
                    remove(i);
                    return true;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(elementData[i])) {
                    remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 查找元素位置（找不到返回 -1）。
     */
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (elementData[i] == null) return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(elementData[i])) return i;
            }
        }
        return -1;
    }

    /**
     * 是否包含某个元素。
     */
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    // ============ 容量相关 ============

    /**
     * 确保数组能装下 minCapacity 个元素。
     * 装不下就扩容为原来的 1.5 倍。
     * <p>
     * 【为什么是 1.5 倍而不是 2 倍？—— 面试高频题】
     * 1.5 倍扩容后，之前几次扩容丢弃的内存块大小加起来，
     * 比当前扩容的大小还要大，这些释放的内存更有可能被复用。
     * <p>
     * 举例（初始 10）：
     * 1.5 倍：10 → 15 → 22 → 33 → 49 → ...
     * 2 倍：  10 → 20 → 40 → 80 → 160 → ...
     * 2 倍时每次新数组大小 = 前面所有旧数组之和，旧内存块永远不够装新数组。
     * <p>
     * 【JDK 源码中用到了 MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8】
     * 减 8 是因为某些 JVM 实现在数组头存了一些元数据，这里简化处理。
     */
    private void ensureCapacity(int minCapacity) {
        if (minCapacity > elementData.length) {
            // 扩容：旧容量 + 旧容量/2 = 1.5 倍
            int oldCapacity = elementData.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);  // >> 1 = 除以 2

            // 如果 1.5 倍还不够（比如一次 addAll 了很多元素），直接用 minCapacity
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }

            elementData = Arrays.copyOf(elementData, newCapacity);
        }
    }

    /**
     * 把容量缩减到当前元素个数（节约内存）。
     */
    public void trimToSize() {
        if (size < elementData.length) {
            elementData = (size == 0)
                ? EMPTY_ELEMENTDATA
                : Arrays.copyOf(elementData, size);
        }
    }

    // ============ 工具方法 ============

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            elementData[i] = null;  // 帮助 GC
        }
        size = 0;
    }

    // ============ 边界检查 ============

    /** 检查访问索引是否合法 [0, size) */
    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                "索引 " + index + " 越界，当前 size = " + size);
        }
    }

    /** 检查插入索引是否合法 [0, size]（插入允许等于 size，即尾部追加） */
    private void checkIndexForAdd(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(
                "插入索引 " + index + " 越界，当前 size = " + size + "，允许范围 [0, " + size + "]");
        }
    }

    // ============ 方便调试 ============

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (int i = 0; i < size; i++) {
            sj.add(String.valueOf(elementData[i]));
        }
        return sj.toString();
    }

    /**
     * 调试用：查看内部数组状态（含 null 槽位）。
     * 输出格式：内部数组(长度=10, size=3): [A, B, C, null, null, null, null, null, null, null]
     */
    public String debugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("内部数组(长度=").append(elementData.length)
          .append(", size=").append(size).append("): ");
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (int i = 0; i < elementData.length; i++) {
            sj.add(String.valueOf(elementData[i]));
        }
        sb.append(sj);
        return sb.toString();
    }

    // ============ main 方法：快速验证 ============

    public static void main(String[] args) {
        MyArrayList<String> list = new MyArrayList<>();

        // ① 测试尾部追加
        list.add("A");
        list.add("B");
        list.add("C");
        System.out.println("① 尾部追加 3 个元素: " + list);

        // ② 测试中间插入
        list.add(1, "X");  // 在 B 前面插入 X
        System.out.println("② 在索引1插入'X': " + list);
        // 预期：[A, X, B, C]

        // ③ 测试获取
        System.out.println("③ get(2) = " + list.get(2));
        // 预期：B

        // ④ 测试删除
        list.remove(1);  // 删掉 X
        System.out.println("④ 删除索引1: " + list);
        // 预期：[A, B, C]

        // ⑤ 测试扩容
        System.out.println("⑤ 扩容测试——插入 20 个元素:");
        MyArrayList<Integer> nums = new MyArrayList<>(3); // 初始容量只给 3
        for (int i = 1; i <= 20; i++) {
            nums.add(i);
            if (i == 3 || i == 4 || i == 5) {
                System.out.println("   插入 " + i + " 个元素后 → " + nums.debugInfo());
            }
        }
        System.out.println("   最终: " + nums.debugInfo());

        // ⑥ 测试边界检查
        System.out.println("⑥ 边界检查:");
        try {
            list.get(999);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("   捕获异常: " + e.getMessage());
        }
    }
}
