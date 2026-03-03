package org.agmas.noellesroles.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MathProblemsManager {
    private static final Random random = new Random();
    private static final char[] OPERATORS = { '+', '-', '*', '/' };

    /**
     * 随机生成一道数学题目
     * 
     * @return 包含题目及选项的 MathProblem 对象
     */
    public MathProblem generateProblem() {
        // 随机选择题目类型：0 表示加减乘除，1 表示简单求导
        int type = random.nextInt(40);
        return type == 0 ? generateDerivativeProblem() : generateArithmeticProblem();
    }

    /**
     * 生成一道加减乘除题目
     */
    private MathProblem generateArithmeticProblem() {
        int num1 = random.nextInt(-16, 17); // 1~10
        int num2 = random.nextInt(-16, 17); // 1~10
        char op = OPERATORS[random.nextInt(OPERATORS.length)];
        int result;
        String opDisplay = "";
        switch (op) {
            case '+':
                result = num1 + num2;
                opDisplay = "+";
                break;
            case '-':
                // 保证结果非负（适合小学题目）
                result = num1 - num2;
                opDisplay = "-";
                break;
            case '*':
                result = num1 * num2;
                opDisplay = "×";
                break;
            case '/':
                // 保证整除：num1 调整为 num2 的倍数
                int multiplier = random.nextInt(5) + 1; // 1~5 倍
                num1 = num2 * multiplier;
                result = multiplier;
                opDisplay = "÷";
                break;
            default:
                result = 0;
        }

        String question = num1 + " " + opDisplay + " " + (num2 < 0 ? " (" + num2 + ")" : num2) + " = ";

        // 生成四个选项（一个正确，三个随机错误）
        List<String> options = new ArrayList<>();
        List<String> woptions = new ArrayList<>();
        options.add(String.valueOf(result)); // 正确答案

        // 生成三个不同的错误选项
        woptions.add(String.valueOf(result * -1));

        while (woptions.size() <= 5) {
            // 策略一：在正确答案附近 ±3 范围内生成
            int wrong = result + random.nextInt(7) - 3;
            if (!woptions.contains(String.valueOf(wrong)) && !options.contains(String.valueOf(wrong))) {
                woptions.add(String.valueOf(wrong));
            } else {
                wrong = random.nextInt(-100, 100);
                if (!woptions.contains(String.valueOf(wrong)) && !options.contains(String.valueOf(wrong))) {
                    woptions.add(String.valueOf(wrong));
                }
            }
        }
        // 打乱选项顺序
        Collections.shuffle(woptions);
        for (int i = 0; i < 3; i++) {
            options.add(woptions.get(i));
        }
        Collections.shuffle(options);
        int correctIndex = options.indexOf(String.valueOf(result));

        return new MathProblem(question, options, correctIndex, 1);
    }

    /**
     * 格式化幂函数项：将系数和指数转换为美观的字符串
     * 例如 (2,2) -> "2x²", (1,1) -> "x", (3,0) -> "3"
     */
    private String formatTerm(int coeff, int exp) {
        if (coeff == 0) {
            return "0";
        }

        // 系数部分：系数为1且指数非0时省略系数
        String coeffPart = (coeff == 1 && exp != 0) ? "" : String.valueOf(coeff);

        // 指数部分
        if (exp == 0) {
            // 常数项：只返回系数（此时系数不省略，即使coeff=1也显示"1"）
            return String.valueOf(coeff);
        } else if (exp == 1) {
            // 一次项：系数 + "x"（系数已按规则省略）
            return coeffPart + "x";
        } else {
            // 高次项：需要将指数转换为上标
            String[] powers = { "⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹" };
            StringBuilder expPart = new StringBuilder();
            int tmp = exp;
            // 将指数的每一位数字转为上标并收集
            java.util.ArrayList<String> digits = new java.util.ArrayList<>();
            while (tmp > 0) {
                digits.add(powers[tmp % 10]);
                tmp /= 10;
            }
            // 反转顺序（原循环是从低位到高位，需要反转得到高位到低位）
            for (int i = digits.size() - 1; i >= 0; i--) {
                expPart.append(digits.get(i));
            }
            return coeffPart + "x" + expPart.toString();
        }
    }

    private MathProblem generateDerivativeProblem() {
        int coefficient = random.nextInt(-10, 10); // 系数 -10~9
        int exponent = random.nextInt(1, 16); // 指数 0~15

        // 原函数表达式（使用上标）
        String function = formatTerm(coefficient, exponent);

        // 求导结果
        int newCoeff = coefficient * exponent;
        int newExp = exponent - 1;
        String derivative = formatTerm(newCoeff, newExp); // 自动处理系数省略和上标

        String question = "F(x) = " + function + "; F'(x) = ?";

        // 生成选项
        List<String> options = new ArrayList<>();
        options.add(derivative);

        // 准备错误候选（均使用 formatTerm 格式化，保持一致性）
        List<String> wrongCandidates = new ArrayList<>();

        // 1. 系数变化 (±1, ±2)
        for (int delta : new int[] { 1, -1, 2, -2 }) {
            int wrongCoeff = newCoeff + delta;
            wrongCandidates.add(formatTerm(wrongCoeff, newExp));
        }
        // 1[5]. 符号变化 (±1, ±2)
        {
            int wrongCoeff = newCoeff * -1;
            wrongCandidates.add(formatTerm(wrongCoeff, newExp));
        }
        // 2. 指数变化 (±1)
        for (int delta : new int[] { 1, -1 }) {
            int wrongExp = newExp + delta;
            if (wrongExp >= 0) {
                wrongCandidates.add(formatTerm(newCoeff, wrongExp));
            }
        }

        // 3. 原函数本身
        wrongCandidates.add(function);

        // 4. 常见错误（如漏写系数/指数）
        // - 原函数错写为导数形式（如 2x² 的导数错误写成 4x³）
        // - 指数加2等
        wrongCandidates.add(formatTerm(newCoeff, newExp + 2));
        if (newExp == 0) {
            wrongCandidates.add(formatTerm(newCoeff, 1)); // 常数项误加x
        } else if (newExp == 1) {
            wrongCandidates.add(formatTerm(newCoeff, 2));
        } else {
            wrongCandidates.add(formatTerm(newCoeff, newExp - 1));
        }

        // 去重并选取三个不同的错误选项
        List<String> wrongOptions = new ArrayList<>();
        for (String cand : wrongCandidates) {
            if (cand != null && !cand.equals(derivative) && !wrongOptions.contains(cand)) {
                wrongOptions.add(cand);
                if (wrongOptions.size() >= 3)
                    break;
            }
        }

        // 若候选不足，补充默认值（也使用上标格式）
        if (wrongOptions.size() < 3) {
            String[] defaults = {
                    formatTerm(0, 0), // "0"
                    formatTerm(1, 0), // "1"
                    formatTerm(1, 1), // "x"
                    formatTerm(2, 1), // "2x"
                    formatTerm(1, 2) // "x²"
            };
            for (String def : defaults) {
                if (!def.equals(derivative) && !wrongOptions.contains(def)) {
                    wrongOptions.add(def);
                    if (wrongOptions.size() >= 3)
                        break;
                }
            }
        }

        options.addAll(wrongOptions);
        Collections.shuffle(options);
        int correctIndex = options.indexOf(derivative);

        return new MathProblem(question, options, correctIndex, 2);
    }

    /**
     * 数学题目信息类
     * 包含题目名称、四个选项以及正确选项的索引
     */
    public static class MathProblem {
        private final String question; // 题目名称，例如 "3 + 5 = "
        private final List<String> options; // 四个选项
        private final int correctIndex; // 正确选项在列表中的索引（0~3）
        private final int type; // 问题类型

        public MathProblem(String question, List<String> options, int correctIndex, int type) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public String getQuestion() {
            return question;
        }

        public List<String> getOptions() {
            return options;
        }

        public int getCorrectIndex() {
            return correctIndex;
        }
    }

}
