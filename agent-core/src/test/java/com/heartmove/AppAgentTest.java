package com.heartmove;

import static org.junit.Assert.assertTrue;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.junit.Test;

import java.io.IOException;
import java.util.Scanner;

/**
 * Unit test for simple AppAgent.
 */
public class AppAgentTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws Exception{
        String pid = null;
        for(VirtualMachineDescriptor descriptor : VirtualMachine.list()){
            if(descriptor.displayName().contains("Application")){
                pid = descriptor.id();
            }
        }
        System.out.println("pid:" + pid);
        final VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent("F:\\github_code\\dynamic-agent\\agent\\target\\agent-1.0.0-SNAPSHOT-jar-with-dependencies.jar");

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    System.out.println("vm detach");
                    vm.detach();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        while(true){
            String command = new Scanner(System.in).nextLine();
            if(command.equals("exit")){
                vm.detach();
                System.exit(1);
            }
        }
    }
}
