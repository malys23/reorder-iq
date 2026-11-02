# 2. Separate Python forecasting service

Date: 2026-11-02
Status: Accepted

## Context
The forecasting libraries I plan to use (statistical models and neural networks) are in Python, while the API is written in Java with Spring Boot. The options were: write the whole backend in Python, do the forecasting in Java, or keep the Java API and add a separate Python service for forecasting.

## Decision
Keep the Spring Boot API and build forecasting as a separate Python service using FastAPI

## Consequences
- The forecasting code can use Python's libraries, and can be developed, tested and deployed separately from the API.
- I keep working in Java and React, where I have more experience.
- There are two languages and two services to run, connect and deploy, which adds work.
- The API has to handle the case where the forecasting service is slow or unavailable.