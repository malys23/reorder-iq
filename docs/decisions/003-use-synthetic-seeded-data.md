# 3. Use synthetic seeded data

Date: 2026-11-03
Status: Accepted

## Context
The application needs realistic inventory data, and real company data is either confidential or not available. Public datasets either don't cover the chosen industry or items, suppliers, and stock together. 

## Decision
I chose to generate my own synthetic datase with a fixed random seed using simple parametric demand models (smooth, seasonal, intermittent, lumpy) per category, and commit the generated CSVs.

## Consequences
- Dataset is reproducible with no licensing or confidentiality issues
- I know the true pattern of every item so I can check whether the forecasting handles it
- The patterns are my assumptions not real market behaviour so forecasting results show how methods behave on these patterns, not how they would perform on real data
- Committing the CSVs means the backend can load them without Python