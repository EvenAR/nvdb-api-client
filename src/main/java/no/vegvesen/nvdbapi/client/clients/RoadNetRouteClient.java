/*
 * Copyright (c) 2015-2019, Statens vegvesen
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.vegvesen.nvdbapi.client.clients;

import no.vegvesen.nvdbapi.client.gson.RouteParser;
import no.vegvesen.nvdbapi.client.model.Coordinates;
import no.vegvesen.nvdbapi.client.model.Projection;
import no.vegvesen.nvdbapi.client.model.RouteOnRoadNet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RoadNetRouteClient extends AbstractJerseyClient {
    private static final Logger LOG = LoggerFactory.getLogger(RoadNetRouteClient.class);

    public RoadNetRouteClient(String baseUrl, Client client) {
        super(baseUrl, client);
    }

    public List<RouteOnRoadNet> getRoutesOnRoadnet(RoadNetRouteRequest request) {
        return getRoutesOnRoadnetAsync(request).toStream().collect(Collectors.toList());
    }

    public Flux<RouteOnRoadNet> getRoutesOnRoadnetAsync(RoadNetRouteRequest request) {
        WebTarget target = getWebTarget(request);
        return doRequest(target);
    }

    private WebTarget getWebTarget(RoadNetRouteRequest request) {
        Objects.requireNonNull(request, "Missing page info argument.");

        UriBuilder path = endpoint();

        if(request.usesReflinkPosition()) {
            path.queryParam("start", request.getStartReflinkPosition());
            path.queryParam("slutt", request.getEndReflinkPosition());
        } else {
            Coordinates startCoordinates = request.getStartCoordinates();
            path.queryParam("start", startCoordinates);
            path.queryParam("slutt", request.getEndCoordinates());
            if(startCoordinates.getProjection() != Projection.UTM33) {
                path.queryParam("srid", startCoordinates.getProjection().getSrid());
            }
        }

        return getClient().target(path);
    }

    private UriBuilder endpoint() {
        return start().path("beta/vegnett/rute");
    }

    private Flux<RouteOnRoadNet> doRequest(WebTarget target) {
        return new AsyncArrayResult<>(target, RouteParser::parseRoute).get();
    }
}