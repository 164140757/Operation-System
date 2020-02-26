import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Sheduler {
    /**
     * readyQueue,queue -> find super
     */
    public static class PCBScheduler {

        private static final int featuresNub = 5;
        private static ArrayList<Integer> NEXT;
        private static ArrayList<Integer> WAITING;
        // 公有链表
        private static ArrayList<PCB> readyQueue;
        private static ArrayList<PCB> blockingQueue;

        // 已分配到的资源数
        static int total;
        // 剩下的资源数
        static int remainder = 100;
        // 当前运行的进程号
        static int CurrentIndexOfProcess;
        // 随机设置分配的资源数范围
        protected static int MINSource = 1;
        protected static int MAXSource = 15;

        private static enum ALGORITHMSTYPE {PRIORITY, RR, BANKER}

        ArrayList<PCB> getReadyQueue() {
            return readyQueue;
        }

        private ArrayList<PCB> getBlockingQueue() {
            return blockingQueue;
        }

        public static void main(String[] args) {
            PCBScheduler scheduler = new PCBScheduler();
            scheduler.createPCB(6);
            RR p = scheduler.new RR();
            while (!scheduler.getReadyQueue().isEmpty()) {
                p.update();
            }
        }

        PCBScheduler() {
            readyQueue = new ArrayList<>();
            blockingQueue = new ArrayList<>();
            NEXT = new ArrayList<>();
            WAITING = new ArrayList<>();
        }

        /***
         * 展示当前链表中进程的状态
         */
        private void showStatus(ALGORITHMSTYPE algo) {
            if (algo == ALGORITHMSTYPE.PRIORITY) {
                System.out.println("TYPE THE ALGORITHM:" + ALGORITHMSTYPE.PRIORITY);
            }

            if (algo == ALGORITHMSTYPE.RR) {
                System.out.println("TYPE THE ALGORITHM:" + ALGORITHMSTYPE.RR);
            }
            System.out.print("==========================================\nRUNNING PROCESSES\t\t\tWAITING QUEUE\n\t\t\t\t\t\t\t");
            for (int numb : WAITING) {
                System.out.printf("%d ", numb);
            }


            System.out.println("\n===========================================");
            // 准备结果矩阵
            Object[][] result = new Object[featuresNub][readyQueue.size()];
            // 在等候队列中输出当前情况信息
            // WAITING（备份）按照ID排序
            ArrayList<Integer> tmp = WAITING;
            // 搞懂写法！
            tmp.sort(Integer::compareTo);
            for (int j = 0; j < tmp.size(); j++) {
                int ID = tmp.get(j);
                PCB pcb = findPCB(ID);
                ArrayList<Object> temp = new ArrayList<>();
                temp.add(pcb.getIndexProcess());
                temp.add(pcb.getTimeSlicesPriority());
                temp.add(pcb.getCPUTime());
                temp.add(pcb.getTimeslicesInNeed());
                temp.add(pcb.getStatus());
                for (int i = 0; i < featuresNub; i++) {
                    result[i][j] = temp.get(i);
                }


            }
            // 打印
            print(0, "ID", result);
            print(1, "PRIORITY", result);
            print(2, "CPUTIME", result);
            print(3, "TIMELEFT", result);
            print(4, "STATE", result);
            System.out.printf("%15s ", "NEXT" + "\t\t\t\t\t");
            for (Integer integer : NEXT) {
                System.out.printf("%s ", integer);
            }
            System.out.println();
        }

        /**
         * 打印结果矩阵
         *
         * @param index 矩阵第index行
         * @param head  标题
         */
        private void print(int index, String head, Object[][] result) {
            System.out.printf("%15s", head + "\t\t\t\t\t");
            for (int i = 0; i < readyQueue.size(); i++) {
                System.out.printf("%s ", result[index][i]);
            }
            System.out.println();
        }

        private ArrayList<Integer> getListID() {
            ArrayList<Integer> tmp = new ArrayList<>();
            for (PCB pcb : readyQueue
            ) {
                tmp.add(pcb.getIndexProcess());
            }
            return tmp;
        }

        /***
         *
         * @return 判断是否list中所有PCB已经运行结束
         */
        private boolean checkOver() {
            for (PCB pcb : readyQueue
            ) {
                if (pcb.getTimeslicesInNeed() > 0) return false;
            }
            System.out.println("所有进程已结束");
            return true;
        }

        ;

        /**
         * 创建 PCB并插入list
         *
         * @param numberOfPCB PCB 的个数
         */
        protected void createPCB(int numberOfPCB) {
            assert numberOfPCB > 0;
            // ID
            int ID = 1;
            while (numberOfPCB != 0) {
                // 此时的pcb已经有：PRIORITY，CPUTIME（0），ALLTIME
                PCB pcb = new PCB();
                pcb.setIndexProcess(ID++);
                pcb.setStatus(PCB.status.WAIT);
                readyQueue.add(pcb);
                numberOfPCB--;
            }
        }

        /***
         * 找到相应ID的PCB，并返回它的全部信息
         * @param ID 目标PCB的ID
         * @return 一个ArrayList 依次记录PCB信息(不包括next信息）
         */
        static PCB findPCB(int ID) {
            PCB target = null;
            for (PCB temp : readyQueue) {
                if (temp.getIndexProcess() == ID) {
                    target = temp;
                    break;
                }
            }
            if (target == null) throw new IllegalArgumentException("没有找到对应ID的PCB");
            return target;

        }

        private void test() {
            Type type = getClass().getGenericSuperclass();
            System.out.println(type);
        }

        // 设置一些PCB管理的基本操作(不同算法独有）
        interface SchedulePCBInterface {
            // 建立ArrayList管理进程,设置比较类，构建优先队列
            // 运行一个时间片之后，更新
            void update();
        }

        /**
         * PCB创建的计时器
         */
        static class timer {

        }

        /***
         * PCB 调度算法
         */
        class Priority implements SchedulePCBInterface {
            Comparator<PCB> comparator;

            Priority() {
                comparator = new Comparator<PCB>() {
                    @Override
                    public int compare(PCB o1, PCB o2) {
                        return o1.getTimeSlicesPriority() - o2.getTimeSlicesPriority();
                    }
                };
                readyQueue.sort(comparator);
                Collections.reverse(readyQueue);
                NEXT = getListID();
                WAITING = getListID();
            }

            /**
             * 每当时间片到后调用一次
             */
            @Override
            public void update() {
                PCB pcb = readyQueue.get(0);
                assert pcb != null;
                pcb.setStatus(PCB.status.RUN);
                // 时间片到，时间片数减掉1
                pcb.setTimeslicesInNeed(pcb.getTimeslicesInNeed() - 1);
                // 优先数减掉3
                pcb.setTimeSlicesPriority(pcb.getTimeSlicesPriority() - 3);
                // 占用CPU时间加1
                pcb.setCPUTime(pcb.getCPUTime() + 1);
                // 展示
                showStatus(ALGORITHMSTYPE.PRIORITY);
                // 更新list
                readyQueue.remove(0);
                if (pcb.getTimeslicesInNeed() != 0) {
                    pcb.setStatus(PCB.status.WAIT);
                    readyQueue.add(pcb);
                } else pcb.setStatus(PCB.status.FINISH);
                WAITING = getListID();
                // 更新list和相应数据
                // NEXT 为下一次的排序结果
                readyQueue.sort(comparator);
                Collections.reverse(readyQueue);
                NEXT = getListID();
            }


        }

        class RR implements SchedulePCBInterface {

            RR() {
                NEXT = getListID();
                WAITING = getListID();
            }

            @Override
            public void update() {
                PCB pcb = readyQueue.get(0);
                assert pcb != null;
                pcb.setStatus(PCB.status.RUN);
                // 时间片到，时间片数减掉1
                pcb.setTimeslicesInNeed(pcb.getTimeslicesInNeed() - 1);
                pcb.setCPUTime(pcb.getCPUTime() + 1);
                showStatus(ALGORITHMSTYPE.RR);
                // 检查所需时间片是否为0，非零继续做
                if (pcb.getTimeslicesInNeed() == 0) readyQueue.remove(0);
                    // 当占用CPU的时间与Priority相同，放入链表最后
                else if (pcb.getCPUTime() == pcb.getTimeSlicesPriority()) {
                    readyQueue.remove(0);
                    pcb.setStatus(PCB.status.WAIT);
                    readyQueue.add(readyQueue.size(), pcb);
                }
                // WAITING 与 readyQueue 同时更新
                WAITING = getListID();
                NEXT = WAITING;

            }


        }

        /**
         * 死锁检测
         */
        static class BankerAlgorithm {

            BankerAlgorithm(int numb) {
                if (!front(numb)) {
                    return;
                }
                int[] sign = new int[numb];
                judgeForBanker(sign);
            }

            BankerAlgorithm(int[] vClaim, int[] vAlc) {
                if (!front(vClaim, vAlc)) {
                    return;
                }
                int[] sign = new int[vClaim.length];
                judgeForBanker(sign);
            }

            private void judgeForBanker(int[] sign) {
                int order = 0;
                if (brute_force(readyQueue, 1, sign)) {
                    System.out.println("分配安全，可以进行分配,分配序列：");
                    for (int i :
                            sign) {
                        System.out.printf("%d ", i);
                    }
                } else {
                    System.out.println("分配不安全，无法分配.");
                }
            }

            private boolean brute_force(ArrayList<PCB> list, int order, int[] sign) {
                PriorityQueue<PCBDeadLock> unfinished = new PriorityQueue<PCBDeadLock>((o1, o2) -> {
                    int need1 = o1.getVpMaxClam() - o1.getValLocation();
                    int need2 = o2.getVpMaxClam() - o2.getValLocation();
                    return need1 - need2;
                }
                );
                for (int i = 0; i < list.size(); i++) {
                    PCBDeadLock pcb = (PCBDeadLock) list.get(i);
                    int ID = pcb.getIndexProcess();
                    int need = pcb.getVpMaxClam() - pcb.getValLocation();
                    if (!apply(ID, need)) {
                        unfinished.add(pcb);
                    } else {
                        sign[ID - 1] = order++;
                        // release
                        release(ID);
                    }
                }
                boolean s = false;
                if (unfinished.size() != 0) {
                    s = brute_force_2(unfinished, order, sign);
                    if (s) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return true;
            }

            private boolean brute_force_2(PriorityQueue<PCBDeadLock> unfinished, int order, int[] sign) {
                for (PCBDeadLock pcb : unfinished
                ) {
                    int ID = pcb.getIndexProcess();
                    int need = pcb.getVpMaxClam() - pcb.getValLocation();
                    if (!apply(ID, need)) {
                        return false;
                    } else {
                        sign[ID - 1] = order++;
                        release(ID);
                    }
                }
                return true;
            }


            private int randomSource() {
                return (int) (Math.random() * (MAXSource - MINSource) + MINSource);
            }

            private int randomNeed() {
                int MAXNeed = 10;
                int MINNeed = 1;
                return (int) (Math.random() * (MAXNeed - MINNeed) + MINNeed);

            }
            // 初始化进程，装入所有初始数据

            /***
             * 最少可以创建6个进程
             * @param numberOfProcess 输入创建进程数目
             * @return 是否创建成功
             */
            private boolean front(int numberOfProcess) {
                assert numberOfProcess > 0;
                readyQueue = new ArrayList<>();
                // ID
                int ID = 1;
                int temp = numberOfProcess;
                while (numberOfProcess != 0) {
                    // 此时的pcb已经有：PRIORITY，CPUTIME（0），ALLTIME
                    PCBDeadLock pcb = new PCBDeadLock();
                    pcb.setIndexProcess(ID++);
                    pcb.setStatus(PCB.status.WAIT);
                    // 设置PCBDeadLock的独有属性
                    pcb.setVpMaxClam(randomSource());
                    readyQueue.add(pcb);
                    numberOfProcess--;
                }
                return true;


            }

            private boolean front(int[] vpMaxClaim, int[] alo) {
                assert (vpMaxClaim.length == alo.length);
                readyQueue = new ArrayList<>();
                // ID
                int ID = 1;
                for (int i = 0; i < vpMaxClaim.length; i++) {
                    // 此时的pcb已经有：PRIORITY，CPUTIME（0），ALLTIME
                    PCBDeadLock pcb = new PCBDeadLock();
                    pcb.setIndexProcess(ID++);
                    pcb.setStatus(PCB.status.WAIT);
                    // 设置PCBDeadLock的独有属性
                    if (vpMaxClaim[i] < alo[i]) {
                        throw new IllegalArgumentException("input claim should be greater than allocation.");
                    }
                    pcb.setVpMaxClam(vpMaxClaim[i]);
                    pcb.setValLocation(alo[i]);
                    readyQueue.add(pcb);
                }
                return true;
            }

            /**
             * 进程号为ID的进程发出申请
             *
             * @param need 申请的资源数
             * @param ID   进程ID
             * @return 是否成功申请
             */
            private boolean apply(int ID, int need) {

                PCBDeadLock target = (PCBDeadLock) findPCB(ID);
                if (need > target.getVpMaxClam()) {
                    System.out.printf("申请ID为%d的进程超出最大需求\n", ID);
                    return false;
                } else {
                    if (remainder >= need) {
                        remainder -= need;
                        target.setValLocation(need + target.valLocation);
                        target.setvCount(1);
                        target.setVpAdvance(false);
                        return true;
                    } else {
                        System.out.printf("申请ID为%d的进程超出已有资源,尝试重新寻找\n", ID);
                        return false;
                    }
                }
            }

            private void release(int ID) {
                PCBDeadLock target = (PCBDeadLock) findPCB(ID);
                remainder += target.valLocation;
                target.setValLocation(0);
            }


        }
    }
}
