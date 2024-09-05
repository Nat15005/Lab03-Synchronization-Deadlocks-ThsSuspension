package edu.eci.arsw.highlandersim;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;
    private int defaultDamageValue;
    private final CopyOnWriteArrayList<Immortal> immortalsPopulation;
    private final String name;
    private final Random r = new Random(System.currentTimeMillis());
    private volatile boolean isStillAlive = true; // Atributo para el estado de vida

    // Bandera
    private static volatile boolean paused = false;
    private static final Object pauseLock = new Object();
    private static boolean stopped = false;

    public Immortal(String name, CopyOnWriteArrayList<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
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
            pauseLock.notifyAll();
        }
    }

    private void checkPaused() {
        while (paused && !stopped) {
            try {
                synchronized (pauseLock) {
                    pauseLock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void stopImmortal() {
        synchronized (pauseLock) {
            stopped = true;
            pauseLock.notifyAll(); // Despertar hilos en espera
        }
    }
    public static void resetState() {
        synchronized (pauseLock) {
            paused = false;
            stopped = false;
        }
    }

    public void run() {
        while (isStillAlive && !stopped) { // El inmortal solo pelea si está vivo
            checkPaused();
            if (stopped) break;
            Immortal im;
            int myIndex;
            int nextFighterIndex;

            // Bloqueo para acceder a la lista de inmortales
            synchronized (immortalsPopulation) {
                myIndex = immortalsPopulation.indexOf(this);
                do {
                    nextFighterIndex = r.nextInt(immortalsPopulation.size());
                } while (nextFighterIndex == myIndex || !immortalsPopulation.get(nextFighterIndex).isStillAlive);
                // Asegurarse de no pelear contra uno mismo o un inmortal muerto

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
        Immortal i1 = this;

        // Orden de bloqueo basado en el hashCode
        Immortal firstLock = (i1.hashCode() < i2.hashCode()) ? i1 : i2;
        Immortal secondLock = (i1.hashCode() < i2.hashCode()) ? i2 : i1;

        // Bloqueamos en orden
        synchronized (firstLock) {
            synchronized (secondLock) {
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                } else {
                    // Eliminar el inmortal muerto de la lista de inmortales
                    synchronized (immortalsPopulation) {
                        if (i2.isStillAlive) {
                            i2.markAsDead(); // Marcar el inmortal como muerto
                            immortalsPopulation.remove(i2); // Eliminar el inmortal de la lista
                        }
                    }
                    updateCallback.processReport(this + " says: " + i2 + " is already dead!\n");
                }
            }
        }
    }

    // Método para marcar un inmortal como muerto
    public void markAsDead() {
        this.isStillAlive = false;
    }

    public void changeHealth(int v) {
        health = v;
        if (health <= 0) {
            markAsDead(); // Si la salud es 0 o menos, marcar como muerto
        }
    }

    public int getHealth() {
        return health;
    }

    public boolean isStillAlive() {
        return isStillAlive;
    }

    @Override
    public String toString() {
        return name + "[" + health + "]";
    }
}