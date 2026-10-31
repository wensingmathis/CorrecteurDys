# Historique

## Préparation de publication — 31 octobre 2026
- Ajout du wrapper Gradle 8.13 (scripts Linux/Windows, JAR et checksum de distribution).
- Workflow configuré sur les push de `main` et les lancements manuels ; résolution des dépendances, tests, compilation et vérification de taille de l'APK avant l'artefact.
- Remplacement de l'installation Android qui échouait sur le paquet SDK obsolète `tools` par l'installation explicite des composants Android 35.
- Correction du pipeline d'acceptation des licences Android : `yes` pouvait recevoir SIGPIPE sous `pipefail` après acceptation réussie.
- Mise à jour des actions vers checkout v7, setup-java v6, setup-gradle v6 et upload-artifact v7 ; runner fixé sur Ubuntu 24.04.
- Documentation de compilation locale et du démarrage LanguageTool avec restrictions réseau ; ajout de la licence MIT et exclusions de fichiers locaux/secrets.
- Run [41232354478](https://github.com/wensingmathis/CorrecteurDys/actions/runs/41232354478) réussi : tests unitaires, compilation debug, contrôle de taille et publication de l'artefact `CorrecteurDys-debug`.
- L'archive de l'artefact fait 9 464 754 octets ; l'APK extrait fait 9 874 037 octets. Aucun essai d'installation sur téléphone ou émulateur n'a été effectué.
- GitHub signale encore Gradle 8.13 comme ancien ; sa mise à niveau est différée pour préserver la compatibilité vérifiée avec AGP 8.11.1.

## 0.1.0 — sources initiales (31 octobre 2026)
- Éditeur Compose, corrections LT locales, sélection et application de suggestions.
- Catégories colorées, activation/désactivation dans les préférences.
- Copie, réception du partage Android, choix français/anglais/allemand.
- Paramètres visuels : taille, espacement, interligne, thème, police système.
- Protection réseau par liste d'adresses IPv4 privées, absence de télémétrie.
- Tests unitaires ajoutés ; compilation GitHub Actions configurée.
- Tests unitaires et compilation debug validés via GitHub Actions ; interface non testée sur téléphone/émulateur, police OpenDyslexic absente.
