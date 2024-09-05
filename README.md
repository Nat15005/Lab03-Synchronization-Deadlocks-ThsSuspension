
## Escuela Colombiana de Ingeniería
### Arquitecturas de Software – ARSW


#### Ejercicio – programación concurrente, condiciones de carrera y sincronización de hilos. EJERCICIO INDIVIDUAL O EN PAREJAS.

##### Parte I – Antes de terminar la clase.

Control de hilos con wait/notify. Productor/consumidor.

1. Revise el funcionamiento del programa y ejecútelo. Mientras esto ocurren, ejecute jVisualVM y revise el consumo de CPU del proceso correspondiente. A qué se debe este consumo?, cual es la clase responsable?

   ![image](https://github.com/user-attachments/assets/12f3a084-a100-4677-9f8d-eee85208b514)

   La clase responsable del consumo en este proceso es "Consumer". Esto debido a que su ejecución constante se encuentra dentro de un "while(true)", condición que nunca cambia en el momento de la ejecución. También, no hay una espera dentro del proceso, lo que implica que se ejecute todo el tiempo asi no tenga elementos en la lista.

2. Haga los ajustes necesarios para que la solución use más eficientemente la CPU, teniendo en cuenta que -por ahora- la producción es lenta y el consumo es rápido. Verifique con JVisualVM que el consumo de CPU se reduzca.

	Necesitamos que Consumer se ejecute solo cuando haya algún elemento en la lista, para esto pondremos a dormir los hilos de Consumer siempre que este vacía la lista, asi evitaremos el consumo innecesario de CPU y haremos el programa más eficiente. Producer avisará cuando haya algo en la lista y despetará los hilos de Consumer.

	![image](https://github.com/user-attachments/assets/38fd66e9-25f2-4ad8-b061-d9d3cda2dec7)

	

4. Haga que ahora el productor produzca muy rápido, y el consumidor consuma lento. Teniendo en cuenta que el productor conoce un límite de Stock (cuantos elementos debería tener, a lo sumo en la cola), haga que dicho límite se respete. Revise el API de la colección usada como cola para ver cómo garantizar que dicho límite no se supere. Verifique que, al poner un límite pequeño para el 'stock', no haya consumo alto de CPU ni errores.
   
	![image](https://github.com/user-attachments/assets/12503417-c1e1-458b-980f-c576e28babb6)



##### Parte II. – Antes de terminar la clase.

Teniendo en cuenta los conceptos vistos de condición de carrera y sincronización, haga una nueva versión -más eficiente- del ejercicio anterior (el buscador de listas negras). En la versión actual, cada hilo se encarga de revisar el host en la totalidad del subconjunto de servidores que le corresponde, de manera que en conjunto se están explorando la totalidad de servidores. Teniendo esto en cuenta, haga que:

- La búsqueda distribuida se detenga (deje de buscar en las listas negras restantes) y retorne la respuesta apenas, en su conjunto, los hilos hayan detectado el número de ocurrencias requerido que determina si un host es confiable o no (_BLACK_LIST_ALARM_COUNT_).
- Lo anterior, garantizando que no se den condiciones de carrera.

	Vamos a implementar los conceptos vistos en clase. Para esto, identificaremos las zonas críticas y posibles zonas de mejora.

	![image](https://github.com/user-attachments/assets/81c53277-d4a3-4ce9-9ed0-d4e0dd639cd2)

	Los contadores ahora los tratamos como Atomic Integers, la zona crítica e indispensable de sincronizar es cuando encuentra la IP en un servidor, entonces agregamos el synchronized.
	Evidenciamos la mejoría puesto que ahora el programa hace su ejecución más rápida y utiliza mejor los recursos del computador.

	![image](https://github.com/user-attachments/assets/d767c82a-6143-452b-aecf-c79d7f90ad64)

	Finalmente para garantizar que no siga buscando cuando ya llegó a las ocurrencias dadas, hacemos uso de una bandera atómica para detener la búsqueda globalmente. Prevenimos tener condiciones de carrera al hacer uso de operaciones atómicas y bloques de sincronización.

	![image](https://github.com/user-attachments/assets/59c01eb7-1917-4567-becc-1a345fdde8f4)


##### Parte III. – Avance para el martes, antes de clase.

Sincronización y Dead-Locks.

![](http://files.explosm.net/comics/Matt/Bummed-forever.png)

1. Revise el programa “highlander-simulator”, dispuesto en el paquete edu.eci.arsw.highlandersim. Este es un juego en el que:

	* Se tienen N jugadores inmortales.
	* Cada jugador conoce a los N-1 jugador restantes.
	* Cada jugador, permanentemente, ataca a algún otro inmortal. El que primero ataca le resta M puntos de vida a su contrincante, y aumenta en esta misma cantidad sus propios puntos de vida.
	* El juego podría nunca tener un único ganador. Lo más probable es que al final sólo queden dos, peleando indefinidamente quitando y sumando puntos de vida.

2. Revise el código e identifique cómo se implemento la funcionalidad antes indicada. Dada la intención del juego, un invariante debería ser que la sumatoria de los puntos de vida de todos los jugadores siempre sea el mismo(claro está, en un instante de tiempo en el que no esté en proceso una operación de incremento/reducción de tiempo). Para este caso, para N jugadores, cual debería ser este valor?.

	```Como el valor de vida inicial de cada inmortal es H=100 y todos comienzan con la misma cantidad de vida, además de que no hay pérdida de vida (es constante el total de vida), el invariante sería N x H. Este es el valor que se debe mantener a lo largo del juego```

3. Ejecute la aplicación y verifique cómo funcionan las opción ‘pause and check’. Se cumple el invariante?.

	![image](https://github.com/user-attachments/assets/6373bd1b-ba67-4859-95ee-49ef531a0e22)
	![image](https://github.com/user-attachments/assets/5729c6cc-91a1-4560-a120-4eca605f0eb0)
	![image](https://github.com/user-attachments/assets/0fdd5d73-9888-4d50-b294-9413c1e6bd2c)

	La invariante no se está cumpliendo, ya que el valor de vida total no está siendo constante

4. Una primera hipótesis para que se presente la condición de carrera para dicha función (pause and check), es que el programa consulta la lista cuyos valores va a imprimir, a la vez que otros hilos modifican sus valores. Para corregir esto, haga lo que sea necesario para que efectivamente, antes de imprimir los resultados actuales, se pausen todos los demás hilos. Adicionalmente, implemente la opción ‘resume’.

	- Primero definimos una banderaa y el monitor que utilizaremos para la sincronización
	![image](https://github.com/user-attachments/assets/09d1f149-7f1a-480d-b728-26a09f580c55)

	- Creamos los métodos necesarios para pausar y resumir el programa
		- pauseImmortal(), pone paused en true.
		- resumeImmortal(), pone paused en false y notifica a todos los hilos en espera
  		- checkPaused() pone en pausa al hilo cuando detecta que la bandera paused está activada.

		![image](https://github.com/user-attachments/assets/b82f9406-5780-4c49-b768-feb06eafd6eb)

	- Actualizamos los botones en ControlFrame
		![image](https://github.com/user-attachments/assets/87c114ce-5f87-4032-8472-2a4faf249bf6)


5. Verifique nuevamente el funcionamiento (haga clic muchas veces en el botón). Se cumple o no el invariante?.

	- Al hacer click muchas veces en el botón vemos que el invariante sigue sin cumplirse.

	![image](https://github.com/user-attachments/assets/0ba38249-e29d-445c-8019-a4c794f5b33f)
	![image](https://github.com/user-attachments/assets/d3e6bf7a-c03c-4705-ae74-5a1391fc0950)
	![image](https://github.com/user-attachments/assets/f457dc63-503a-4d01-bdaa-ac2c07409dee)

6. Identifique posibles regiones críticas en lo que respecta a la pelea de los inmortales. Implemente una estrategia de bloqueo que evite las condiciones de carrera. Recuerde que si usted requiere usar dos o más ‘locks’ simultáneamente, puede usar bloques sincronizados anidados:

	```java
	synchronized(locka){
		synchronized(lockb){
			…
		}
	}
	```

	![image](https://github.com/user-attachments/assets/2e71d38a-1254-4493-890b-32f1cb72596f)


7. Tras implementar su estrategia, ponga a correr su programa, y ponga atención a si éste se llega a detener. Si es así, use los programas jps y jstack para identificar por qué el programa se detuvo.

	![image](https://github.com/user-attachments/assets/db18547c-0cb9-4278-b470-190c60d81f22)
	![image](https://github.com/user-attachments/assets/4aea6d92-b48e-41fd-86f5-c584c0ba7728)
	![image](https://github.com/user-attachments/assets/67b0b9dc-8f3f-4a84-b90d-49210f97186d)


8. Plantee una estrategia para corregir el problema antes identificado (puede revisar de nuevo las páginas 206 y 207 de _Java Concurrency in Practice_).

	![image](https://github.com/user-attachments/assets/5627de1a-0e59-4b65-833b-e8c6650b2a0b)

9. Una vez corregido el problema, rectifique que el programa siga funcionando de manera consistente cuando se ejecutan 100, 1000 o 10000 inmortales. Si en estos casos grandes se empieza a incumplir de nuevo el invariante, debe analizar lo realizado en el paso 4.
   
	Debido a que el invariante está cambiando cada vez que se para y reanuda el programa, evidenciamos que se está presentando condición de carrera. Para arreglar esto, añadimos el método Thread.sleep(100) para garantizar que todos los hilos tengan tiempo suficiente para procesar la pausa antes de calcular y mostrar la suma de la salud, y también añadimos un bloque synchronized en la región critica del botón, donde se asegura que no haya modificaciones concurrentes en la lista de inmortales mientras se itera sobre ella.

	![image](https://github.com/user-attachments/assets/1c116bb8-b57e-46c1-b1da-107c5d598543)

10. Un elemento molesto para la simulación es que en cierto punto de la misma hay pocos 'inmortales' vivos realizando peleas fallidas con 'inmortales' ya muertos. Es necesario ir suprimiendo los inmortales muertos de la simulación a medida que van muriendo. Para esto:
	* Analizando el esquema de funcionamiento de la simulación, esto podría crear una condición de carrera? Implemente la funcionalidad, ejecute la simulación y observe qué problema se presenta cuando hay muchos 'inmortales' en la misma. Escriba sus conclusiones al respecto en el archivo RESPUESTAS.txt.
	* Corrija el problema anterior __SIN hacer uso de sincronización__, pues volver secuencial el acceso a la lista compartida de inmortales haría extremadamente lenta la simulación.

	![image](https://github.com/user-attachments/assets/d8479614-8b5b-49eb-bd93-9fc69d424535)

11. Para finalizar, implemente la opción STOP.
 
	![image](https://github.com/user-attachments/assets/d222d7ff-c3ee-4b36-a9c6-d1eb3f8bb474)

    Implementamos la funcionalidad del botón STOP. Añadimos lo siguiente: cuando se da click en Stop, se activa el botón Start, permitiendo cambiar el número de peleadores e iniciando una nueva pelea.


<!--
### Criterios de evaluación

1. Parte I.
	* Funcional: La simulación de producción/consumidor se ejecuta eficientemente (sin esperas activas).

2. Parte II. (Retomando el laboratorio 1)
	* Se modificó el ejercicio anterior para que los hilos llevaran conjuntamente (compartido) el número de ocurrencias encontradas, y se finalizaran y retornaran el valor en cuanto dicho número de ocurrencias fuera el esperado.
	* Se garantiza que no se den condiciones de carrera modificando el acceso concurrente al valor compartido (número de ocurrencias).


2. Parte III.
	* Diseño:
		- Coordinación de hilos:
			* Para pausar la pelea, se debe lograr que el hilo principal induzca a los otros a que se suspendan a sí mismos. Se debe también tener en cuenta que sólo se debe mostrar la sumatoria de los puntos de vida cuando se asegure que todos los hilos han sido suspendidos.
			* Si para lo anterior se recorre a todo el conjunto de hilos para ver su estado, se evalúa como R, por ser muy ineficiente.
			* Si para lo anterior los hilos manipulan un contador concurrentemente, pero lo hacen sin tener en cuenta que el incremento de un contador no es una operación atómica -es decir, que puede causar una condición de carrera- , se evalúa como R. En este caso se debería sincronizar el acceso, o usar tipos atómicos como AtomicInteger).

		- Consistencia ante la concurrencia
			* Para garantizar la consistencia en la pelea entre dos inmortales, se debe sincronizar el acceso a cualquier otra pelea que involucre a uno, al otro, o a los dos simultáneamente:
			* En los bloques anidados de sincronización requeridos para lo anterior, se debe garantizar que si los mismos locks son usados en dos peleas simultánemante, éstos será usados en el mismo orden para evitar deadlocks.
			* En caso de sincronizar el acceso a la pelea con un LOCK común, se evaluará como M, pues esto hace secuencial todas las peleas.
			* La lista de inmortales debe reducirse en la medida que éstos mueran, pero esta operación debe realizarse SIN sincronización, sino haciendo uso de una colección concurrente (no bloqueante).

	

	* Funcionalidad:
		* Se cumple con el invariante al usar la aplicación con 10, 100 o 1000 hilos.
		* La aplicación puede reanudar y finalizar(stop) su ejecución.
		
		-->

<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />Este contenido hace parte del curso Arquitecturas de Software del programa de Ingeniería de Sistemas de la Escuela Colombiana de Ingeniería, y está licenciado como <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
