import org.junit.jupiter.api.Test;

public class AlgTest {
    @Test
    void Test_1_1(){
        Sheduler.PCBScheduler scheduler=new Sheduler.PCBScheduler();
        scheduler.createPCB(3);
        Sheduler.PCBScheduler.RR p = scheduler.new RR();
        while(!scheduler.getReadyQueue().isEmpty()){
            p.update();
        }
    }
    @Test
    void Test_1_2(){
        Sheduler.PCBScheduler scheduler=new Sheduler.PCBScheduler();
        scheduler.createPCB(4);
        Sheduler.PCBScheduler.Priority p = scheduler.new Priority();
        while(!scheduler.getReadyQueue().isEmpty()){
            p.update();
        }
    }
    @Test
    void Test_2_1(){
        // 无竞争
        Sheduler.PCBScheduler.BankerAlgorithm algorithm= new Sheduler.PCBScheduler.BankerAlgorithm(6);
    }

    @Test
    void Test_2_2(){
        // 无法完成资源分配
        Sheduler.PCBScheduler.remainder = 10;
        Sheduler.PCBScheduler.BankerAlgorithm algorithm= new Sheduler.PCBScheduler.BankerAlgorithm(10);
    }

    @Test
    void Test_2_3(){
        // 让竞争更激烈,且无法成功
        Sheduler.PCBScheduler.remainder = 6;
        int[] vClaim = {10,8,9};
        int[] vAlc = {0,0,3};
        Sheduler.PCBScheduler.BankerAlgorithm algorithm= new Sheduler.PCBScheduler.BankerAlgorithm(vClaim,vAlc);
    }
    @Test
    void Test_2_4(){
        // 让竞争更激烈,且成功
        Sheduler.PCBScheduler.remainder = 6;
        int[] vClaim = {10,18,8,10};
        int[] vAlc = {3,9,7,3};
        Sheduler.PCBScheduler.BankerAlgorithm algorithm= new Sheduler.PCBScheduler.BankerAlgorithm(vClaim,vAlc);
    }

    @Test
    void Test_3_1(){
        PageManager pageManager = new PageManager(100);
        System.out.println("OPT:");
        pageManager.OPT();
        System.out.println("LRU:");
        pageManager.LRU();
        System.out.println("FIFO:");
        pageManager.FIFO();
        System.out.println("LFU:");
        pageManager.LFU();
    }
    @Test
    void Test_3_2(){
        // large number (不爆int)
        PageManager pageManager = new PageManager(600000);
        System.out.println("OPT:");
        pageManager.OPT();
        System.out.println("LRU:");
        pageManager.LRU();
        System.out.println("FIFO:");
        pageManager.FIFO();
        System.out.println("LFU:");
        pageManager.LFU();
    }
    @Test
    void Test_3_3(){
        //页表长度参数影响
        PageManager pageManager = new PageManager(50000);
        pageManager.setParameters(32,5,5);
        System.out.println("OPT:");
        pageManager.OPT();
        System.out.println("LRU:");
        pageManager.LRU();
        System.out.println("FIFO:");
        pageManager.FIFO();
        System.out.println("LFU:");
        pageManager.LFU();
        System.out.println("页表长度翻倍");
        pageManager = new PageManager(50000);
        pageManager.setParameters(32,5,10);
        System.out.println("OPT:");
        pageManager.OPT();
        System.out.println("LRU:");
        pageManager.LRU();
        System.out.println("FIFO:");
        pageManager.FIFO();
        System.out.println("LFU:");
        pageManager.LFU();
    }
    @Test
    void Test_3_4(){
        //页面尺寸参数影响
        PageManager pageManager = new PageManager(50000);
        pageManager.setParameters(32,5,5);
        System.out.println("OPT:");
        pageManager.OPT();
        System.out.println("LRU:");
        pageManager.LRU();
        System.out.println("FIFO:");
        pageManager.FIFO();
        System.out.println("LFU:");
        pageManager.LFU();
        System.out.println("页面尺寸翻倍");
        pageManager = new PageManager(50000);
        pageManager.setParameters(32,10,5);
        System.out.println("OPT:");
        pageManager.OPT();
        System.out.println("LRU:");
        pageManager.LRU();
        System.out.println("FIFO:");
        pageManager.FIFO();
        System.out.println("LFU:");
        pageManager.LFU();
    }

}
