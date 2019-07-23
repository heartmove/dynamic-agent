package com.heartmove;

import static org.junit.Assert.assertTrue;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.junit.Test;

import java.io.IOException;

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
        vm.loadAgent("F:\\git_code\\dynamic-agent\\agent\\target\\agent-1.0.0-SNAPSHOT-jar-with-dependencies.jar");
        Thread.sleep(100000000);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    vm.detach();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
