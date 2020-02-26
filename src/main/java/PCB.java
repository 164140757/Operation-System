// 中断（IO，资源请求）
// 随机到来

public class PCB {
    // 进程标志数
    private int indexProcess;
    // 进程所需要的时间片数
    private int timeslicesInNeed;

    // 已占用CPU的时间
    private int CPUTime;
    // 进程状态
    private status status;
    // 优先数时间片数
    private int TimeSlicesPriority;
    public  enum status{RUN,WAIT,FINISH}

    // 设置时间片数产生的范围
    private static final int MIN_TS = 1;
    private static final int MAX_TS = 5;

    PCB(){
        // 随机选择需要的时间片
        timeslicesInNeed = returnRandom();
        // 随机选择PCB的优先级
        setTimeSlicesPriority(returnRandom());
        // 设置状态
        status = status.WAIT;
    }


    public static void main(String[] args) {
        PCB pcb = new PCB();
        pcb.outPut(pcb.getTimeslicesInNeed());
    }

    void setTimeSlicesPriority(int timeSlicesPriority) {
        this.TimeSlicesPriority = timeSlicesPriority;
    }
    int getTimeSlicesPriority() {
        return TimeSlicesPriority;
    }
    PCB.status getStatus() {
        return status;
    }

    int getCPUTime() {
        return CPUTime;
    }

    void setCPUTime(int CPUTime) {
        this.CPUTime = CPUTime;
    }

    int getIndexProcess() {
        return indexProcess;
    }

    int getTimeslicesInNeed() {
        return timeslicesInNeed;
    }

    void setStatus(PCB.status status) {
        this.status = status;
    }

    void setIndexProcess(int indexProcess) {
        this.indexProcess = indexProcess;
    }

    void setTimeslicesInNeed(int timeslicesInNeed) {
        this.timeslicesInNeed = timeslicesInNeed;
    }

    private int returnRandom(){
        return (int)(Math.random()*(MAX_TS-MIN_TS)+MIN_TS);
    }

    private void outPut(Object object){
        System.out.println(object);
    }

}

class PCBDeadLock extends PCB{
    protected boolean ADVANCE = true;
    // 对资源的最大需求量
    protected int vpMaxClam;
    // 已分配到的资源数
    protected int valLocation;
    // 进程状态(true -> 未结束)
    protected boolean vpAdvance;
    // 请求次数计数器
    protected int vCount;

    int getVpMaxClam() {
        return vpMaxClam;
    }

    void setVpMaxClam(int vpMaxClam) {
        this.vpMaxClam = vpMaxClam;
    }

    /***
     * @return 已分配到的资源数
     */
    int getValLocation() {
        return valLocation;
    }
    /***
     * 设置需要分配资源数
     */
    void setValLocation(int valLocation) {
        this.valLocation = valLocation;
    }
    /***
     * @return 进程继续？
     */
    boolean getVpAdvance() {
        return vpAdvance;
    }
    /***
     * 设置进程状态，true -> 继续
     */
    void setVpAdvance(boolean vpAdvance) {
        this.vpAdvance = vpAdvance;
    }

    /***
     * 请求次数计数器
     * @return 次数
     */
    public int getvCount() {
        return vCount;
    }

    public void setvCount(int vCount) {
        this.vCount = vCount;
    }

}

