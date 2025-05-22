# Gestion des Patients - Application Spring Boot 

Server port : 8086
MySql DB port  :3308

## Introduction

Ce projet est une application web développée avec Spring Boot, Spring MVC, Thymeleaf, Spring Data JPA, et Spring Security dans le cadre du TP3 du module SDMW. 
L'application permet de gérer une liste de patients dans un hôpital avec des fonctionnalités telles que l'affichage, la pagination, la recherche, la suppression, l'ajout, la modification, et la validation des formulaires, tout en sécurisant les accès via Spring Security. 
Ce rapport décrit les fonctionnalités implémentées, l'architecture du projet, les explications du code, et les tests effectués.
--- 
## Technologies

Java 17 et Spring Boot 3.4.5

Spring MVC pour le pattern MVC et le routage

Spring Data JPA pour l’accès aux données avec Hibernate

Thymeleaf pour le templating côté serveur

Spring Security pour l’authentification et l’autorisation

MySQL comme base de données relationnelle

Bootstrap  pour le design des pages


## Fonctionnalités

### Partie 1 : Gestion des patients

Conformément aux exigences de la vidéo 1, l'application permet :

Affichage des patients : Liste les patients avec leurs informations (nom, date de naissance, malade, score) via /user/index.

Pagination : Implémentée avec 4 patients par page, utilisant Spring Data JPA (PageRequest).
Recherche de patients : Recherche par nom via le paramètre keyword dans l'URL.
Suppression d'un patient : Réservée aux utilisateurs avec le rôle ADMIN via /admin/delete.
Améliorations supplémentaires :
Interface utilisateur avec Thymeleaf et Bootstrap pour un design responsive.
Ajout d'une page d'erreur (non présente dans les vidéos

### Partie 2 : Template et validation
Selon les instructions de la vidéo 2 :
Page template : Templates Thymeleaf (patients.html, formPatients.html, editPatient.html, login.html, notAuthorized.html) pour une interface cohérente.
Validation des formulaires : Validation des données des patients (par exemple, nom non vide, format de date correct) avec affichage des erreurs via BindingResult.
Ajout et modification de patients via des formulaires sécurisés.

### Partie 3 : Sécurité avec Spring Security

La sécurité est implémentée selon les trois approches décrites dans les vidéos :

InMemory Authentication (vidéo 3) : Utilisateurs codés en dur (user1, user2, admin) avec rôles USER et ADMIN.
JDBC Authentication (vidéo 4) : Utilisateurs stockés dans une base de données MySQL avec les tables users et authorities définies dans schema.sql.
UserDetailsService (vidéo 5) : Implémentation personnalisée via UserDetailServiceImpl pour charger les utilisateurs depuis la table AppUser.

Les fonctionnalités de sécurité incluent :
- Protection des routes (ex. /admin/delete et /admin/save réservés aux ADMIN).
- Page de connexion personnalisée (/login) et page d'accès refusé (/notAuthorized).
- Option "Remember Me" pour la persistance des sessions.
- Hachage des mots de passe avec BCryptPasswordEncoder.
---
# Architecture du projet

Le projet suit une architecture MVC avec :

Modèle : Entités Patient, AppUser, et AppRole pour gérer les patients et les utilisateurs...
Vue : Templates Thymeleaf dans src/main/resources/templates.
Contrôleur : PatientController pour les opérations CRUD et SecurityController pour la gestion des pages de sécurité.
Base de données : MySQL pour la production, avec H2 comme alternative pour le développement.

![image](https://github.com/user-attachments/assets/4f9fac7b-7a61-4678-981a-b719ca335378)
---
## Explication du code :
### Configuration de la sécurité (SecurityConfig.java)

Le fichier SecurityConfig.java configure Spring Security avec trois approches :

- InMemory Authentication (commentée) : Définit des utilisateurs statiques (user1, user2, admin) avec des mots de passe hachés via BCryptPasswordEncoder.
![image](https://github.com/user-attachments/assets/6dae64bc-f604-4052-9bbc-fd1424b6e712)
- JDBC Authentication (commentée) : Utilise les tables users et authorities définies dans schema.sql pour stocker les utilisateurs.
![image](https://github.com/user-attachments/assets/9bc65c10-fb29-4e13-a706-508e0969a5c2)

* schema.sql:
  
<pre><code> create table IF NOT EXISTS users( username varchar(50) not null primary key, password varchar(500) not null, enabled boolean not null ); 
  create table IF NOT EXISTS authorities ( username varchar(50) not null, authority varchar(50) not null, constraint fk_authorities_users foreign key(username) references users(username) ); 
  create unique index IF NOT EXISTS ix_auth_username on authorities (username, authority); </code></pre>
- UserDetailsService (active) : Utilise UserDetailServiceImpl pour charger les utilisateurs depuis la table AppUser via AccountService.
  ![image](https://github.com/user-attachments/assets/5a5b88d3-f1bf-4925-acee-dbfd0f97bd78)

### Configuration du controlleur (PatientController.java) :
![image](https://github.com/user-attachments/assets/6a6ab0a3-1d8b-455d-8961-2723c3e8af84)

* Fonction index(...) :
#### But :
Afficher une liste paginée de patients en filtrant par un mot-clé (nom du patient).
#### Paramètres :
Model model : sert à passer les données à la vue (patients.html).
int p (page) : indique le numéro de la page actuelle (par défaut 0).
int s (size) : nombre d’éléments à afficher par page (par défaut 4).
String kw (keyword) : mot-clé pour filtrer les patients par nom (par défaut chaîne vide).

#### Ce que fait la fonction :

Récupère une page de patients dont le nom contient le mot-clé donné.

Ajoute au modèle la liste des patients, les infos de pagination, la page courante, et le mot-clé.

Retourne la vue HTML correspondante à la liste.

* Fonction delete(...) :
#### But :
Supprimer un patient à partir de son identifiant, accessible uniquement aux administrateurs.
### Paramètres :
long id : identifiant du patient à supprimer.
String keyword : mot-clé de recherche pour conserver le contexte après suppression.
int page : numéro de page pour rediriger l’utilisateur vers la même page après suppression.
#### Ce que fait la fonction :
Supprime le patient avec l’ID donné.
Redirige vers la vue de la liste avec les mêmes paramètres de recherche et pagination.
 Il existe d'autres fonctions dans ce fichier !
 
## fichier AccountServiceImpl :
![image](https://github.com/user-attachments/assets/c0a8b81d-85a4-4e71-9ee7-4a2f0a7fe536)

Ce fichier définit l’implémentation du service AccountService, qui gère la gestion des utilisateurs et des rôles dans l’application.
Il est annoté avec :

@Service => pour être reconnu comme un composant métier Spring.

@Transactional => pour assurer que toutes les opérations dans une méthode sont exécutées de manière atomique.
### Fonctions principales:
- addNewUser(...) :
Crée un nouvel utilisateur si le nom d’utilisateur n’existe pas encore et si les mots de passe correspondent. Le mot de passe est crypté avant d’être enregistré.

- addNewRole(...) :
Crée un nouveau rôle s’il n’existe pas déjà dans la base.

- addRoleToUser(...) :
Ajoute un rôle existant à un utilisateur existant.

- removeRoleFromUser(...) :
Retire un rôle existant d’un utilisateur.

- loadUserByUsername(...) :
Récupère un utilisateur par son nom d’utilisateur (utile pour l’authentification).

# Tests et captures d'écran

# Affichage des patients avec pagination :
localhost:8086 nous rend vers la page login
![image](https://github.com/user-attachments/assets/f6d5d63e-4dc9-43c3-a496-f863ac6aa2a0)
- Role Utilisateur :
 username : user2
 pwd : 1234
 ![image](https://github.com/user-attachments/assets/97f69e19-ef5b-4053-89b3-d75bac27854f)

 Comme user2 n'a pas le role ADMIN il ne peux pas supprimer ou modifier un patient,mais il peux effectuer une recherche
 ![image](https://github.com/user-attachments/assets/6b03aedf-af60-421c-a6c0-5b47b8aaab20)
 ![image](https://github.com/user-attachments/assets/1d9a8be5-e411-418a-b62e-90ee19f9875f)
 
 il peut aussi se deconnecter :
 ![image](https://github.com/user-attachments/assets/c5a6afa8-1705-4c63-ab00-40082b7841e2)
- Role Admin :
  username : admin
  pwd : 1234
  ![image](https://github.com/user-attachments/assets/6919c34b-2535-41c4-9cdc-f04e3193c477)
  L’administrateur peut effectuer toutes les fonctionnalités de l’utilisateur. En plus de cela, il peut supprimer, modifier ou ajouter un patient.
  
  ![image](https://github.com/user-attachments/assets/9497d99c-3003-4068-ac7d-ae50068f5e23)
  - Critere de validation des formulaires:
  Le nom doit comporter au moins 3 caractères, et le score doit être supérieur à 5.
  
  Ajout d'un nouvel patient :
  
  ![image](https://github.com/user-attachments/assets/a4921010-a566-404a-83b1-1b7851a6c2bc)
  ![image](https://github.com/user-attachments/assets/8afa3233-9233-4f6a-8362-aed96c30f224)
  ![image](https://github.com/user-attachments/assets/48eda0e1-c7ed-4baa-af90-b632e2144e00)

  modification d'un patient :

  ![image](https://github.com/user-attachments/assets/29c5dee2-a19f-443e-8dd4-0a75a72869c3)
  ![image](https://github.com/user-attachments/assets/05e7c825-f81c-4724-ab3c-20fb9fd9c0f1)
  
  supression d'un patient :

  ![image](https://github.com/user-attachments/assets/7ead5aad-d6e6-4bc1-88ab-07bd4adcaecf)
  ![image](https://github.com/user-attachments/assets/8054e633-2c1d-45ed-812b-6b07dbd49ec9)

  Si un utilisateur essaie d’exécuter une requête à laquelle il n’a pas le droit d’accéder, il reçoit un avertissement.
  
  ![image](https://github.com/user-attachments/assets/f0951b99-e78d-4208-a34f-29c09db6be2c)

  Comme vous pouvez le voir ici, l’utilisateur user2, ayant uniquement le rôle user, a essayé de supprimer un utilisateur via l’URL : http://localhost:8086/admin/delete?id=34&keyword=yassir&page=0, mais il n’a pas pu.
  Il existe aussi user1, avec le mot de passe 1234, à qui le rôle d’administrateur a été attribué depuis la base de données. Il peut effectuer les mêmes fonctionnalités qu’un administrateur. Cependant, lors du premier 
  lancement du projet, il n’aura que le rôle user.

  ### Pour pouvoir créer des patients, il faut décommenter cette partie du code.

  ![image](https://github.com/user-attachments/assets/4b4c6354-2344-4988-b550-9d8ee2aca6ef)

  Lors du lancement du projet, vous obtiendrez les tables suivantes :

  DB Link : http://localhost/phpmyadmin/index.php?route=/database/structure&db=yassir-hospital-db

  ![image](https://github.com/user-attachments/assets/bd74c1d9-8044-4c3f-866b-2aabb2ec4436)

  
# Conclusion

Ce projet répond aux exigences du TP3 en implémentant une application web complète avec Spring Boot, Thymeleaf, et Spring Security. Les fonctionnalités de gestion des patients (affichage, pagination, recherche, suppression, ajout, modification) et la sécurité (authentification InMemory, JDBC, et UserDetailsService) ont été soigneusement implémentées. La classe AccountServiceImpl permet une gestion efficace des utilisateurs et des rôles, avec des fonctionnalités de création et de suppression. Les templates Thymeleaf assurent une interface utilisateur cohérente et responsive. Les tests, y compris le dépannage de l'erreur 404 pour /admin/save, confirment le bon fonctionnement de l'application.







  









