# simple service registry (reg)

## configuration

The following configuration files must be available in the /config directory.

### catalina.properties

This file contains java system properties to configure the tomcat server and some
of the java libraries used in the service.

See <a href="https://github.com/opencadc/docker-base/tree/master/cadc-tomcat">cadc-tomcat</a>
for system properties related to the deployment environment.

See <a href="https://github.com/opencadc/core/tree/master/cadc-util">cadc-util</a>
for common system properties. 

`reg` does not include any IdentityManager implementations because it allows anonymous
access.

### reg-resource-caps.properties
This configuration file provides a list of service identifiers (resourceID)  and the URL to the VOSI-capabilities endpoint for that service. 
```
{resourceID} = {VOSI-capabilities URL}
```
The <a href="https://github.com/opencadc/reg/tree/master/cadc-registry">cadc-registry</a> client library
uses this content to lookup services on behalf of service-specific clients.

### reg-applications.properites
This configuration file provides a list of UI resources (resourceID)  and the URL of the page that provides
the specified feature.
```
{resourceID} = {URL}
```
The <a href="https://github.com/opencadc/reg/tree/master/cadc-registry">cadc-registry</a> client library
uses this content to lookup UI resources.

## building

```
gradle clean build
docker build -t reg -f Dockerfile .
```

## checking it
```
docker run -it reg:latest /bin/bash
```

## running it
```
docker run --user tomcat:tomcat --volume=/path/to/external/config:/config:ro --name reg reg:latest
```

## apply version tags
```
bash
. VERSION && echo "tags: $TAGS"
for t in $TAGS; do
   docker image tag reg:latest reg:$t
done
unset TAGS
docker image list reg
```
