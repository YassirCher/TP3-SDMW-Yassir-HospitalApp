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
- Interface utilisateur avec Thymeleaf et Bootstrap pour un design responsive.
- Ajout d'une page d'erreur (non présente dans les vidéos

### Partie 2 : Template et validation
Selon les instructions de la vidéo 2 :
Page template : Templates Thymeleaf (patients.html, formPatients.html, editPatient.html, login.html, notAuthorized.html) pour une interface cohérente.

Validation des formulaires : Validation des données des patients (par exemple, nom non vide, format de date correct) avec affichage des erreurs via BindingResult.

Ajout et modification de patients via des formulaires sécurisés.

### Partie 3 : Sécurité avec Spring Security

La sécurité est implémentée selon les trois approches décrites dans les vidéos :

- InMemory Authentication (vidéo 3) : Utilisateurs codés en dur (user1, user2, admin) avec rôles USER et ADMIN.
- JDBC Authentication (vidéo 4) : Utilisateurs stockés dans une base de données MySQL avec les tables users et authorities définies dans schema.sql.
- UserDetailsService (vidéo 5) : Implémentation personnalisée via UserDetailServiceImpl pour charger les utilisateurs depuis la table AppUser.

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

![image](https://github.com/user-attachments/assets/a4647362-ca80-46a7-a500-c67a19833873)

---
## Explication du code :
### Configuration de la sécurité (SecurityConfig.java)

Le fichier SecurityConfig.java configure Spring Security avec trois approches :

- InMemory Authentication (commentée) : Définit des utilisateurs statiques (user1, user2, admin) avec des mots de passe hachés via BCryptPasswordEncoder.
  
![image](https://github.com/user-attachments/assets/39dfb258-b5b6-4f63-8dd7-81f75c2bafe3)

- JDBC Authentication (commentée) : Utilise les tables users et authorities définies dans schema.sql pour stocker les utilisateurs.

![image](https://github.com/user-attachments/assets/68ece76b-922d-4e3f-b091-458fef196536)


* schema.sql:
  
<pre><code> create table IF NOT EXISTS users( username varchar(50) not null primary key, password varchar(500) not null, enabled boolean not null ); 
  create table IF NOT EXISTS authorities ( username varchar(50) not null, authority varchar(50) not null, constraint fk_authorities_users foreign key(username) references users(username) ); 
  create unique index IF NOT EXISTS ix_auth_username on authorities (username, authority); </code></pre>
- UserDetailsService (active) : Utilise UserDetailServiceImpl pour charger les utilisateurs depuis la table AppUser via AccountService.
- 
  ![image](https://github.com/user-attachments/assets/5fb5e859-7033-428d-9dff-1bf750b87bb7)


### Configuration du controlleur (PatientController.java) :

![image](https://github.com/user-attachments/assets/4758adf3-64fd-4fcb-8a65-7c65d57ea21a)


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

![image](https://github.com/user-attachments/assets/258bf8d9-beeb-4ffc-881f-7f64bd326154)


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

---

# Tests et captures d'écran

## Affichage des patients avec pagination :
localhost:8086 nous rend vers la page login

![image](https://github.com/user-attachments/assets/c940a833-3311-4658-8f2c-261839f2edfa)

- Role Utilisateur :
 username : user2
 pwd : 1234
 ![image](https://github.com/user-attachments/assets/9dced65f-69e3-4685-af35-6941dbfa1cbe)


 Comme user2 n'a pas le role ADMIN il ne peux pas supprimer ou modifier un patient,mais il peux effectuer une recherche
 ![image](https://github.com/user-attachments/assets/ec13ab98-bd82-4ada-ab1b-6ae140df3511)
 ![image](https://github.com/user-attachments/assets/476c4252-7437-4a52-a4b2-76d827b0c4ec)
 ![image](https://github.com/user-attachments/assets/b80166ac-54ce-4a64-921e-dc9a55f3a411)

 
 il peut aussi se deconnecter :

 ![image](https://github.com/user-attachments/assets/f5021e9d-916e-4e9f-948e-9255498003f6)

- Role Admin :
  username : admin
  pwd : 1234
 
  ![image](https://github.com/user-attachments/assets/c4f8a798-d3cc-44c5-817b-9ba9b3a67bd5)

  L’administrateur peut effectuer toutes les fonctionnalités de l’utilisateur. En plus de cela, il peut supprimer, modifier ou ajouter un patient.
  
  - Critere de validation des formulaires:
  Le nom doit comporter au moins 3 caractères, et le score doit être supérieur à 5.
  
  Ajout d'un nouvel patient :
  ![image](https://github.com/user-attachments/assets/e5a63114-09ba-45a6-b41d-ab864394dce8)
  ![image](https://github.com/user-attachments/assets/0af4cedf-6ac0-415b-bb36-2aa2fea895f2)



  modification d'un patient :
  ![image](https://github.com/user-attachments/assets/788e30c1-ca21-481b-a03a-de4d20ee795f)
  ![image](https://github.com/user-attachments/assets/96e851ed-4040-493d-b167-48ab9e162456)

  supression d'un patient :

  ![image](https://github.com/user-attachments/assets/4ca98b2c-fd0f-41e4-95f0-8670d5ea4e1d)
  ![image](https://github.com/user-attachments/assets/859cee49-d4d6-411a-9db0-50c704315e80)



  Si un utilisateur essaie d’exécuter une requête à laquelle il n’a pas le droit d’accéder, il reçoit un avertissement.
  
  ![image](https://github.com/user-attachments/assets/b0ee3732-59e3-4b35-b587-c64aea985744)


  Comme vous pouvez le voir ici, l’utilisateur user2, ayant uniquement le rôle user, a essayé de supprimer un utilisateur via l’URL : http://localhost:8086/admin/delete?id=34&keyword=yassir&page=0, mais il n’a pas pu.
  Il existe aussi user1, avec le mot de passe 1234, à qui le rôle d’administrateur a été attribué depuis la base de données. Il peut effectuer les mêmes fonctionnalités qu’un administrateur. Cependant, lors du premier 
  lancement du projet, il n’aura que le rôle user.

  ### Pour pouvoir créer des patients, il faut décommenter cette partie du code.

  ![image](https://github.com/user-attachments/assets/c073ee5e-265c-428c-bec9-7269a190deab)

  Lors du lancement du projet, vous obtiendrez les tables suivantes :

  DB Link : http://localhost/phpmyadmin/index.php?route=/database/structure&db=yassir-hospital-db

  ![image](https://github.com/user-attachments/assets/5c729c13-b693-4909-a0d6-e93e808337ac)

 Une page d'erreur s'affiche si l'utilisateur essaie d'accéder à un lien erroné.

  ![image](https://github.com/user-attachments/assets/6e617385-b776-458c-89e2-a621adb72b9b)

---

# Conclusion

Ce projet répond aux exigences du TP3 en implémentant une application web complète avec Spring Boot, Thymeleaf, et Spring Security. Les fonctionnalités de gestion des patients (affichage, pagination, recherche, suppression, ajout, modification) et la sécurité (authentification InMemory, JDBC, et UserDetailsService) ont été soigneusement implémentées. La classe AccountServiceImpl permet une gestion efficace des utilisateurs et des rôles, avec des fonctionnalités de création et de suppression. Les templates Thymeleaf assurent une interface utilisateur cohérente et responsive. Les tests, y compris le dépannage de l'erreur 404 pour /admin/save, confirment le bon fonctionnement de l'application.

