package com.lab;

import java.util.*;

/**
 * Week 1 算法练习 —— LeetCode Hot 100 前 10 题
 *
 * <p>学习方式：核心代码已注释，请手动敲一遍（不要复制粘贴）</p>
 *
 * <p>覆盖算法类型：
 *   哈希表 / 双指针 / 滑动窗口 / 链表 / 栈 / DFS / 回溯 / 动态规划(中心扩散)
 * </p>
 */
public class Week01Solutions {

    // ============================================================
    // 第 1 题：两数之和 (Two Sum) — 哈希表
    // LeetCode 1: https://leetcode.cn/problems/two-sum/
    // 技巧：HashMap 存 值→索引，一次遍历找 complement
    // 时间 O(n)  空间 O(n)
    // ============================================================

    /**
     * 给定数组 nums 和目标 target，返回两数之和为 target 的两个下标。
     */
    public int[] twoSum(int[] nums, int target) {
        // TODO 第 1 步：创建 HashMap<Integer, Integer>，key=数值, value=下标

        // TODO 第 2 步：遍历数组 nums
        //    计算 complement = target - nums[i]
        //    如果 map 包含 complement → 返回 new int[]{map.get(complement), i}
        //    否则 → map.put(nums[i], i)

        // TODO 第 3 步：最后 return new int[0]（理论上不会走到这里）

        /*
        // ====== 参考答案（写完后对照）======
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (map.containsKey(complement)) {
                return new int[]{map.get(complement), i};
            }
            map.put(nums[i], i);
        }
        return new int[0];
        */
        throw new UnsupportedOperationException("请手动实现 twoSum");
    }


    // ============================================================
    // 第 2 题：三数之和 (3Sum) — 排序 + 双指针
    // LeetCode 15: https://leetcode.cn/problems/3sum/
    // 技巧：排序 → 固定 i → left/right 双指针夹逼 → 去重是关键
    // 时间 O(n²)  空间 O(1)（排序栈不计）
    // ============================================================

    /**
     * 找出所有和为 0 且不重复的三元组。
     */
    public List<List<Integer>> threeSum(int[] nums) {
        // TODO 第 1 步：Arrays.sort(nums)

        // TODO 第 2 步：遍历 i = 0..n-3
        //    如果 nums[i] > 0 → break（后面都 > 0，不可能和为 0）
        //    如果 i > 0 且 nums[i] == nums[i-1] → continue（去重）

        // TODO 第 3 步：双指针 left = i+1, right = n-1
        //    while (left < right):
        //      sum = nums[i] + nums[left] + nums[right]
        //      sum == 0 → 加入结果 → left++ right-- → 跳过重复
        //      sum < 0  → left++
        //      sum > 0  → right--

        /*
        // ====== 参考答案 ======
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums);
        int n = nums.length;

        for (int i = 0; i < n - 2; i++) {
            if (nums[i] > 0) break;
            if (i > 0 && nums[i] == nums[i - 1]) continue;

            int left = i + 1, right = n - 1;
            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];
                if (sum == 0) {
                    result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                    while (left < right && nums[left] == nums[left + 1]) left++;
                    while (left < right && nums[right] == nums[right - 1]) right--;
                    left++;
                    right--;
                } else if (sum < 0) {
                    left++;
                } else {
                    right--;
                }
            }
        }
        return result;
        */
        throw new UnsupportedOperationException("请手动实现 threeSum");
    }


    // ============================================================
    // 第 3 题：无重复字符的最长子串 (Longest Substring) — 滑动窗口
    // LeetCode 3: https://leetcode.cn/problems/longest-substring-without-repeating-characters/
    // 技巧：HashMap 记字符最后出现位置，窗口收缩到重复字符之后
    // 时间 O(n)  空间 O(字符集大小)
    // ============================================================

    /**
     * 返回无重复字符的最长子串长度。
     */
    public int lengthOfLongestSubstring(String s) {
        // TODO 第 1 步：创建 HashMap<Character, Integer> 存字符最后出现位置
        //             创建 maxLen = 0, left = 0

        // TODO 第 2 步：遍历 right = 0..s.length()-1
        //    如果 map 包含当前字符 → left = max(left, map.get(c) + 1)
        //    map.put(c, right)
        //    maxLen = max(maxLen, right - left + 1)

        // TODO 第 3 步：return maxLen

        /*
        // ====== 参考答案 ======
        Map<Character, Integer> map = new HashMap<>();
        int maxLen = 0;
        for (int left = 0, right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (map.containsKey(c)) {
                left = Math.max(left, map.get(c) + 1);
            }
            map.put(c, right);
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
        */
        throw new UnsupportedOperationException("请手动实现 lengthOfLongestSubstring");
    }


    // ============================================================
    // 第 4 题：反转链表 (Reverse Linked List) — 链表
    // LeetCode 206: https://leetcode.cn/problems/reverse-linked-list/
    // 技巧：三指针 prev/curr/next 迭代反转
    // 时间 O(n)  空间 O(1)
    // ============================================================

    static class ListNode {
        int val;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
        ListNode(int val, ListNode next) { this.val = val; this.next = next; }
    }

    /**
     * 反转单链表（迭代法）。
     * 画图：prev(null) → curr(1) → next(2) → ...
     *            ← ①反转    ②prev前移  ③curr前移
     */
    public ListNode reverseList(ListNode head) {
        // TODO 第 1 步：prev = null, curr = head

        // TODO 第 2 步：while (curr != null):
        //    next = curr.next   // ① 先记住下一个节点
        //    curr.next = prev   // ② 反转：当前节点指向前一个
        //    prev = curr        // ③ 前移 prev
        //    curr = next        // ④ 前移 curr

        // TODO 第 3 步：return prev  （prev 就是新的头节点）

        /*
        // ====== 参考答案（迭代）======
        ListNode prev = null;
        ListNode curr = head;
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
        */

        // 附加题：递归写法
        /*
        // ====== 参考答案（递归）======
        if (head == null || head.next == null) return head;
        ListNode newHead = reverseList(head.next);
        head.next.next = head;
        head.next = null;
        return newHead;
        */
        throw new UnsupportedOperationException("请手动实现 reverseList");
    }


    // ============================================================
    // 第 5 题：有效的括号 (Valid Parentheses) — 栈
    // LeetCode 20: https://leetcode.cn/problems/valid-parentheses/
    // 技巧：左括号入栈，右括号匹配栈顶
    // 时间 O(n)  空间 O(n)
    // ============================================================

    /**
     * 判断括号字符串是否有效。
     */
    public boolean isValid(String s) {
        // TODO 第 1 步：创建 Stack<Character>

        // TODO 第 2 步：遍历字符串每个字符
        //    如果是左括号 ( [ { → 入栈
        //    如果是右括号:
        //      如果栈为空 → return false（没左括号匹配）
        //      弹出栈顶，检查是否匹配（ '('配')'  '['配']'  '{'配'}' ）
        //      不匹配 → return false

        // TODO 第 3 步：return stack.isEmpty()
        //    （如果栈非空，说明有未闭合的左括号）

        /*
        // ====== 参考答案 ======
        Stack<Character> stack = new Stack<>();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else {
                if (stack.isEmpty()) return false;
                char top = stack.pop();
                if (c == ')' && top != '(') return false;
                if (c == ']' && top != '[') return false;
                if (c == '}' && top != '{') return false;
            }
        }
        return stack.isEmpty();
        */
        throw new UnsupportedOperationException("请手动实现 isValid");
    }


    // ============================================================
    // 第 6 题：合并两个有序链表 (Merge Two Sorted Lists) — 链表
    // LeetCode 21: https://leetcode.cn/problems/merge-two-sorted-lists/
    // 技巧：哨兵(dummy)节点 + 比较插入
    // 时间 O(m+n)  空间 O(1)
    // ============================================================

    /**
     * 合并两个升序链表为一个升序链表。
     */
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        // TODO 第 1 步：创建 dummy = new ListNode(-1), curr = dummy
        //    （哨兵节点的作用：统一处理逻辑，不用判断头节点是否为空）

        // TODO 第 2 步：while (list1 != null && list2 != null):
        //    比较 list1.val 和 list2.val，谁小接谁
        //    接完后对应链表后移，curr 后移

        // TODO 第 3 步：curr.next = (list1 != null) ? list1 : list2
        //    （把剩余部分接上）

        // TODO 第 4 步：return dummy.next

        /*
        // ====== 参考答案 ======
        ListNode dummy = new ListNode(-1);
        ListNode curr = dummy;

        while (list1 != null && list2 != null) {
            if (list1.val <= list2.val) {
                curr.next = list1;
                list1 = list1.next;
            } else {
                curr.next = list2;
                list2 = list2.next;
            }
            curr = curr.next;
        }

        curr.next = (list1 != null) ? list1 : list2;
        return dummy.next;
        */
        throw new UnsupportedOperationException("请手动实现 mergeTwoLists");
    }


    // ============================================================
    // 第 7 题：二叉树的中序遍历 (Binary Tree Inorder Traversal) — 树
    // LeetCode 94: https://leetcode.cn/problems/binary-tree-inorder-traversal/
    // 技巧：递归（简单）+ 迭代用栈（面试常考）
    // 时间 O(n)  空间 O(h) h=树高
    // ============================================================

    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode() {}
        TreeNode(int val) { this.val = val; }
        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * 中序遍历（左→根→右）—— 递归版。
     */
    public List<Integer> inorderTraversal(TreeNode root) {
        // TODO 递归版：
        //    创建 result 列表
        //    写一个 helper(TreeNode node) 辅助方法:
        //      if (node == null) return;
        //      helper(node.left);   // 先左
        //      result.add(node.val); // 再根
        //      helper(node.right);  // 最后右
        //    helper(root);
        //    return result;
        //
        // TODO 迭代版（用栈，面试常考）：
        //    Stack<TreeNode> stack = new Stack<>();
        //    TreeNode curr = root;
        //    while (curr != null || !stack.isEmpty()):
        //      // 一直往左走到底
        //      while (curr != null): stack.push(curr); curr = curr.left;
        //      // 弹出栈顶（最左节点）
        //      curr = stack.pop();
        //      result.add(curr.val);
        //      // 转向右子树
        //      curr = curr.right;

        /*
        // ====== 参考答案（递归）======
        List<Integer> result = new ArrayList<>();
        inorder(root, result);
        return result;
        */

        /*
        // ====== 参考答案（迭代）======
        List<Integer> result = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        TreeNode curr = root;
        while (curr != null || !stack.isEmpty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.left;
            }
            curr = stack.pop();
            result.add(curr.val);
            curr = curr.right;
        }
        return result;
        */
        throw new UnsupportedOperationException("请手动实现 inorderTraversal");
    }

    /** 递归辅助方法 */
    private void inorder(TreeNode node, List<Integer> result) {
        // TODO: if (node == null) return;
        //       inorder(node.left, result);
        //       result.add(node.val);
        //       inorder(node.right, result);
        throw new UnsupportedOperationException("请手动实现 inorder 辅助方法");
    }


    // ============================================================
    // 第 8 题：岛屿数量 (Number of Islands) — DFS 淹没
    // LeetCode 200: https://leetcode.cn/problems/number-of-islands/
    // 技巧：遍历网格 → 遇到 '1' count++ → DFS 淹掉整座岛（1→0）
    // 时间 O(m×n)  空间 O(m×n)（递归栈最坏情况）
    // ============================================================

    /**
     * 返回二维网格中岛屿的数量。
     */
    public int numIslands(char[][] grid) {
        // TODO 第 1 步：边界判断（grid == null || grid.length == 0）

        // TODO 第 2 步：count = 0
        //    双层循环遍历 grid:
        //      如果 grid[i][j] == '1':
        //        count++
        //        dfs(grid, i, j)  // 淹掉整座岛

        // TODO 第 3 步：return count

        /*
        // ====== 参考答案 ======
        if (grid == null || grid.length == 0) return 0;
        int count = 0;
        int rows = grid.length, cols = grid[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == '1') {
                    count++;
                    dfs(grid, i, j);
                }
            }
        }
        return count;
        */
        throw new UnsupportedOperationException("请手动实现 numIslands");
    }

    /**
     * DFS 淹没：把 (i,j) 及其上下左右相连的 '1' 全部变为 '0'
     */
    private void dfs(char[][] grid, int i, int j) {
        // TODO 边界检查 + 终止条件:
        //    i < 0 || i >= rows || j < 0 || j >= cols → return
        //    grid[i][j] == '0' → return（已经是水）

        // TODO 淹掉当前格子:
        //    grid[i][j] = '0'
        //    dfs 四个方向: (i-1,j), (i+1,j), (i,j-1), (i,j+1)

        /*
        // ====== 参考答案 ======
        if (i < 0 || i >= grid.length || j < 0 || j >= grid[0].length) return;
        if (grid[i][j] == '0') return;

        grid[i][j] = '0';

        dfs(grid, i - 1, j); // 上
        dfs(grid, i + 1, j); // 下
        dfs(grid, i, j - 1); // 左
        dfs(grid, i, j + 1); // 右
        */
        throw new UnsupportedOperationException("请手动实现 dfs 淹没");
    }


    // ============================================================
    // 第 9 题：全排列 (Permutations) — 回溯
    // LeetCode 46: https://leetcode.cn/problems/permutations/
    // 技巧：回溯模板 for→choose→backtrack→unchoose
    // 时间 O(n×n!)  空间 O(n)
    // ============================================================

    /**
     * 返回数组的所有排列（无重复元素）。
     */
    public List<List<Integer>> permute(int[] nums) {
        // TODO 第 1 步：创建 result 列表

        // TODO 第 2 步：backtrack(nums, 路径, 已使用标记数组, result)

        // TODO 第 3 步：return result

        /*
        // ====== 参考答案 ======
        List<List<Integer>> result = new ArrayList<>();
        boolean[] used = new boolean[nums.length];
        backtrack(nums, new ArrayList<>(), used, result);
        return result;
        */
        throw new UnsupportedOperationException("请手动实现 permute");
    }

    /**
     * 回溯核心方法
     * @param nums   原始数组
     * @param path   当前路径（已选的数字）
     * @param used   标记哪些元素已经被选过
     * @param result 最终结果集
     */
    private void backtrack(int[] nums, List<Integer> path, boolean[] used,
                           List<List<Integer>> result) {
        // TODO 终止条件：path.size() == nums.length
        //    → result.add(new ArrayList<>(path))  // 注意：要 new 一份！
        //    → return

        // TODO 遍历选择列表：
        //    for (int i = 0; i < nums.length; i++):
        //      if (used[i]) continue;     // 已选过，跳过
        //      used[i] = true;            // 标记已选
        //      path.add(nums[i]);         // 做选择
        //      backtrack(nums, path, used, result); // 递归
        //      path.remove(path.size() - 1); // 撤销选择（回溯！）
        //      used[i] = false;           // 取消标记

        /*
        // ====== 参考答案 ======
        if (path.size() == nums.length) {
            result.add(new ArrayList<>(path));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            used[i] = true;
            path.add(nums[i]);
            backtrack(nums, path, used, result);
            path.remove(path.size() - 1);
            used[i] = false;
        }
        */
        throw new UnsupportedOperationException("请手动实现 backtrack");
    }


    // ============================================================
    // 第 10 题：最长回文子串 (Longest Palindromic Substring) — DP/中心扩散
    // LeetCode 5: https://leetcode.cn/problems/longest-palindromic-substring/
    // 技巧：中心扩散法（推荐），每个位置向两边扩展
    //       奇回文（单中心）和偶回文（双中心）分别处理
    // 时间 O(n²)  空间 O(1)
    // ============================================================

    /**
     * 返回字符串中的最长回文子串。
     */
    public String longestPalindrome(String s) {
        // TODO 第 1 步：边界判断（长度 < 2 直接返回）

        // TODO 第 2 步：start = 0, maxLen = 0
        //    遍历 i = 0..n-1:
        //      以 i 为中心扩展 → 奇回文（"aba"型，中心是单个字符）
        //      以 i 和 i+1 为中心扩展 → 偶回文（"abba"型，中心是两个字符）
        //      每次扩展后更新 start 和 maxLen

        // TODO 第 3 步：return s.substring(start, start + maxLen)

        /*
        // ====== 参考答案 ======
        if (s == null || s.length() < 2) return s;

        int start = 0, maxLen = 0;

        for (int i = 0; i < s.length(); i++) {
            // 奇回文："aba"，中心是 i
            int len1 = expandAroundCenter(s, i, i);
            // 偶回文："abba"，中心是 i 和 i+1
            int len2 = expandAroundCenter(s, i, i + 1);

            int len = Math.max(len1, len2);
            if (len > maxLen) {
                maxLen = len;
                start = i - (len - 1) / 2;
            }
        }

        return s.substring(start, start + maxLen);
        */

        // 附加题：DP 写法（空间 O(n²)，时间 O(n²)）
        /*
        // ====== 参考答案（DP）======
        int n = s.length();
        boolean[][] dp = new boolean[n][n];
        int start = 0, maxLen = 1;

        // 长度为 1 的都是回文
        for (int i = 0; i < n; i++) dp[i][i] = true;

        // 按长度递增填表
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;
                if (s.charAt(i) == s.charAt(j)) {
                    if (len == 2) {
                        dp[i][j] = true;
                    } else {
                        dp[i][j] = dp[i + 1][j - 1];
                    }
                }
                if (dp[i][j] && len > maxLen) {
                    start = i;
                    maxLen = len;
                }
            }
        }
        return s.substring(start, start + maxLen);
        */
        throw new UnsupportedOperationException("请手动实现 longestPalindrome");
    }

    /**
     * 中心扩散：返回以 left, right 为中心的最长回文子串长度
     */
    private int expandAroundCenter(String s, int left, int right) {
        // TODO while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)):
        //        left--;
        //        right++;
        //     return right - left - 1;  // 注意：回文长度 = (right-1) - (left+1) + 1 = right-left-1

        /*
        // ====== 参考答案 ======
        while (left >= 0 && right < s.length()
                && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }
        return right - left - 1;
        */
        throw new UnsupportedOperationException("请手动实现 expandAroundCenter");
    }


    // ============================================================
    // 测试入口：每个方法都有独立的测试用例
    // ============================================================

    public static void main(String[] args) {
        Week01Solutions sol = new Week01Solutions();
        int passed = 0, failed = 0;

        // --- 1. 两数之和 ---
        try {
            int[] r1 = sol.twoSum(new int[]{2, 7, 11, 15}, 9);
            assert Arrays.equals(r1, new int[]{0, 1}) : "twoSum 失败";
            System.out.println("✅ 第1题 两数之和 通过");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println("⏳ 第1题 两数之和 待实现");
        } catch (AssertionError e) {
            System.out.println("❌ 第1题 两数之和 失败: " + e.getMessage());
            failed++;
        }

        // --- 2. 三数之和 ---
        try {
            List<List<Integer>> r2 = sol.threeSum(new int[]{-1, 0, 1, 2, -1, -4});
            assert r2.size() == 2 : "三数之和 结果数量不对";
            System.out.println("✅ 第2题 三数之和 通过");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println("⏳ 第2题 三数之和 待实现");
        } catch (AssertionError e) {
            System.out.println("❌ 第2题 三数之和 失败: " + e.getMessage());
            failed++;
        }

        // --- 3. 无重复字符最长子串 ---
        try {
            assert sol.lengthOfLongestSubstring("abcabcbb") == 3 : "lengthOfLongestSubstring 失败";
            assert sol.lengthOfLongestSubstring("bbbbb") == 1 : "lengthOfLongestSubstring 失败";
            assert sol.lengthOfLongestSubstring("pwwkew") == 3 : "lengthOfLongestSubstring 失败";
            System.out.println("✅ 第3题 无重复字符最长子串 通过");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println("⏳ 第3题 无重复字符最长子串 待实现");
        } catch (AssertionError e) {
            System.out.println("❌ 第3题 无重复字符最长子串 失败: " + e.getMessage());
            failed++;
        }

        // --- 4. 反转链表 ---
        try {
            // 构建链表 1→2→3→4→5
            ListNode head = new ListNode(1, new ListNode(2, new ListNode(3,
                    new ListNode(4, new ListNode(5)))));
            ListNode reversed = sol.reverseList(head);
            // 验证反转后是 5→4→3→2→1
            assert reversed.val == 5 : "reverseList 头节点不对";
            assert reversed.next.val == 4 : "reverseList 第二个节点不对";
            assert reversed.next.next.next.next.val == 1 : "reverseList 尾节点不对";
            System.out.println("✅ 第4题 反转链表 通过");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println("⏳ 第4题 反转链表 待实现");
        } catch (AssertionError e) {
            System.out.println("❌ 第4题 反转链表 失败: " + e.getMessage());
            failed++;
        }

        // --- 5. 有效的括号 ---
        try {
            assert sol.isValid("()") : "isValid 失败";
            assert sol.isValid("()[]{}") : "isValid 失败";
            assert !sol.isValid("(]") : "isValid 失败";
            assert sol.isValid("{()}") : "isValid 失败";
            System.out.println("✅ 第5题 有效的括号 通过");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println("⏳ 第5题 有效的括号 待实现");
        } catch (AssertionError e) {
            System.out.println("❌ 第5题 有效的括号 失败: " + e.getMessage());
            failed++;
        }

        // --- 6. 合并两个有序链表 ---
        try {
            ListNode l1 = new ListNode(1, new ListNode(2, new ListNode(4)));
            ListNode l2 = new ListNode(1, new ListNode(3, new ListNode(4)));
            ListNode merged = sol.mergeTwoLists(l1, l2);
            assert merged.val == 1 : "mergeTwoLists 头节点不对";
            System.out.println("✅ 第6题 合并两个有序链表 通过");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println("⏳ 第6题 合并两个有序链表 待实现");
        } catch (AssertionError e) {
            System.out.println("❌ 第6题 合并两个有序链表 失败: " + e.getMessage());
            failed++;
        }

        // --- 7. 二叉树的中序遍历 ---
        try {
            TreeNode root = new TreeNode(1, null,
                    new TreeNode(2, new TreeNode(3), null));
            List<Integer> r7 = sol.inorderTraversal(root);
            assert r7.equals(Arrays.asList(1, 3, 2)) : "inorderTraversal 结果不对: " + r7;
            System.out.println("✅ 第7题 中序遍历 通过");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println("⏳ 第7题 中序遍历 待实现");
        } catch (AssertionError e) {
            System.out.println("❌ 第7题 中序遍历 失败: " + e.getMessage());
            failed++;
        }

        // --- 8. 岛屿数量 ---
        try {
            char[][] grid = {
                {'1','1','0','0','0'},
                {'1','1','0','0','0'},
                {'0','0','1','0','0'},
                {'0','0','0','1','1'}
            };
            assert sol.numIslands(grid) == 3 : "numIslands 结果不对";
            System.out.println("✅ 第8题 岛屿数量 通过");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println("⏳ 第8题 岛屿数量 待实现");
        } catch (AssertionError e) {
            System.out.println("❌ 第8题 岛屿数量 失败: " + e.getMessage());
            failed++;
        }

        // --- 9. 全排列 ---
        try {
            List<List<Integer>> r9 = sol.permute(new int[]{1, 2, 3});
            assert r9.size() == 6 : "permute 结果数量不对，应为6，实际" + r9.size();
            System.out.println("✅ 第9题 全排列 通过");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println("⏳ 第9题 全排列 待实现");
        } catch (AssertionError e) {
            System.out.println("❌ 第9题 全排列 失败: " + e.getMessage());
            failed++;
        }

        // --- 10. 最长回文子串 ---
        try {
            String r10_1 = sol.longestPalindrome("babad");
            assert r10_1.length() == 3 : "longestPalindrome('babad') 长度应为3";
            String r10_2 = sol.longestPalindrome("cbbd");
            assert r10_2.equals("bb") : "longestPalindrome('cbbd') 应为'bb'";
            System.out.println("✅ 第10题 最长回文子串 通过");
            passed++;
        } catch (UnsupportedOperationException e) {
            System.out.println("⏳ 第10题 最长回文子串 待实现");
        } catch (AssertionError e) {
            System.out.println("❌ 第10题 最长回文子串 失败: " + e.getMessage());
            failed++;
        }

        // --- 汇总 ---
        System.out.println("\n====== 测试结果 ======");
        System.out.println(" 通过: " + passed + "  失败: " + failed + "  待实现: " + (10 - passed - failed));
        System.out.println("======================");
    }
}
