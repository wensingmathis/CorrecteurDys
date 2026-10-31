# Historique

## Préparation de publication — 31 octobre 2026
- Ajout du wrapper Gradle 8.13 (scripts Linux/Windows, JAR et checksum de distribution).
- Workflow configuré sur les push de `main` et les lancements manuels ; résolution des dépendances, tests, compilation et vérification de taille de l'APK avant l'artefact.
- Remplacement de l'installation Android qui échouait sur le paquet SDK obsolète `tools` par l'installation explicite des composants Android 35.
- Correction du pipeline d'acceptation des licences Android : `yes` pouvait recevoir SIGPIPE sous `pipefail` après acceptation réussie.
- Documentation de compilation locale et du démarrage LanguageTool avec restrictions réseau ; ajout de la licence MIT et exclusions de fichiers locaux/secrets.
- Compilation, tests unitaires, exécution GitHub Actions et installation sur appareil non confirmés dans cet environnement.

## 0.1.0 — sources initiales (31 octobre 2026)
- Éditeur Compose, corrections LT locales, sélection et application de suggestions.
- Catégories colorées, activation/désactivation dans les préférences.
- Copie, réception du partage Android, choix français/anglais/allemand.
- Paramètres visuels : taille, espacement, interligne, thème, police système.
- Protection réseau par liste d'adresses IPv4 privées, absence de télémétrie.
- Tests unitaires ajoutés ; compilation GitHub Actions configurée.
- Non validé : compilation réelle, tests instrumentés sur téléphone, police OpenDyslexic.
