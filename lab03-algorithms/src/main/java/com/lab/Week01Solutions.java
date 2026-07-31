package com.lab;

import java.util.*;

/**
 * Week 1 算法练习 —— LeetCode Hot 100 前 10 题
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

    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (map.containsKey(complement)) {
                return new int[]{map.get(complement), i};
            }
            map.put(nums[i], i);
        }
        return new int[0];
    }


    // ============================================================
    // 第 2 题：三数之和 (3Sum) — 排序 + 双指针
    // LeetCode 15: https://leetcode.cn/problems/3sum/
    // 技巧：排序 → 固定 i → left/right 双指针夹逼 → 去重是关键
    // 时间 O(n²)  空间 O(1)（排序栈不计）
    // ============================================================

    public List<List<Integer>> threeSum(int[] nums) {
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
    }


    // ============================================================
    // 第 3 题：无重复字符的最长子串 — 滑动窗口
    // LeetCode 3: https://leetcode.cn/problems/longest-substring-without-repeating-characters/
    // 技巧：HashMap 记字符最后出现位置，窗口收缩到重复字符之后
    // 时间 O(n)  空间 O(字符集大小)
    // ============================================================

    public int lengthOfLongestSubstring(String s) {
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
    }


    // ============================================================
    // 第 4 题：反转链表 — 链表
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

    public ListNode reverseList(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;
        while (curr != null) {
            ListNode next = curr.next;   // ① 先记住下一个节点
            curr.next = prev;            // ② 反转：当前节点指向前一个
            prev = curr;                 // ③ 前移 prev
            curr = next;                 // ④ 前移 curr
        }
        return prev;
    }


    // ============================================================
    // 第 5 题：有效的括号 — 栈
    // LeetCode 20: https://leetcode.cn/problems/valid-parentheses/
    // 技巧：左括号入栈，右括号匹配栈顶
    // 时间 O(n)  空间 O(n)
    // ============================================================

    public boolean isValid(String s) {
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
    }


    // ============================================================
    // 第 6 题：合并两个有序链表 — 链表
    // LeetCode 21: https://leetcode.cn/problems/merge-two-sorted-lists/
    // 技巧：哨兵(dummy)节点 + 比较插入
    // 时间 O(m+n)  空间 O(1)
    // ============================================================

    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
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
    }


    // ============================================================
    // 第 7 题：二叉树的中序遍历 — 树
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

    /** 中序遍历（递归版）：左 → 根 → 右 */
    public List<Integer> inorderTraversal(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        inorder(root, result);
        return result;
    }

    private void inorder(TreeNode node, List<Integer> result) {
        if (node == null) return;
        inorder(node.left, result);
        result.add(node.val);
        inorder(node.right, result);
    }


    // ============================================================
    // 第 8 题：岛屿数量 — DFS 淹没
    // LeetCode 200: https://leetcode.cn/problems/number-of-islands/
    // 技巧：遍历网格 → 遇到 '1' count++ → DFS 淹掉整座岛（1→0）
    // 时间 O(m×n)  空间 O(m×n)（递归栈最坏情况）
    // ============================================================

    public int numIslands(char[][] grid) {
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
    }

    /** DFS 淹没：把 (i,j) 及其上下左右相连的 '1' 全部变为 '0' */
    private void dfs(char[][] grid, int i, int j) {
        if (i < 0 || i >= grid.length || j < 0 || j >= grid[0].length) return;
        if (grid[i][j] == '0') return;

        grid[i][j] = '0';

        dfs(grid, i - 1, j); // 上
        dfs(grid, i + 1, j); // 下
        dfs(grid, i, j - 1); // 左
        dfs(grid, i, j + 1); // 右
    }


    // ============================================================
    // 第 9 题：全排列 — 回溯
    // LeetCode 46: https://leetcode.cn/problems/permutations/
    // 技巧：回溯模板 for→choose→backtrack→unchoose
    // 时间 O(n×n!)  空间 O(n)
    // ============================================================

    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        boolean[] used = new boolean[nums.length];
        backtrack(nums, new ArrayList<>(), used, result);
        return result;
    }

    private void backtrack(int[] nums, List<Integer> path, boolean[] used,
                           List<List<Integer>> result) {
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
    }


    // ============================================================
    // 第 10 题：最长回文子串 — 中心扩散法
    // LeetCode 5: https://leetcode.cn/problems/longest-palindromic-substring/
    // 技巧：中心扩散法，每个位置向两边扩展
    //       奇回文（单中心）和偶回文（双中心）分别处理
    // 时间 O(n²)  空间 O(1)
    // ============================================================

    public String longestPalindrome(String s) {
        if (s == null || s.length() < 2) return s;

        int start = 0, maxLen = 0;

        for (int i = 0; i < s.length(); i++) {
            int len1 = expandAroundCenter(s, i, i);      // 奇回文："aba"
            int len2 = expandAroundCenter(s, i, i + 1);  // 偶回文："abba"

            int len = Math.max(len1, len2);
            if (len > maxLen) {
                maxLen = len;
                start = i - (len - 1) / 2;
            }
        }

        return s.substring(start, start + maxLen);
    }

    /** 返回以 left, right 为中心的最长回文子串长度 */
    private int expandAroundCenter(String s, int left, int right) {
        while (left >= 0 && right < s.length()
                && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }
        // 循环退出时 left 和 right 多走了一步
        return right - left - 1;
    }


    // ============================================================
    // 测试入口
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
        } catch (AssertionError e) {
            System.out.println("❌ 第2题 三数之和 失败: " + e.getMessage());
            failed++;
        }

        // --- 3. 无重复字符最长子串 ---
        try {
            assert sol.lengthOfLongestSubstring("abcabcbb") == 3;
            assert sol.lengthOfLongestSubstring("bbbbb") == 1;
            assert sol.lengthOfLongestSubstring("pwwkew") == 3;
            System.out.println("✅ 第3题 无重复字符最长子串 通过");
            passed++;
        } catch (AssertionError e) {
            System.out.println("❌ 第3题 无重复字符最长子串 失败: " + e.getMessage());
            failed++;
        }

        // --- 4. 反转链表 ---
        try {
            ListNode head = new ListNode(1, new ListNode(2, new ListNode(3,
                    new ListNode(4, new ListNode(5)))));
            ListNode reversed = sol.reverseList(head);
            assert reversed.val == 5 : "reverseList 头节点不对";
            assert reversed.next.val == 4 : "reverseList 第二个节点不对";
            assert reversed.next.next.next.next.val == 1 : "reverseList 尾节点不对";
            System.out.println("✅ 第4题 反转链表 通过");
            passed++;
        } catch (AssertionError e) {
            System.out.println("❌ 第4题 反转链表 失败: " + e.getMessage());
            failed++;
        }

        // --- 5. 有效的括号 ---
        try {
            assert sol.isValid("()");
            assert sol.isValid("()[]{}");
            assert !sol.isValid("(]");
            assert sol.isValid("{()}");
            System.out.println("✅ 第5题 有效的括号 通过");
            passed++;
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
        } catch (AssertionError e) {
            System.out.println("❌ 第10题 最长回文子串 失败: " + e.getMessage());
            failed++;
        }

        // --- 汇总 ---
        System.out.println("\n====== 测试结果 ======");
        System.out.println(" 通过: " + passed + "  失败: " + failed);
        System.out.println("======================");
    }
}
