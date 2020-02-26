import com.sun.source.tree.Tree;

import javax.swing.*;
import java.text.NumberFormat;
import java.util.*;

public class PageManager {
    Stack<Integer> instructionStream;            // 指令流
    Vector<Integer> demandPageTable;            // 请求页表
    final int virtualMemoryCapacity = 32;    // 虚存容量 32 KB = 32768 B
    int realMemoryTableNum;                    // 实存页数
    double pageSize;                            // 页面尺寸
    int pageTableCacheSize;
    int instructionNum;

    PageManager(int instructionNum) {
        this.instructionNum = instructionNum;
        realMemoryTableNum = 32;
        pageSize = 1;
        pageTableCacheSize = 4;
        instructionStream = new Stack<>();
        demandPageTable = new Vector<>();
        createInstructionStream();
    }

    void createInstructionStream() {
        int randNum;
        int virtualMem = virtualMemoryCapacity * 1024;
        instructionStream.add((int) (Math.random() * (virtualMem)));
        for (int i = 1; i < instructionNum; ++i) {
            int last = instructionStream.get(instructionStream.size() - 1);
            // 1/2 顺序
            if ((randNum = (int) (Math.random() * 4)) <= 2) {
                if (last < virtualMem) {
                    instructionStream.add(last + 1);
                } else {
                    instructionStream.add(virtualMem);
                }
            }
            // 1/4 前地址
            else if (randNum <= 3) {
                if (last == 0) {
                    instructionStream.add(0);
                    continue;
                }
                instructionStream.add((int) (Math.random() * last));
            }
            // 1/4 后地址
            else {
                if (virtualMem - last - 1 == 0) {
                    instructionStream.add(virtualMem - 1);
                }
                instructionStream.add((int) (Math.random() * (virtualMem - last - 1) + 1 + last));
            }
        }
        // 确定页号
        instructionStream.forEach(integer -> integer = (int) (integer / (1024 * pageSize)));
    }

    /**
     * 最佳淘汰算法
     */
    void OPT() {
        HashMap<Integer, Integer> failFeqMap;
        Stack<Integer> instructionStream = (Stack<Integer>) this.instructionStream.clone();
        failFeqMap = new HashMap<>();
        demandPageTable.clear();
        //填满请求页表 && 当页表还未填满时
        do {
            int num = instructionStream.pop();
            // 如果请求页表中已存在该页，跳过
            int indexRes = demandPageTable.indexOf(num);
            if (indexRes != -1) {
                continue;
            }
            recordFail(failFeqMap, num);
            demandPageTable.add(num);
        } while (demandPageTable.size() <= pageTableCacheSize && !instructionStream.isEmpty());
        // 指令流输入 && 替换开始
        while (!instructionStream.isEmpty()) {
            int num = instructionStream.remove(0);

            // 如果请求页表中已存在该页，跳过
            int indexRes = demandPageTable.indexOf(num);
            if (indexRes != -1) {
                continue;
            }
            // 未命中替换
            recordFail(failFeqMap, num);
            if (instructionStream.isEmpty()) {
                outputHitRate(failFeqMap);
                return;
            }
            int streamOutIndex = 0;
            int tableIndex = -1;
            int outIndex;
            for (outIndex = 0; outIndex < demandPageTable.size(); outIndex++) {
                // 第一次出现请求的位置，找出最后访问的页位置
                int tp = instructionStream.indexOf(demandPageTable.get(outIndex));
                if (tp == -1) {
                    tableIndex = outIndex;
                    streamOutIndex = tp;
                    break;
                }
                if (tp > streamOutIndex) {
                    streamOutIndex = tp;
                    tableIndex = outIndex;

                }
            }
            // 请求页表删除
            demandPageTable.remove(tableIndex);
            demandPageTable.add(num);
        }
        outputHitRate(failFeqMap);


    }

    /**
     * 最少使用频次淘汰算法
     */
    void LFU() {
        HashMap<Integer,Integer>failFeqMap = new HashMap<>();
        Stack<Integer> instructionStream = (Stack<Integer>) this.instructionStream.clone();
        demandPageTable.clear();
        Vector<Integer> freRecord = new Vector<>();
        do {
            int num = instructionStream.pop();
            int indexRes = demandPageTable.indexOf(num);
            if (indexRes != -1) {
                freRecord.set(indexRes,freRecord.get(indexRes)+1);
                continue;
            }
            recordFail(failFeqMap, num);
            freRecord.add(1);
            demandPageTable.add(num);
        } while (demandPageTable.size() <= pageTableCacheSize && !instructionStream.isEmpty());
        // 指令流输入
        while (!instructionStream.isEmpty()) {
            int num = instructionStream.pop();
            int indexRes = demandPageTable.indexOf(num);
            if (indexRes != -1) {
                freRecord.set(indexRes,freRecord.get(indexRes)+1);
                continue;
            }
            recordFail(failFeqMap, num);
            int min = Integer.MAX_VALUE;
            int key = -1;
            // 删去频次最少的
            for (int i = 0; i < freRecord.size(); i++) {
                int times = freRecord.get(i);
                if(times < min){
                    min = times;
                    key = i;
                }
            }
            demandPageTable.remove(key);
            freRecord.remove(key);
            freRecord.add(1);
            demandPageTable.add(num);

        }
        outputHitRate(failFeqMap);
    }

    /**
     * 最近最少使用页淘汰算法
     */
    void LRU() {
        HashMap<Integer, Integer> failFeqMap;
        Stack<Integer> instructionStream = (Stack<Integer>) this.instructionStream.clone();
        failFeqMap = new HashMap<Integer, Integer>();
        demandPageTable.clear();
        // 填表
        do {
            int num = instructionStream.pop();
            // 如果请求页表中已存在该页，放入页表最后
            int indexRes = demandPageTable.indexOf(num);
            if (indexRes != -1) {
                demandPageTable.remove(indexRes);
                demandPageTable.add(num);
                continue;
            }
            recordFail(failFeqMap, num);
            demandPageTable.add(num);
        } while (demandPageTable.size() <= pageTableCacheSize && !instructionStream.isEmpty());
        // 指令流输入
        while (!instructionStream.isEmpty()) {
            int num = instructionStream.pop();
            // 如果请求页表中已存在该页，放入页表最后
            int indexRes = demandPageTable.indexOf(num);
            if (indexRes != -1) {
                demandPageTable.remove(indexRes);
                demandPageTable.add(num);
                continue;
            }
            recordFail(failFeqMap, num);
            // 删去最久未使用的
            demandPageTable.remove(0);
            // 最后
            demandPageTable.add(num);
        }
        outputHitRate(failFeqMap);

    }


    /**
     * 先进先出算法
     */
    void FIFO() {
        HashMap<Integer, Integer> failFeqMap;
        Stack<Integer> instructionStream = (Stack<Integer>) this.instructionStream.clone();
        failFeqMap = new HashMap<Integer, Integer>();
        demandPageTable.clear();
        // 填表
        do {
            int num = instructionStream.pop();
            int indexRes = demandPageTable.indexOf(num);
            if (indexRes != -1) {
                continue;
            }
            recordFail(failFeqMap, num);
            demandPageTable.add(num);
        } while (demandPageTable.size() <= pageTableCacheSize && !instructionStream.isEmpty());
        // 指令流输入
        while (!instructionStream.isEmpty()) {
            int num = instructionStream.pop();
            // 如果请求页表中已存在该页，放入页表最后
            int indexRes = demandPageTable.indexOf(num);
            if (indexRes != -1) {
                continue;
            }
            recordFail(failFeqMap, num);
            // 删去最久未使用的
            demandPageTable.remove(0);
            // 最后
            demandPageTable.add(num);
        }
        outputHitRate(failFeqMap);
    }

    void outputHitRate(HashMap<Integer, Integer> failFeqMap) {
        int sum = 0;
        for (Integer i :
                failFeqMap.values()) {
            sum += i;
        }
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(4);
        System.out.printf("缺页次数： %d, 命中率： %s%%\n", sum, format.format((float) (instructionNum - sum) / (float) instructionNum * 100));
    }

    void clearInstructionStream() {
        instructionStream.clear();
    }

    void setParameters(int realMemoryTableNum, int pageSize, int pageTableCacheSize) {
        this.realMemoryTableNum = realMemoryTableNum;
        this.pageSize = pageSize;
        this.pageTableCacheSize = pageTableCacheSize;
    }

    public Stack<Integer> getInstructionStream() {
        return instructionStream;
    }

    public void recordFail(HashMap<Integer, Integer> failFeqMap, int num) {
        if (failFeqMap.containsKey(num)) {
            failFeqMap.put(num, failFeqMap.get(num) + 1);
        } else {
            failFeqMap.put(num, 1);
        }
    }

    public static void main(String[] args) {
        Vector<Integer> stack = new Vector<>();
        stack.add(2);
        stack.add(5);
        stack.add(7);
        stack.remove(0);
    }
}
