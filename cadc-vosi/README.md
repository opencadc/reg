# cadc-vosi

This library provides server-side components that implement VOSI-availability
and VOSI-capabilities for IVOA compliant web services. VOSI-tables support is
available as part of the <a href="https://github.com/opencadc/tap/tree/master/cadc-tap-schema">
cadc-tap-schema</a> library.

In addition to the normal VOSI API, the VOSI-availability component supports optional
changing of the service state. Supported states: ReadWrite (normal), ReadOnly, and Offline.

Set state example:
```
curl --cert <cert file> -d state=ReadOnly https://example.net/srv/availability
```
Get example:
```
curl https://example.net/srv/availability
```

## cadc-vosi.properties
This config file (optional) allows deployers to specify users that are allowed to
change the service state via HTTP POST to the `/availability` endpoint.
```properties
user = {X509 distinguished name}
user = ...

# optional mode at startup: Offline, ReadOnly, ReadWrite (default)
startupMode = ReadWrite|ReadOnly|Offline
```

For the X509 distinguished name, the availability endpoint can successfully authorise the 
user even when the associated AAI system is unavailable, so this mechanism is more robust
as it does not depend on any other functioning component.

If an optional _startupMode_ is configured, the availability servlet will create the service-specific
AvailabilityPlugin and call `setState`. The behaviour is completely in rthe control of the service
and how it implements the plugin.

## TODO
- rewrite the AvailabilityServlet as a RestAction based on the `cadc-rest` library
- add ability to configure users and groups consistent with the `cadc-log` library
- remove the old CapabilitiesServlet
- developer documentation (how to include in web.xml)

## temporary developer documentation
The following (or something like it) is needed in the `web.xml` deployment descriptor to add 
an /avaailability endpoint to a service:
```xml
<!-- VOSI availability -->
  <servlet>
    <servlet-name>AvailabilityServlet</servlet-name>
    <servlet-class>ca.nrc.cadc.vosi.AvailabilityServlet</servlet-class>
    <init-param>
      <param-name>ca.nrc.cadc.vosi.AvailabilityPlugin</param-name>
      <param-value>fully.qualified.classname.for.AvailabilityPluginImpl</param-value>
    </init-param>
    <load-on-startup>2</load-on-startup>
  </servlet>
  <servlet-mapping>
    <servlet-name>AvailabilityServlet</servlet-name>
    <url-pattern>/availability</url-pattern>
  </servlet-mapping>
```
Especially if using the _startupMode_, putting this servlet early in the load sequence (2 above, before
service specific endpoints and init) will ensure that the startup mode is set before other init happens.

