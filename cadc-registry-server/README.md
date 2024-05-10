# cadc-registry-server: OBSOLETE

The last published version of this library is `org.opencadc:cadc-registry-server:1.1.2`.

The code has been moved into the `reg` service which has been improved to allow for
runtime configuration of service behaviour and content.

## reg-resource-caps.properties
This is the canonical version of the configuration of services for the production 
CADC registry service. The _live_ version used by software is:

https://ws.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/reg/resource-caps

and returns a `key = value` (properties file) with {resourceID} = {capabilities URL}.

## reg-applications.properties
This is the canonical version of the configuration of applications for the production
CADC registry service. The _live_ version used by software is:

https://ws.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/reg/applications

and returns a `key = value` (properties file) with {featureID} = {access URL} for web site
(application) resources.
