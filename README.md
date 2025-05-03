# 🧠 IA Diagnostic & Testimonial Intelligence — Flask + Angular + Power BI

Ce projet regroupe plusieurs services intelligents au sein de la plateforme **REFUGEE AND SOLIDARITY** :

1. 🎯 Diagnostic médical via une API IA (BERT + TensorFlow)
2. 📸 Analyse automatique d’images de témoignages avec HuggingFace
3. 🌍 Traduction multilingue des témoignages avec Azure Translate
4. 📊 Visualisation Power BI intégrée pour les bénévoles

---

## 📦 Fonctionnalités

### 🧬 1. Diagnostic IA via Flask API

- 🔬 Traitement NLP des symptômes médicaux
- 🤖 Embedding des textes avec [`Bio_ClinicalBERT`](https://huggingface.co/emilyalsentzer/Bio_ClinicalBERT)
- 🧠 Réseau dense Keras pour la classification
- 📮 API REST `/predict` retournant diagnostic + traitement
- 💾 Sauvegarde du modèle `.keras`, `LabelEncoder`, et map traitement

### 📸 2. Analyse d’images des témoignages

- 🖼️ Envoi de deux images (avant/après)
- 🧠 Interprétation automatique via un service Flask local (HuggingFace Transformers)
- ✍️ Génération automatique de contenu et description

### 🌐 3. Traduction multilingue (UI Angular)

- 🌍 Traduction en temps réel des témoignages (contenu + description)
- 📤 Appels backend à l’API `/translate`
- 📑 Langues supportées : 🇬🇧 🇸🇦 🇨🇳 🇧🇬 🇳🇱 🇫🇷 🇷🇺 🇮🇳 et autres

### 📊 4. Dashboard Power BI (Angular)

- 🔐 Accès réservé aux volontaires
- 📈 Visualisation des rapports de soins
- 📎 Bouton intégré pour afficher/masquer l’`iframe` Power BI
- 📤 Lien sécurisé depuis `/api/healthcare/report-url`

---

## ⚙️ Installation

### Backend (Flask IA)

```bash
git clone https://github.com/yassinezribizribi/PIDEV-CHARITY-BACKEND.git
cd PIDEV-CHARITY-BACKEND/flask-ia
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
