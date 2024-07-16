# Mangampire Backend Repository - Italian

Questo repository è parte di una serie di microservizi di esempio per il funzionamento di un servizio e-commerce di esempio chiamato Mangampire Store.
<br />

- <strong>manga-storehouse</strong> : microservizio database in grado di gestire Manga e i registri. Nel caso il manga risultasse come genere "Yaoi" o "Yuri", la variabile Restricted verrà assegnata a true, quindi vietato ai minori di 18 anni.<br />
- <strong>clients-transaction</strong> : microservizio database in grado di gestire clienti e carte associate.<br />
- <strong>backend-service</strong> : microservizio principale che gestisce le operazioni di manga-storehouse e clients-transaction. E' in grado inoltre di gestire il lato del carrello del cliente e di completare le transazioni di una carta esistente.<br />

Per utilizzare questo microservizio, occorre effettuare una serie di passaggi :<br />
- <strong>Clona il progetto</strong> con il comando <strong>git clone</strong>.<br />
- <strong>Effettua la build</strong> in una delle sottocartelle con il comando : <i>mvn clean install -DskipTests</i><br />
- <strong>Sostituisci le variabili d'ambiente</strong> presente nei file application.properties delle cartelle clients-transaction e manga-storehouse con i tuoi presenti nel database.<br />

# Mangampire Backend Repository - English 

This repository is part of a series of example microservices for running an example e-commerce service called Mangampire Store.
<br />

- <strong>manga-storehouse</strong> : database microservice that can manage Manga and registers. In case that manga appears as 'Yaoi' or 'Yuri' genre, the Restricted variable will be assigned to true, thus forbidden to minors under 18 years of age.<br />
- <strong>clients-transaction</strong> : database microservice capable of managing customers and associated cards.<br />
- <strong>backend-service</strong> : main micro-service that manages the manga-storehouse and clients-transaction operations. It is also able to manage the shopping cart side of the client and complete transactions of an existing card.

To use this microservice, a series of steps must be performed:<br />
- <strong>Clone this project</strong> with the <strong>git clone</strong> command.<br />
- <strong>Do the build</strong> in one of its subfolders with the <i>mvn clean install -DskipTests</i> command.<br />
- <strong>Replace environment variables</strong> present in application.properties on of clients-transaction and manga-storehouse folders with your database data.