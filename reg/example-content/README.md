# example registry content

This example is for the _authority_ named `example.net`. To expose this content
and run the integration tests, the `reg` service needs to be configured with
```
org.opencadc.reg.authority = example.net
```
and the remaining files must be included in `/config/content` of the container.

## Identify.xml and example.net/registry.xml
These two files describe the registry itself. It's the same VOResource
record inside a different OAI _envelope_.

## example.net.xml
This file is the authority record.

## ListMetadataFormats.xml and ListSets.xml
These are static responses to OAI queries and can be used as-is.

## example.net
This directory contains records for all the resources to be published. In this
example there is only the registry record itself, but other files can be added.

If resource identifiers (`ivo://{authority}/{name}`) have multiple path components,
those should be reflected in the structure under the _authority_ directory.

For example, the `ivo://example.net/registry` record is in `example.net/registry.xml`
and the `ivo://example.net/foo/bar` record would be in `example.net/foo/bar.xml`.
