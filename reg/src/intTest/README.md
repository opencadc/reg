# simple service registry (reg) integration tests

The integration tests can be run against a deployed `reg` service to verify that it is working
and validate (some?) of the XML content. This is only worth while if you are testing the OAI
publishing endpoint as the other endpoints can be tested with simple visual inspection of the
output.

First, verify the deployment by looking at the output of the capabilities endpoint and make sure
the hostname and path in the various accessURL entries is correct.

To run the integration tests against a deployed `reg` service:
```
SRV={base reg URL}/capabilities
gradle -Dorg.opencadc.reg.capabilitiesURL=${SRV} intTest ...
```

The CannedQueryTest tests will PASS if the feature is not configured so manual checking is advised.

