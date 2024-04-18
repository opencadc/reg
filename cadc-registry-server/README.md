# cadc-registry-server: OBSOLETE

The last published version of this library is `org.opencadc:cadc-registry-server:1.1.2`.

The code has been moved into the `reg` service which has been improved to allow for
runtime configuration of service behaviour and content.

## old information

Simple registry implementation that provides limited support for two IVOA Registry features:

* OAI publishing registry: uses a set of manually created and maintained static XML files

* queryable runtime registry: uses simple key=value properties files to provide specific "canned queries"

Basically, there is no registry database per se -- these tools provide some simple or optimised front-ends to get by
without one. 

## OAI Publishing Registry
The OAI publishing is not OAI-compliant but does do enough that other IVOA registries can harvest resource records. 
If you are careful you can even update records such that incremental harvesting works. TODO: figure out how to correctly
"delete" records.

## Canned Lookup Queries
The canned queries supports a query that returns key=value pairs of the form {resourceID} = {URL}. The meaning depends
on which canned query (properties file) is invoked. The reg-resource-caps.properties example file is a 
query for the accessURL of the VOSI-capabilities endpoint for each resource. For CADC, this is used when finding and using
web services and data collections (with aux capabilties). The reg-applications.properties file is a query result that returns
the base accessURL for browser-based UI applications. The code in this library allows one to deploy one or more such canned queries
by writing a suitable properties file. The cadc-registry (client) is written to make use of the resource-caps and 
VOSI-capabilities approach.

## TODO
1. move the code from internal repository to github
2. document use of the canned query setup
3. document use the the OAI publishing setup
4. ...
5. implement a proper back end to support queries (RegTAP) and content curation
