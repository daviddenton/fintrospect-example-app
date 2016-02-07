# building security system

#### about
This is a complete example application which uses the majority of the features of the [Fintrospect](http://fintrospect.io) library:
- HTTP request routing with automatic parameter marshalling and unmarshalling (Headers/Query/Path/Body)
- HTTP clients with request creation and route spec reuse for Fake Server implementations
- HTTP response building, including custom JSON library support (Json4S)
- Automatic invalid request handling
- Swagger 2.0 documentation and JSON schema generation from example model objects
- Endpoint security
- Templating system (Mustache)
- Serving of static resources

It has been developed in a London-TDD style with outside-in acceptance testing and CDCs for outside dependencies,
to give a complete overview of how the app would look when finished. The code itself has been left without optimisation of
imports in order to aid comprehension - which is a little frustrating from a maintainer perspective (as you always want your 
code looking as awesome as possible! :).

#### requirements
This example models a simple building security system accessible over HTTP. Requirements are:

1. Users can ask to be let into and out of the building.
2. Usernames are checked for validity against a remote HTTP UserDirectory system.
3. Successful entries and exits are logged in a remote HTTP EntryLogger system.
4. Ability to check on the current inhabitants of a building.
5. Users are tracking in a binary state - inside or not (outside). Only people outside the building can enter, and vice versa.
6. All HTTP endpoints are protected with a secret HTTP header to only allow authorised access.
7. API documentation should be available.
8. Logging of every 10 successful requests should be made.

#### running this demo app
1. Clone this repo
2. Run ```sbt test:run```, or  ```RunnableEnvironment``` from an IDE. This will start the application on port 9999 
which has been configured to use a fake versions of the remote dependencies (on ports 10000 and 10001)
3. Just point your browser at <a href="http://localhost:9999/">http://localhost:9999/</a>

