/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;



/**
 * Clase que valida si una dirección IP está en listas negras conocidas.
 * Si la IP aparece en al menos un número determinado de listas negras,
 * se considera no confiable.
 * @author hcadavid
 */
public class HostBlackListsValidator {
    // numero de listas negras necesarias para considerar una IP como no confiable
     private static final int BLACK_LIST_ALARM_COUNT=5;
     AtomicBoolean stopSearching = new AtomicBoolean(false);

    /**
     * Método que verifica si la dirección IP dada está en listas negras conocidas.
     * La verificación se realiza en paralelo utilizando múltiples hilos.
     * Si la IP aparece en al menos BLACK_LIST_ALARM_COUNT listas, se reporta como no confiable.
     *
     * @param ipaddress Dirección IP que se va a verificar.
     * @param N Número de hilos que se usarán para dividir la búsqueda.
     * @return Una lista de números de las listas negras donde se encontró la dirección IP.
     * @throws IllegalArgumentException Si N es menor o igual a cero.
     */
    public List<Integer> checkHost(String ipaddress, int N){
        // inicializar la lista que almacenara los numeros de las listas negras donde se encuentra la IP
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();

        // obtener la instancia de HostBlacklistsDataSourceFacade
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();

        // numero total de servidores registrados
        int totalServers = skds.getRegisteredServersCount();

        // verificamos si N es válido
        if (N <= 0) {
            throw new IllegalArgumentException("El número de hilos debe ser mayor que 0");
        }

        // Definimos la cantidad de servidores que cada hilo procesara
        int serversPerThread = totalServers / N; // cada hilo se encargara de un segmento del total de servidores. La variable serversPerThread calcula cuantos servidores revisara cada hilo
        int remainingServers = totalServers % N; // servidores que no se dividen exactamente

        // creamos una lista para almacenar los hilos
        List<BlackListSearchThread> threads = new ArrayList<>();

        AtomicInteger ocurrencesCount = new AtomicInteger(0); // contador de ocurrencias de la IP en listas negras
        AtomicInteger checkedListsCount = new AtomicInteger(0); // contador de listas negras revisadas
        AtomicBoolean stopSearching = new AtomicBoolean(false);

        // crear, iniciar y coordinar múltiples hilos para la busqueda en paralelo
        for (int i = 0; i < N ; i++){ // crea un nuevo hilo por cada iteracion
            int start = i * serversPerThread;
            int end = (i == N - 1) ? totalServers : start + serversPerThread;

            // si hay servidores restantes, añadir uno mas al ultimo hilo
            if (remainingServers > 0 && i == N - 1) {
                end += remainingServers;
            }

            // crear y agregar hilo a la lista
            BlackListSearchThread thread = new BlackListSearchThread(start, end, ipaddress, skds, ocurrencesCount, blackListOcurrences, checkedListsCount, stopSearching);
            threads.add(thread);
            //System.out.println("Starting thread " + thread.getName() + " with range [" + start + ", " + end + ")");

            thread.start(); // iniciamos el hilo
        }

        // esperar a que todos los hilos terminen su ejecucion
        for (BlackListSearchThread thread : threads) {
            try {
                thread.join(); // esperar a que cada hilo termine su trabajo antes de continuar
//                System.out.println("Thread " + thread.getName() + " has completed.");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // si la IP es confiable o no
        if (ocurrencesCount.get()>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }
        // registrar en el log la cantidad de listas revisadas y el total de listas disponibles
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});

        return blackListOcurrences; // retornar la lista de numeros de listas negras donde se encontro la ip
    }
    public static int getBlackListAlarmCount() {
        return BLACK_LIST_ALARM_COUNT;
    }
    // logger
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

}
