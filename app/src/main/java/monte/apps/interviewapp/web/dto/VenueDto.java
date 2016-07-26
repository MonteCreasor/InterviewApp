package monte.apps.interviewapp.web.dto;

import java.io.Serializable;

/**
 * Created by monte on 2016-07-17.
 */

public class VenueDto implements Serializable {
    private static final long serialVersionUID = -7067281855140170476L;
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
        private static final long serialVersionUID = 8413177535824692217L;
        private VenueComplete venue;

        public VenueComplete getVenue() {
            return venue;
        }
    }
}
