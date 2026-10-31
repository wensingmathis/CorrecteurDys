# Correcteur Dys (Android)

Application Android Kotlin + Jetpack Compose, français en priorité. Aucun compte, publicité, télémétrie ou stockage du texte. **Le texte est envoyé exclusivement à l'adresse IPv4 privée du serveur LanguageTool local que tu configures** ; l'application refuse les hôtes publics et les noms DNS. Le texte quitte donc l'appareil vers ce serveur sur le LAN et reste sous le contrôle de son administrateur.

## État et limites v0.1.0
Version 0.1.0. **La compilation, les tests et l'exécution sur téléphone ne sont pas encore validés dans cet environnement.** Aucun APK n'est inclus dans les sources. Le moteur LanguageTool ne s'exécute pas sur Android : un ordinateur/serveur local est nécessaire. La police OpenDyslexic n'est pas embarquée ; polices système disponibles (Sans-serif et Serif). Les sous-catégories sont indicatives et les suggestions LanguageTool ne sont pas parfaites. Les erreurs se touchent dans la zone de prévisualisation sous la zone de saisie (pas directement dans l'éditeur). Les changements de texte suppriment les résultats obsolètes. Le bouton « Tout corriger » applique la première suggestion pour chaque erreur non chevauchante, de droite à gauche. Revérifier après correction.

## Démarrer LanguageTool sur ton PC Linux
1. Télécharger l'archive du serveur depuis https://dev.languagetool.org/http-server puis l'extraire. Le serveur requiert Java ; suivre la version indiquée par LanguageTool.
2. Dans le dossier extrait, créer `server.properties` (vide pour commencer).
3. Exécuter le serveur : `java -cp languagetool-server.jar org.languagetool.server.HTTPServer --config server.properties --port 8081 --public`.
4. `--public` rend le serveur accessible depuis le réseau. Sur le PC, configurer le pare-feu pour n'autoriser le port TCP 8081 que depuis le sous-réseau local de confiance. **Ne pas créer de redirection de port sur le routeur, ne pas exposer le service à Internet et ne pas utiliser de Wi-Fi public/non fiable.**
5. Trouver l'adresse IPv4 privée du PC avec `hostname -I`, puis saisir `http://IP_DU_PC:8081` dans l'appli. Le téléphone et le PC doivent être sur le même réseau local.
6. Vérifier le service depuis le PC : `curl -d 'language=fr' --data-urlencode 'text=Je sui content.' http://127.0.0.1:8081/v2/check`.

**Important :** `192.168.1.100` est une valeur illustrative ; remplace-la. Le trafic HTTP local n'est pas chiffré. Le serveur peut journaliser les requêtes : contrôler sa configuration, désactiver les journaux sensibles et vérifier qu'il ne transmet pas le contenu à des services tiers. La validation d'adresse de l'application n'est pas un pare-feu et ne protège pas le serveur contre d'autres appareils du LAN.

## APK via GitHub Actions
Après publication du dépôt, chaque push sur `main` et chaque lancement manuel du workflow exécutent la résolution des dépendances, les tests unitaires et la compilation. Le workflow vérifie que l'APK existe et dépasse 1 Mo, puis le publie sous l'artefact `CorrecteurDys-debug`.

Dans **GitHub → Actions**, ouvrir une exécution réussie, télécharger l'artefact `CorrecteurDys-debug`, puis extraire `app-debug.apk`. Transférer l'APK sur le téléphone, l'ouvrir et autoriser temporairement l'installation depuis le gestionnaire de fichiers si Android le demande. Une APK debug n'est pas une version de production ; les APK produits par des exécutions différentes peuvent nécessiter la désinstallation préalable de l'ancienne version.

Si le workflow échoue, **ne pas installer et ne pas déclarer l'APK validé** : consulter le journal d'erreurs, puis corriger les sources. Un APK debug est signé par la clé de debug GitHub Actions ; les mises à jour entre exécutions peuvent nécessiter une désinstallation, ce qui est acceptable car aucun texte n'est enregistré.

## Construction locale
Prérequis : JDK 17 et Android SDK avec Android 35 et les Build Tools correspondants. Le wrapper télécharge Gradle 8.13 et vérifie son SHA-256 ; aucun Gradle système n'est requis.

À la racine du projet :

```sh
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Sous Windows, remplacer `./gradlew` par `gradlew.bat`. Sortie debug : `app/build/outputs/apk/debug/app-debug.apk`.

## Architecture
- `MainActivity.kt` : UI Compose + réglages (préférences seulement) + partage Android.
- `EditorViewModel.kt` : état éphémère et orchestration de la correction.
- `LanguageToolClient.kt` : API locale, validation des adresses et parsing JSON.
- `Correction.kt` : modèles, classification et application sûre des suggestions.
- `app/src/test` : tests unitaires.

## Licence
Projet distribué sous licence MIT ; voir [LICENSE](LICENSE).

## Confidentialité technique
Aucun hôte Internet public accepté. Pas de stockage du texte en base ou préférences. Le texte peut temporairement exister dans la mémoire du processus, le presse-papier après « Copier » ou dans la pile d'apps Android. Désactiver les sauvegardes Android. Le serveur LT est sous le contrôle de son administrateur.
