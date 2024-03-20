# cadc-vosi

This library provides server-side components that implement VOSI-availability
and VOSI-capabilities for IVOA compliant web services. VOSI-tables support is
available as part of the <a href="https://github.com/opencadc/tap/tree/master/cadc-tap-schema">
cadc-tap-schema</a> library.

In addition to the normal VOSI API, the VOSI-availability component supports optional
changing of the service state. Supported states: ReadWrite (normal), ReadOnly, and Offline.

## cadc-vosi.properties
This config file (optional) allows deployers to specify users that are allowed to
change the service state via HTTP POST to the `/availability` endpoint.
```properties
user = {X509 distinguished name}
user = ...
```
For the X509 distinguished name, the availability endpoint can successfully authorise the 
user even when the associated AAI system is unavailable, so this mechanism is more robust
as it does not depend on any other functioning component.

## TODO
- rewrite the AvailabilityServlet as a RestAction based on the `cadc-rest` library
- add ability to configure users and groups consistent with the `cadc-log` library
- remove the old CapabilitiesServlet
- developer documentation (how to include in web.xml)
