package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;


public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());
    //bandera
    private AtomicBoolean paused = new AtomicBoolean(false);
    // Monitor para la sincronización
    private final Object pauseLock = new Object();
    // Monitor para la sincronización de peleas
    private final Object fightLock = new Object();


    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
    }

    public void pauseImmortal() {
        paused.set(true);
    }

    public void resumeImmortal() {
        synchronized (pauseLock) {
            paused.set(false);
            pauseLock.notifyAll();
        }
    }
    private void checkPaused() {
            while (paused.get()) {
                try {
                    synchronized (pauseLock) {
                        pauseLock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }

    public void run() {
        while (true) {
            checkPaused();

            Immortal im;
            int myIndex;
            int nextFighterIndex;

            // Bloqueo para acceder a la lista de inmortales
            synchronized (immortalsPopulation) {
                myIndex = immortalsPopulation.indexOf(this);
                nextFighterIndex = r.nextInt(immortalsPopulation.size());

                // Evitar pelea consigo mismo
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = (nextFighterIndex + 1) % immortalsPopulation.size();
                }

                im = immortalsPopulation.get(nextFighterIndex);
            }

            fight(im);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void fight(Immortal i2) {
        Immortal first, second;

        // Determina el orden de los inmortales para evitar deadlock
        if (this.name.compareTo(i2.name) < 0) {
            first = this;
            second = i2;
        } else {
            first = i2;
            second = this;
        }
        // Bloquea los inmortales en orden
        synchronized (first.fightLock) {
            synchronized (second.fightLock) {
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                } else {
                    updateCallback.processReport(this + " says: " + i2 + " is already dead!\n");
                }
            }
        }
    }


    public void changeHealth(int v) {
        synchronized (fightLock) {
            health = v;
        }
    }

    public int getHealth() {
        synchronized (fightLock) {
            return health;
        }
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
