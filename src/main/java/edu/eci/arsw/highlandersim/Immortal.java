package edu.eci.arsw.highlandersim;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback;
    private int health;
    private final int defaultDamageValue;
    private final CopyOnWriteArrayList<Immortal> immortalsPopulation;
    private final String name;
    private final Random random = new Random(System.currentTimeMillis());

    private static volatile boolean paused = false;
    private static final Object pauseLock = new Object();
    private static boolean stopped = false;

    public Immortal(String name, CopyOnWriteArrayList<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
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
        synchronized (pauseLock) {
            while (paused && !stopped) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void stopImmortal() {
        synchronized (pauseLock) {
            stopped = true;
            pauseLock.notifyAll();
        }
    }

    public static void resetState() {
        synchronized (pauseLock) {
            paused = false;
            stopped = false;
        }
    }

    @Override
    public void run() {
        while (health > 0 && !stopped) {
            checkPaused();
            if (stopped) break;

            Immortal opponent = selectOpponent();
            if (opponent != null) {
                fight(opponent);
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Immortal selectOpponent() {
        Immortal opponent = null;
        if (immortalsPopulation.size() > 1) {
            int myIndex = immortalsPopulation.indexOf(this);
            int nextFighterIndex;
            do {
                nextFighterIndex = random.nextInt(immortalsPopulation.size());
            } while (nextFighterIndex == myIndex);

            opponent = immortalsPopulation.get(nextFighterIndex);

        }

        return opponent;
    }

    private void fight(Immortal opponent) {
        Immortal firstLock = (this.hashCode() < opponent.hashCode()) ? this : opponent;
        Immortal secondLock = (this.hashCode() < opponent.hashCode()) ? opponent : this;

        synchronized (firstLock) {
            synchronized (secondLock) {
                if (this.health > 0 && opponent.getHealth() > 0) {
                    opponent.changeHealth(opponent.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + opponent + "\n");

                    if (opponent.getHealth() <= 0) {
                        immortalsPopulation.remove(opponent);
                    }
                } else {
                    updateCallback.processReport(this + " says: " + opponent + " is already dead!\n");
                }
            }
        }
    }

    public void changeHealth(int value) {
        this.health = Math.max(value, 0); // Evita que la salud sea negativa
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {
        return name + "[" + health + "]";
    }
}
