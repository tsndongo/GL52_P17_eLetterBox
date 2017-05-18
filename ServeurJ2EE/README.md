Serveur J2E
==


Le dossier serveur J2EE contient tous les dossier responsable de la 
gestion du client REST de l'application.

Pour cela il y a 4 sous projet :


Reservation Service
-

Reservation service est la couche metier de l'application, il doit juste acceder au donne stocker
 sur un serveur mangoDb pourapres renvoyer une liste d'informations.
 Pour ce lancer il vas demander sa configuration aupres de ConfigServiceApplication afin de connaitre sont port.
 Cette fonction peut étre appeler directement en utilsant le port /name
 
 Config Service
 -
 Config Service à pour objectif la centralisation de tous les micro-service :  
  grace a cela nous pourons :
        
* Avoir un seul dossier pour toute les configuration de ce fait il la maintenance sera simple
* Il pouras il y avoir une configuration par default pour tous les micro-service
 
  Cela ce traduit par le fait de donner le port aux autres services ainsi que d'enregistrer les divers application 
 tournant sur le processeur. POur cela il utilise la technlogie Server de spring boot.

Pour acceder au fichier de conf il suffie d'aller sur http://localhost:8888/{nom-service}/master  
Le nom de l'application étant défini dans le fichier bootstrap.properties de chaque micro service avec la directive spring.application.name
 
 Eureka Service
  -
  
  Eureka service permet de creer un service ereka celui permet de localiser les services REST dans le bur de 
  gerer les charge de serveur ainsi que la panne d'un micro service.
  Grace a ce service nous pouvons instancier des client eureka (ici Reservation Service et reservation client)   
  De ce fait, les clients vont pouvoir s’enregistrer auprès du serveur et périodiquement donner des signes de vie.
  Le service Eureka (composant serveur) va pouvoir conserver les informations de localisation desdits clients afin de les mettre à disposition aux autres services (service registry). 
  
Reservation Client
   -
   reservation client est responsable du fait d'envoyer de faire l'interface entre l'exterrieur et les micro application
   pour l'instant il ne posséde que 2 requete
   * http://localhost:{port-definie}/reservations/names permettant de visualier la liste des personnes
   * Une methode post sur http://localhost:{port-defini}/reservations pour inscrire des nouvelle personne.  
   Afin de pouvoir envoyer l'informations, il a été necessaire d'utilisé un message broquer. En effet grace a cette methoe
   nous pouvons étre certain que la donné sera transmise méme si Reservation Service est arreter.
   De ce fait il est necessaire de lance un messageBreker, nous avons choisi rabbitMQ
   qui peut étre télécharger à cette [adresse](https://www.rabbitmq.com).  
   
   Voici la commande pour creer une nouvelle personne avec le nom "Dr who": 
    
     curl -d '{ "reservationName" : "Dr who"}' -H "content-type: application/json" http://localhost:9999/reservations
  
   hystrix dashboard
   -
   Cette application permet de visualiser l'utilisation de nos ressource en temps réel
   Cela permet de diminuer le temps pour découvrir un incident.
  
  zipkin-service
   -
   Zipkin est un service de lecture de log distribué. Cela permet de comprendre les raisons pour lesquel
   nos service peuvent avoir beaucoup de latence. 
   
  auth-service
   -
   
   Auth service est le service responsable de la sécurisation de l'application.
   En effet grace a ce service nous pouvons pas joindre l'API sans token et donc sans autorisation.
   