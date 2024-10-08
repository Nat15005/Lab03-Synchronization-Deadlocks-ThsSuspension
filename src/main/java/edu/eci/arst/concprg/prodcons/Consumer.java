/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Queue;

/**
 *
 * @author hcadavid
 */
public class Consumer extends Thread{
    
    private Queue<Integer> queue;
    
    
    public Consumer(Queue<Integer> queue){
        this.queue=queue;        
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                while (queue.isEmpty()) {
                    synchronized (queue) {
                        queue.wait();
                    }
                }
                synchronized (queue) {
                    int elem = queue.poll();
                    System.out.println("Consumer consumes " + elem);
                    queue.notifyAll();
                }
                Thread.sleep(3000);
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
