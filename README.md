# 🎯 Duel Quiz — Application Android

Une application Android de quiz éducatif interactif pour 1 ou 2 joueurs, avec génération de questions par IA (Gemini), détection de gestes par caméra et système de gamification.

<img width="1536" height="1024" alt="image" src="https://github.com/user-attachments/assets/6ae27ed8-eab0-4b57-b5e6-eaa7bf74122d" />


---

## 📱 Fonctionnalités

### 🤖 Génération de quiz par IA (Gemini)
- Import d'un document PDF analysé automatiquement par l'API Gemini
- Génération de questions QCM en JSON via prompt engineering strict
- Nettoyage Regex et validation des données avant insertion en base

### 🔄 Mode de secours (Fallback)
- Si l'API Gemini échoue, le prompt est copié automatiquement dans le presse-papier
- L'application ouvre directement [gemini.google.com](https://gemini.google.com) dans le navigateur
- L'utilisateur peut coller la réponse JSON manuellement dans l'application

### 📷 Mode Duel Caméra
- Détection de gestes via **MediaPipe** (21 points de repère sur la main)
- Les joueurs répondent en montrant un chiffre avec leurs doigts
- Overlay personnalisé affichant scores et animations en temps réel

### 🎮 Modes de jeu
| Mode | Description |
|------|-------------|
| QCM Classique | Entraînement solo sur les thèmes importés |
| Duel Caméra | Deux joueurs, réponses par gestes détectés |
| Duel Classique | Deux joueurs, réponses par boutons |

### 🏆 Gamification
- Système de **skins** déblocables selon les performances
- Questions de QI général pour évaluer le niveau du joueur
- Suivi des statistiques et historique des scores

### 🔒 Système de tutoriel et verrouillage
- Les fonctionnalités sont grisées jusqu'à la completion du tutoriel
- État sauvegardé via **SharedPreferences**

### 🌍 Internationalisation
Langues supportées : 🇫🇷 Français · 🇬🇧 Anglais · 🇪🇸 Espagnol · 🇨🇳 Chinois

---

## 🏗️ Architecture (MVVM + SOLID)

```
├── UI Layer (Jaune)       → Activities, navigation, affichage
├── Logic Layer (Vert)     → QuizManager, HandTrackingService, ViewModels
├── Data Layer (Bleu)      → Room Database, DAOs, Repositories
├── Components (Violet)    → OverlayView personnalisé
└── Main (Orange)          → Point d'entrée, initialisation globale
```

### Base de données Room
3 tables : `User` · `Question` · `Score`

---

## 🗺️ Navigation

```
MainActivity
└── LoginActivity / RegisterActivity
    └── MenuActivity (Hub central)
        ├── CreateQuizActivity → ListQcmActivity
        ├── ProfileActivity → AvatarActivity
        ├── StoryActivity (Tutoriel)
        ├── SelectionThemeActivity
        │   ├── DuelChoiceActivity → GameActivity
        │   └── DuelCameraActivity → GameActivity
        └── HistoryActivity
```

---

## 🛠️ Stack technique

| Technologie | Usage |
|-------------|-------|
| Java | Langage principal |
| Android SDK | Framework mobile |
| Room (SQLite) | Persistance des données |
| MediaPipe | Détection de gestes (vision par ordinateur) |
| Gemini API | Génération de questions par IA |
| PdfBox-Android | Extraction de texte depuis PDF |
| Gson | Désérialisation JSON |
| LiveData / ViewModel | Architecture réactive MVVM |
| SharedPreferences | Sauvegarde locale (tutoriel, session) |

---

## 🚀 Installation

1. Clone le dépôt :
```bash
git clone https://github.com/Shadeleo/app-android-quiz.git
```

2. Ouvre le projet dans **Android Studio**

3. Configure ta clé API Gemini dans le fichier `local.properties` :
```
GEMINI_API_KEY=ta_clé_ici
```

4. Lance l'application sur un émulateur ou un appareil Android (API 26+)

---
