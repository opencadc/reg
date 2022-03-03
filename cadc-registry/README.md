# cadc-registry
client implementation of IVOA Registry specifications

### LocalAuthority.properties
The LocalAuthority.properties file specifies which local service is authoritative for various site-wide functions. The keys are standardID values for the functions and the values are resourceID values for the service that implements that standard feature.

Example:
```
ivo://ivoa.net/std/GMS#search-1.0 = ivo://cadc.nrc.ca/gms           
ivo://ivoa.net/std/UMS#users-0.1 = ivo://cadc.nrc.ca/gms    
ivo://ivoa.net/std/UMS#login-0.1 = ivo://cadc.nrc.ca/gms           

ivo://ivoa.net/std/CDP#delegate-1.0 = ivo://cadc.nrc.ca/cred
ivo://ivoa.net/std/CDP#proxy-1.0 = ivo://cadc.nrc.ca/cred
```
