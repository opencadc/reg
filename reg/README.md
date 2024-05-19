# simple service registry (reg)

## docker image
Prebuilt and tested images are available in the `images.opencadc.org` image registry. 

The image name is `images.opencadc.org/core/reg`. The current version comes from the VERSION file, e.g. 
```
images.opencadc.org/core/reg:1.1.0
```

## deployment
The `reg` war file can be renamed at deployment time in order to support an alternate 
service name, including introducing additional path elements. 
See <a href="https://github.com/opencadc/docker-base/tree/master/cadc-tomcat">cadc-tomcat</a> (war-rename.conf).

## configuration

The following configuration files must be available in the `/config` directory.

### catalina.properties

This file contains java system properties to configure the tomcat server and some
of the java libraries used in the service.

See <a href="https://github.com/opencadc/docker-base/tree/master/cadc-tomcat">cadc-tomcat</a>
for system properties related to the deployment environment.

See <a href="https://github.com/opencadc/core/tree/master/cadc-util">cadc-util</a>
for common system properties. 

`reg` does not include any IdentityManager implementations because it is intended 
to called anonymously.

### reg.properties (optional)
A reg.properties file in /config is required to configure OAI publishing.
```
org.opencadc.reg.authority = {IVOA authority}
```
See below for how to add _content_ (registry records to publish). The current implementation
only supports one _authority_.

If the file is not present or no _authority_ is configured, the OAI endpoint will respond
with a 404 and suitable message.

### reg-resource-caps.properties (optional)
This configuration file provides a list of service identifiers (resourceID)  and the URL to the VOSI-capabilities endpoint for that service. 
```
{resourceID} = {VOSI-capabilities URL}
```
The <a href="https://github.com/opencadc/reg/tree/master/cadc-registry">cadc-registry</a> 
client library uses this content to lookup services on behalf of service-specific clients.

If the file is not present, the resource-caps endpoint will respond with a 404 and suitable
message.

### reg-applications.properites (optional, experimental)
This configuration file provides a list of UI resources (featureID)  and the URL of the 
page that provides the specified feature.
```
{featureID} = {URL}
```
The <a href="https://github.com/opencadc/reg/tree/master/cadc-registry">cadc-registry</a> 
client library uses this content to lookup UI resources. For example, pages/applications 
on a web site could look for the _login-page_ feature in order to make a link to the page 
that provides the feature.

If the file is not present, the applications endpoint will respond with a 404 and suitable
message.

### cadc-log.properties (optional)
See <a href="https://github.com/opencadc/core/tree/master/cadc-log">cadc-log</a> for common 
dynamic logging control.

### cadc-vosi.properties (optional)
See <a href="https://github.com/opencadc/reg/tree/master/cadc-vosi">cadc-vosi</a> for common 
service state control.

## content
The _content_ (registry records to publish) are simply XML files stored in the `/config/content`
directory. The following files are required:
### Identify.xml
This is the OAI `<Identify>` record for the publishing registry with `<VOResource>` extension in
the `<description>`. See example: TODO.

### ListMetadataFormats.xml
This is a static response to the OAI `verb=ListMetadataFormats` query. See (use) example: TODO.

### ListSets.xml
This is a static response to the OAI `verb=ListSets` query. See (use) example: TODO.

### {authority}.xml
This is an OAI `<GetRecord>` response for the configured authority. The OAI `<metadata>`
contains a `<VOResource>` with `xsi:type="vg:Authority"` that describes the IVOA authority.
See example: TODO.

### {authority} sub-directory
This directory contains OAI `<GetRecord>` response(s) for all the resources to be published
under the this authority. This includes records that were previously published and later
deleted (marked as deleted). 

In the current code, the `<datestamp>` element in the OAI envelope and the `updated` attribute
in the `<VOResource>` element must be manually udpated, must (should?) be the same, and are 
used as-is to filter records when OAI `start` and `end` query parameters are used. Sort of plan:
see if the filesystem timestamps are a reliable way to detect changes and set those values while
processing the record for output (use filesystem timestamps to filter and replace the values
on the fly).

See examples: TODO.

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
