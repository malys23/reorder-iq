# ReorderIQ
 
A procurement and inventory planning app that helps a planner decide **what to reorder, when, and how much**, without overstocking. It combines an inventory dashboard, a reorder-point calculator, a purchase-request workflow and demand forecasting.
 
> **Status: in development.** This README describes the planned scope. Items are checked off in the [roadmap](#roadmap) as they are built, and anything not checked is not built yet.
 
## Why I'm building it
 
In my procurement work at Exiros I handled about 17,000 materials and over $25M in annual spend. The hard part was never a single order. It was knowing, across thousands of items, which ones to reorder now, which were overstocked, and how much lead time to plan for. ReorderIQ models that workflow end to end, using synthetic data, as a project that covers the full stack, a forecasting component and cloud deployment.
 
## Planned features
 
**Inventory and procurement**
- Inventory dashboard: stock on hand, days of supply, stockouts
- Reorder-point and safety-stock calculator, with alerts when an item falls below its reorder point
- Purchase-request tracker with a status flow: Draft, Submitted, Approved, Ordered, Received
- Supplier lead-time view
- Excess-stock flags
**Forecasting**
- Demand forecast per item
- Baseline models (moving average, exponential smoothing) compared with a small neural network
- Accuracy metrics (MAPE and RMSE) shown against the baseline, and a forecast chart with a confidence range
- An evaluation notebook that documents the method and results
**Engineering**
- Login with roles (planner and approver)
- Unit and integration tests
- API documentation with Swagger/OpenAPI
- Deployment on AWS with a CI/CD pipeline
- 
## How the reorder point works
 
For each item, with average daily demand *d*, supplier lead time *L* in days, service-level factor *z* and standard deviation of daily demand *σ<sub>d</sub>*:
 
$$ROP = d \cdot L + SS, \qquad SS = z \cdot \sigma_d \sqrt{L}$$
 
When stock on hand plus incoming orders falls to the reorder point (ROP), the app flags the item for reorder. The safety stock (SS) covers variation in demand during the lead time.
 
## Tech stack (planned)
 
| Layer | Technology |
| --- | --- |
| Front end | React, Redux, Recharts |
| API | Spring Boot, Maven, JUnit |
| Database | PostgreSQL |
| Forecasting service | Python, FastAPI, statsmodels or scikit-learn, TensorFlow or PyTorch |
| Infrastructure | AWS (EC2 or Elastic Beanstalk, RDS, S3), GitHub Actions |
 
## Data
 
All data is synthetic. The generator creates a few hundred items with about two years of weekly demand, including trend, seasonality and intermittent (spiky) demand, which is the case that makes forecasting hard in practice.
 
## Architecture (planned)
 
```
React front end
   |
   v
Spring Boot REST API  <--->  PostgreSQL
   |
   v
FastAPI forecasting service (baselines + neural network)
```
 
## Roadmap
 
- [ ] Repo setup: README, `.gitignore`, license
- [ ] Synthetic dataset generator
- [ ] Database schema (items, suppliers, stock levels, demand history, purchase requests, forecasts, users)
- [ ] Spring Boot API for items, stock levels and purchase requests
- [ ] Login and roles
- [ ] Reorder-point calculation, with tests
- [ ] React dashboard, item page and purchase-request workflow
- [ ] Baseline forecasts and evaluation notebook
- [ ] Neural network forecast, compared with the baseline
- [ ] Forecasting service connected to the app
- [ ] AWS deployment with CI/CD, and a live demo
- [ ] Screenshots, architecture diagram and demo video
      
## Out of scope for version 1
 
ERP integrations, real supplier APIs and multi-warehouse logic.
 
## Running it locally
 
*Not available yet. Setup instructions will be added when there is something to run.*
 
## What I'm learning from it
 
*Not available yet.*
 
## License
 
*MIT*
 
## Contact
 
Alyona M · [LinkedIn](https://www.linkedin.com/in/amaliassova)
