package org.acme.resteasy;

import de.fstab.demo.SuperRandom;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/random")
public class ExampleResource {

    @Inject
    SuperRandom random;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "next random number is " + random.nextInt() + "\n";
    }
}