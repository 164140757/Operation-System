import com.beust.jcommander.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * http://jcommander.org/
 * java commander
 */
public class Shell {

    // use List catch parameters
    @Parameter(names = {"ls"})
    List<String> ls;
    @Parameter(names = {"-help", "-h"})
    boolean help;
    @Parameter(names = {"cat"})
    List<String> cat;

    @Parameter(names = {"-s","schedule"},description = "process scheduling:" +
            "number of pcb, algorithm: {RR,Priority}")
    List<String> schedule;
    @Parameter(names = {"-alg","algorithm"})
    List<String> alg;

    @Parameter(names = {"-b","banker"},description = "banker to prevent deadLock:"
            +"int current resources ,int[] claimed, int[] allocate")
    List<String> banker;
    @Parameter(names={"-clm"})
    List<Integer> banker_claim;
    @Parameter(names={"-alo"})
    List<Integer> banker_aloc;


    @Parameter(names = {"-p","pageSchedule"},description = "memory page scheduling: " +
            "int instructionNumb,int realMemoryTableNum," + "int pageSize," + "int pageTableCacheSize")
    List<String> page;
    Shell() {

    }


    private Vector<String> lexer(char[] cmd) {
        return null;
    }

    private void pipeExecute(Vector<String> params) {

    }

    private void reDict(Vector<String> params) {

    }

    public void run(JCommander jCommander) {
        if (ls != null && ls.get(0).equals("*")) {
            File[] fileList = getCurrentFileInfo();
            assert fileList != null;
            for (File file : fileList) {
                System.out.printf("%s ", file.getName());
            }
        } else if (ls != null) {
            ls.forEach(e -> {
                        if (e.equals("-l")) {
                            File[] fileList = getCurrentFileInfo();
                            System.out.printf("total %d\n", fileList.length);
                            for (File file : fileList) {
                                Path fp = Paths.get(file.getAbsolutePath());
                                try {
                                    BasicFileAttributes ra = Files.readAttributes(fp, BasicFileAttributes.class);
                                    System.out.println("CreationTime:" + ra.creationTime() + "\t file Size:" + ra.size() + "\t " + file.getName());
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
            );
        }
        if (help) {
            jCommander.usage();
        }
        if (cat != null) {
            try {
                int operationIndex = cat.indexOf(">");
                if (operationIndex != -1) {
                    List<File> fileList = Arrays.asList(getCurrentFileInfo());
                    checkAndOut(cat.get(operationIndex - 1), fileList, cat.get(operationIndex + 1));
                }
            } catch (IndexOutOfBoundsException exp) {
                System.out.println("your grammar is wrong!");
            } catch (FileNotFoundException fexp) {
                System.out.println("target file is a dictionary.");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if(page != null){
            int[] params = getParamsString(page,4);
            PageManager pageManager = new PageManager(params[0]);
            pageManager.setParameters(params[1],params[2],params[3]);
            System.out.println("OPT:");
            pageManager.OPT();
            System.out.println("LRU:");
            pageManager.LRU();
            System.out.println("FIFO:");
            pageManager.FIFO();
            System.out.println("LFU:");
            pageManager.LFU();
        }
        if(banker!=null){
            int[] params = getParamsString(banker,1);
            Sheduler.PCBScheduler.remainder = params[0];
            if(banker_aloc!=null&&banker_claim!=null&&banker_aloc.size()==banker_claim.size()){
                int[] vClaim = getParamsInteger(banker_claim,banker_claim.size());
                int[] vAlc = getParamsInteger(banker_aloc,banker_aloc.size());
                Sheduler.PCBScheduler.BankerAlgorithm algorithm= new Sheduler.PCBScheduler.BankerAlgorithm(vClaim,vAlc);
            }
        }
        if(schedule!=null){
            int[] params = getParamsString(schedule,1);
            if(alg!=null){
                Sheduler.PCBScheduler scheduler=new Sheduler.PCBScheduler();
                scheduler.createPCB(params[0]);
                if(alg.size() == 1 && alg.get(0).equals("Priority")){
                    Sheduler.PCBScheduler.Priority p = scheduler.new Priority();
                    while(!scheduler.getReadyQueue().isEmpty()){
                        p.update();
                    }
                }
                if(alg.size() == 1 && alg.get(0).equals("RR")){
                    Sheduler.PCBScheduler.RR p = scheduler.new RR();
                    while(!scheduler.getReadyQueue().isEmpty()){
                        p.update();
                    }
                }

            }
            else{
                System.out.println("Have no algorithm");
            }
        }

    }

    private int[] getParamsString(List<String> args, int argSize) {
        int[] params = null;
        try{
            params = args.stream().mapToInt(Integer::parseInt).toArray();
            if(params.length!= argSize){
                throw new IllegalArgumentException("arguments wrong!");
            }
        } catch(NumberFormatException nexp){
            System.out.println("please input integer.");
        } catch (IllegalArgumentException exp){
            exp.printStackTrace();
        }
        return params;
    }
    private int[] getParamsInteger(List<Integer> args,int argSize) {
        int[] params = null;
        try{
            params = args.stream().mapToInt(Integer::valueOf).toArray();
            if(params.length!= argSize){
                throw new IllegalArgumentException("arguments wrong!");
            }
        } catch(NumberFormatException nexp){
            System.out.println("please input integer.");
        } catch (IllegalArgumentException exp){
            exp.printStackTrace();
        }
        return params;
    }

    private void checkAndOut(String fileName, List<File> list, String tgtFileName) throws IOException {
        File sourceFile = null;
        boolean tgtFileFound = false;
        // check
        for (File file : list) {
            if (file.getName().equals(fileName)) {
                sourceFile = file;
            }
            if (file.getName().equals(tgtFileName)) {
                tgtFileFound = true;
            }
        }


        if (tgtFileFound) {
            System.out.println("Can not override the existing file.");
        } else {
            assert sourceFile != null;
            Pipe pipe = redirect(new FileInputStream(sourceFile));
            outPutToFile(pipe, tgtFileName);
        }


    }

    private void outPutToFile(Pipe pipe, String tgtFileName) throws IOException {
        Pipe.SourceChannel sourceChannel = pipe.source();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        sourceChannel.read(byteBuffer);
        final FileOutputStream fos = new FileOutputStream(tgtFileName);
        // allocate a channel to write that file
        FileChannel fc = fos.getChannel();
        // flip from filling to emptying to prepare buffer for write
        byteBuffer.flip();
        fc.write(byteBuffer);
    }

    private Pipe redirect(InputStream in) throws IOException {
        Pipe pipe = Pipe.open();
        Pipe.SinkChannel sinkChannel = pipe.sink();
        byte[] bytes = new byte[1024];// 1MB/time
        while ((in.read(bytes)) != -1) {
            sinkChannel.write(ByteBuffer.wrap(bytes));
        }
        return pipe;
    }


    private File[] getCurrentFileInfo() {
        // current location
        String path = System.getProperty("user.dir");
        // folder
        File folder = new File(path);
        // files
        return folder.listFiles();
    }



    public static void main(String[] args) {
        Shell shell = new Shell();
        JCommander jCommander = JCommander.newBuilder().addObject(shell).build();
//        String[] test = {"ls"};
//        jCommander.parse(test);
        jCommander.parse(args);
        shell.run(jCommander);
    }
}
