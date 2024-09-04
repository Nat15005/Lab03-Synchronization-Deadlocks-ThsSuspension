package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;

    private int health;

    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());
    //bandera
    private static boolean paused = false;
    private static final Object pauseLock = new Object();

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
    }

    public static void pauseImmortal() {
        paused = true;
    }

    public static void resumeImmortal() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Notificar a todos los hilos que pueden continuar
        }
    }

    private void checkPaused() {
        synchronized (pauseLock) {
            while (paused) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            checkPaused();

            Immortal opponent;
            int myIndex = immortalsPopulation.indexOf(this);
            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            if (nextFighterIndex == myIndex) {
                nextFighterIndex = (nextFighterIndex + 1) % immortalsPopulation.size();
            }

            opponent = immortalsPopulation.get(nextFighterIndex);
            this.fight(opponent);

            try {
                Thread.sleep(1);  // Pausa breve
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void fight(Immortal i2) {
        Immortal i1 = this;

        Immortal firstLock = (i1.hashCode() < i2.hashCode()) ? i1 : i2;
        Immortal secondLock = (i1.hashCode() < i2.hashCode()) ? i2 : i1;

        synchronized (firstLock) {
            synchronized (secondLock) {
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + i1 + " vs " + i2 + "\n");
                } else {
                    updateCallback.processReport(i1 + " says: " + i2 + " is already dead!\n");
                }
            }
        }
    }

    public  void changeHealth(int v) {
        health = v;
    }

    public  int getHealth() {
        return health;
    }

    @Override
    public String toString() {
        return name + "[" + health + "]";
    }
}