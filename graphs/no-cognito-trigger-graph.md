## Graph with Cognito without trigger

Showing possible new dependency graph where Cognito is not using a trigger to create the access token. Requires the client
 to possible look up extra claims from the /userinfo endpoint or similar.

Possible new solution.

```mermaid
graph TD
    PS[Publication Service] --> CD[Custom Domain]
    CS[Customer Service] --> CD
    US[User Service] --> CD
    CS --> CDB[(Customer DB)]
    US --> UDB[(User DB)]
    DNS[Hosted Zone] --> CERT[Certificate]
    CD --> DNS
    COG[Cognito]
    PS --> COG
    US --> COG
    CS --> COG
    PS --> EB[Event Bus]
```