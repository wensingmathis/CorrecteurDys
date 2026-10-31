# Correcteur Dys

Correcteur orthographique et grammatical pour Android, pensé pour écrire en français sans dépendre d'un cloud public ni d'un compte utilisateur.

Le but de Correcteur Dys est simple : corriger les erreurs de manière fiable, lisible et respectueuse de la vie privée. L'application ne transmet pas le texte à Internet. Elle se connecte uniquement à un serveur LanguageTool auto-hébergé sur le réseau local, avec validation stricte des adresses IPv4 privées et sans redirection HTTP.

## Pourquoi ce logiciel ?

- Écriture en français prioritaire
- Aucune publicité, aucun tracking, aucun SDK cloud
- Aucun stockage du texte dans les préférences ou les logs
- Correction locale via un serveur LanguageTool de confiance
- Interface Android simple, claire et accessible
- Conçu pour les utilisateurs qui veulent garder le contrôle de leurs données

## Ce que l'application peut faire

- Correction orthographique
- Correction grammaticale
- Suggestions de conjugaison et ponctuation
- Sélection de texte depuis Android via `ACTION_SEND`
- Travail avec un serveur LanguageTool local sur le réseau domestique ou professionnel
- Respect du principe : le texte reste chez vous autant que possible

## Valeur ajoutée

Correcteur Dys n'est pas un service SaaS. C'est un outil de productivité orienté confidentialité :

- le texte n'est pas envoyé à un domaine public,
- le serveur est explicitement configuré par l'utilisateur,
- la correction repose sur un moteur de référence localisé,
- les données de préférence restent séparées du contenu saisi.

Pour les utilisateurs sensibles au respect de la vie privée, c'est une alternative plus rassurante aux correcteurs en ligne ou aux services centralisés.

## Confidentialité et sécurité

Invariants du projet :

- Aucun texte n'est envoyé à un domaine public,
- le client HTTP n'utilise que des requêtes POST vers une IPv4 privée validée,
- aucune redirection HTTP n'est suivie,
- les textes ne sont ni journalisés ni persistés,
- les préférences sont les seules données sauvegardées,
- aucune analytics, publicité, SDK cloud ni permission de stockage n'est ajoutée,
- les messages d'erreur ne contiennent pas le texte saisi.

## Architecture

- `MainActivity.kt` : écran principal, paramètres d'accessibilité et gestion de `ACTION_SEND`
- `EditorViewModel.kt` : état mémoire, orchestration asynchrone et invalidation des résultats obsolètes
- `LanguageToolClient.kt` : requêtes HTTP POST vers `/v2/check` avec validation stricte de l'adresse locale
- `Correction.kt` : modèles, classification heuristique et application des suggestions par offsets UTF-16

## Stack technique

- Kotlin 2.2.20
- Jetpack Compose
- MVVM
- minSdk 26
- Java 17
- AGP 8.11.1
- Gradle 8.13 via wrapper
- compileSdk / targetSdk 35

## Démarrer le serveur LanguageTool local

1. Télécharger l'archive du serveur LanguageTool depuis le site officiel de l'éditeur.
2. Extraire le paquet sur un ordinateur du réseau local.
3. Créer un fichier `server.properties` si nécessaire.
4. Démarrer le serveur sur un port local, par exemple 8081.
5. Vérifier l'adresse IPv4 privée du PC avec `hostname -I`.
6. Saisir l'URL locale dans l'application, par exemple : `http://192.168.1.100:8081`
7. Vérifier le service local avec :

```sh
curl -d 'language=fr' --data-urlencode 'text=Je sui content.' http://127.0.0.1:8081/v2/check
```

Important : le réseau local ne garantit pas le chiffrement. Le serveur doit être géré par l'utilisateur ou l'administrateur du réseau, et ne doit pas être exposé à Internet.

## Installation de l'application

### Build local

Prérequis : JDK 17 et Android SDK avec Android 35 et les outils associés.

À la racine du projet :

```sh
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Sous Windows, remplacer `./gradlew` par `gradlew.bat`.

### APK via GitHub Actions

Chaque push sur la branche principale et chaque exécution manuelle déclenchent :

- résolution des dépendances,
- tests unitaires,
- compilation debug,
- vérification de la présence de l'APK,
- upload de l'artefact.

Dans GitHub → Actions, ouvrir une exécution réussie et télécharger l'artefact `CorrecteurDys-debug`. Ensuite :

1. extraire `app-debug.apk`,
2. transférer l'APK sur le téléphone,
3. ouvrir le fichier et autoriser l'installation.

## État du projet

Version actuelle : v0.1.0.

Les tests unitaires et la compilation debug ont été validés sur GitHub Actions. L'application est fonctionnelle comme prototype technique et démonstrateur de principe orienté confidentialité.

## Limites actuelles

- aucune test UI / instrumentation n'est présent,
- aucun test sur appareil / émulateur n'est présent,
- la classification orthographe / grammaire / conjugaison / ponctuation reste heuristique,
- le transport local HTTP n'est pas chiffré,
- les catégories LanguageTool, les collisions d'offsets et les réglages avancés restent à améliorer,
- OpenDyslexic, certificat épinglé, authentification serveur et renforcements de sécurité sont des axes de développement à venir.

## Roadmap

### À court terme
- améliorations de la correspondance des catégories LanguageTool,
- meilleure gestion des langues et des options de langue,
- tests unitaires sur les transformations et l'intégration d'hôtes publics,
- validation des cas de collisions d'offsets,
- sécurisation du transport sur le réseau local.

### À moyen terme
- support OpenDyslexic avec attribution de licence et intégration de la police,
- tests UI et instrumentation Android,
- protection du flux réseau avec HTTPS local et certificat épinglé,
- authentification serveur et durcissement de la configuration réseau.

## Licence

Projet distribué sous licence MIT. Voir [LICENSE](LICENSE).

## Démarche de conception

Correcteur Dys a été pensé comme un outil utile, simple et digne de confiance :

- adapté à un usage quotidien,
- sans dépendance aux services publics,
- centré sur la confidentialité,
- compatible avec un usage personnel ou professionnel sur un réseau local contrôlé.

Ce logiciel est destiné aux personnes qui veulent écrire mieux en français sans accepter la collecte de contenu ou le modèle cloud par défaut.

---

Correcteur Dys — écrivez mieux en français, sans exposer vos textes.
