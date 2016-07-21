package monte.apps.interviewapp.web.dto;

import java.io.Serializable;

/**
 * Created by monte on 2016-07-17.
 */

public class VenueDto implements Serializable {
    private Meta meta;
    private Response response;

    public Meta getMeta() {
        return meta;
    }

    public Response getResponse() {
        return response;
    }

    public VenueComplete getVenue() {
        return getResponse().getVenue();
    }

    public class Response implements Serializable {
        private VenueComplete venue;

        public VenueComplete getVenue() {
            return venue;
        }
    }
}
