# Digital Banking Application

Application de gestion des clients, des comptes et des opérations bancaires avec interface JavaFX.

## Fonctionnalités

### 1. Gestion des Clients
- Création de nouveaux clients
- Modification des informations client
- Consultation des informations client
- Suppression de clients
- Champs requis : Nom, Prénom, Email

### 2. Gestion des Comptes
- Création de nouveaux comptes
- Association des comptes aux clients
- Consultation des soldes
- Fermeture de comptes
- Informations requises : Numéro de compte, Solde, Date d'ouverture, Client

### 3. Opérations Bancaires
- Dépôts
- Retraits
- Virements entre comptes
- Consultation de l'historique des transactions
- Génération de relevés bancaires en PDF

## Stack Technique
- Backend : Spring Boot
- Frontend : JavaFX
- Base de données : H2
- ORM : JPA/Hibernate
- Gestion des dépendances : Maven
- Version Java : 11

## Prérequis
1. Java JDK 11 ou supérieur
2. Maven 3.6 ou supérieur
3. Un IDE compatible avec JavaFX (IntelliJ IDEA, Eclipse, etc.)

## Installation et Configuration

1. Cloner le projet :
```bash
git clone [url-du-projet]
cd digital-banking
```

2. Compiler le projet avec Maven :
```bash
mvn clean install
```

3. Exécuter l'application :
```bash
mvn spring-boot:run
```

## Structure du Projet

```
digital-banking/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bank/
│   │   │           ├── entities/       # Entités JPA
│   │   │           ├── repositories/   # Repositories Spring Data
│   │   │           ├── services/       # Services métier
│   │   │           └── controllers/    # Contrôleurs JavaFX
│   │   └── resources/
│   │       ├── fxml/                   # Fichiers FXML pour l'interface
│   │       └── styles/                 # Fichiers CSS
│   └── test/                          # Tests unitaires et d'intégration
└── pom.xml                            # Configuration Maven
```

## Base de Données

L'application utilise une base de données H2 (en mémoire par défaut). La configuration se trouve dans `application.properties` :

```properties
spring.datasource.url=jdbc:h2:file:./bankdb
spring.datasource.username=sa
spring.datasource.password=
```

## Utilisation

1. Lancer l'application
2. Utiliser le menu principal pour :
   - Gérer les clients
   - Gérer les comptes
   - Effectuer des opérations bancaires
   - Générer des relevés

## Génération de Relevés

Les relevés bancaires sont générés au format PDF et incluent :
- Informations du client
- Détails du compte
- Historique des transactions
- Solde actuel

## Sécurité

L'application implémente des mesures de sécurité basiques :
- Validation des données
- Gestion des transactions
- Contrôle des accès aux opérations

## Support

Pour toute question ou problème :
1. Consulter la documentation
2. Vérifier les logs d'application
3. Contacter l'équipe de développement

## Licence

Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.
