# TP7 — Algorithme A* | GPS Tunisie
**Fondements de l'IA — GLSI2 Semestre 4 — 2025/2026**  
Enseignant : Mohamed Lassoued

---

## Structure du projet

```
TP7_AStar/
├── src/astar/
│   ├── Node.java          # Nœud du graphe (h(n) injectée depuis l'extérieur)
│   ├── Edge.java          # Arête pondérée
│   ├── Graph.java         # Graphe (liste d'adjacence)
│   ├── HeuristicTable.java # Injection externe de h(n)
│   ├── GraphBuilder.java  # Construction du graphe tunisien
│   ├── AStar.java         # Algorithme A* (Tâches 2-4)
│   ├── UCS.java           # Uniform Cost Search (Q3)
│   ├── BestFirst.java     # Best-First Search (Q3)
│   └── Main.java          # Point d'entrée — toutes les expériences
├── interface/
│   └── index.html         # Interface visuelle interactive (carte Tunisie)
├── rapport/
│   └── Rapport_TP7_AStar.pdf  # Rapport d'analyse complet
└── README.md
```

## Compilation et exécution (Java)

```bash
# Compiler
mkdir -p out
javac -d out src/astar/*.java

# Exécuter
java -cp out astar.Main
```

Requires: Java 11+

## Interface web

Ouvrez simplement `interface/index.html` dans un navigateur moderne.

**Fonctionnalités :**
- Carte de Tunisie avec les villes du TP
- Animation pas-à-pas de A*, UCS, et Best-First
- Trace d'exécution en temps réel (open/closed list)
- Test d'heuristique inadmissible (modifier h(Gafsa))
- Graphe étendu avec Gabès et El Kef (Question 4)
- Vitesse d'animation ajustable

## Résultats principaux

| Algorithme  | Chemin                              | Coût    | Nœuds développés |
|-------------|-------------------------------------|---------|------------------|
| UCS         | Tunis → Kairouan → Gafsa → Tozeur  | 450 km  | 5                |
| Best-First  | Tunis → Kairouan → Gafsa → Tozeur  | 450 km  | 3                |
| A*          | Tunis → Kairouan → Gafsa → Tozeur  | 450 km  | 4                |
| A* étendu   | Tunis → Kairouan → Gafsa → Tozeur  | 450 km  | 4                |

## Points clés

- **h(Gafsa) = 100 est inadmissible** car le coût réel minimal est 90 km
- A* est optimal grâce à la combinaison g(n) + h(n)
- L'heuristique n'est pas entièrement consistante (Gafsa viole la condition)
- h(n) est injecté depuis `HeuristicTable` (principe SRP)

## Déclaration IA

Ce projet a été développé avec l'aide de Claude (Anthropic) pour la structure du code, 
le rapport PDF et l'interface web. Toutes les réponses analytiques ont été vérifiées manuellement.
