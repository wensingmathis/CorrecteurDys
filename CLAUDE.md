# Mémoire technique — Correcteur Dys

## Mission
Application Android d'assistance à l'écriture, français d'abord, sans cloud ni télémétrie.

## Stack
Kotlin 2.2.20, Jetpack Compose, MVVM, minSdk 26, Gradle 8.13, AGP 8.11.1.

## Confidentialité (invariant)
- Ne jamais transmettre les textes à un domaine public, même en cas d'erreur.
- Client HTTP POST uniquement vers IPv4 privée validée, aucun suivi de redirection.
- Ne jamais journaliser ni persister les textes ; seules les préférences sont sauvegardées.
- Aucune analytics, publicité, SDK cloud ou permission de stockage.
- Ne pas afficher le texte dans les messages d'erreur/logs.

## Moteur
LanguageTool auto-hébergé, API POST /v2/check. L'appli n'embarque pas le moteur.
Le serveur doit être explicitement configuré et accessible sur le réseau local.
La classification orthographe/grammaire/conjugaison/ponctuation est heuristique et imparfaite.

## Qualité
Tout changement dans CorrectionEngine et LocalAddress exige tests unitaires.
Ne jamais qualifier un build ou une fonction de « testé » sans résultat CI ou exécution réelle.

## Architecture et décisions
- `MainActivity.kt` : écran Compose, réglages d'accessibilité en préférences et réception de `ACTION_SEND` en texte brut.
- `EditorViewModel.kt` : état en mémoire, orchestration asynchrone, suppression des résultats obsolètes après édition.
- `LanguageToolClient.kt` : requête HTTP POST `/v2/check`, IPv4 privée validée, redirections désactivées, délais bornés ; le texte n'est pas écrit dans les logs ni les préférences.
- `Correction.kt` : modèles, classification heuristique et application des propositions par offsets UTF-16 ; `CorrectionEngineTest.kt` couvre les transformations et les hôtes publics.
- Java 17, AGP 8.11.1, Kotlin/Compose 2.2.20, Gradle 8.13 via wrapper, compileSdk/targetSdk 35 et minSdk 26.
- Le wrapper contrôle le SHA-256 de la distribution Gradle. Le workflow utilise `actions/checkout@v7`, `actions/setup-java@v6`, `gradle/actions/setup-gradle@v6` et `actions/upload-artifact@v7` sur Ubuntu 24.04. Il installe explicitement `platforms;android-35`, `build-tools;35.0.0` et `platform-tools` via `sdkmanager` ; le pipeline de licences évite le SIGPIPE de `yes` sous `pipefail` sans masquer le statut de `sdkmanager`.

## Problèmes connus / validation
- Le run GitHub Actions [41232354478](https://github.com/wensingmathis/CorrecteurDys/actions/runs/41232354478) a validé les tests unitaires, `assembleDebug`, le contrôle de l'APK et son upload. Avertissement non bloquant restant : Gradle 8.13 est signalé comme ancien ; vérifier sa compatibilité avec AGP avant toute mise à niveau.
- Aucun test UI/instrumentation ni test sur appareil/émulateur n'est présent.
- Le transport HTTP du LAN n'est pas chiffré et le serveur LanguageTool est administré séparément ; ne pas l'exposer à Internet.
- OpenDyslexic, l'épinglage de certificat, l'authentification serveur, les collisions d'offsets complexes et les réglages avancés de catégories restent à traiter.

## À faire
1. Évaluer une mise à niveau coordonnée d'AGP et Gradle après vérification de leur compatibilité.
2. Ajouter police OpenDyslexic après vérification de sa licence et intégration de l'asset.
3. Tests UI/instrumentation ; règles de confidentialité et collisions d'offsets.
4. Améliorer les correspondances de catégories LanguageTool et options de langue.
5. Durcir le transport (HTTPS LAN, épinglage de certificat) et authentification serveur.
