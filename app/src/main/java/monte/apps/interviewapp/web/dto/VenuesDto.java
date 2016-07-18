package monte.apps.interviewapp.web.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by monte on 2016-07-17.
 */

public class VenuesDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Meta meta;
    private Response response;

    public Meta getMeta() {
        return meta;
    }

    public Response getResponse() {
        return response;
    }

    public class Response implements Serializable {
        private static final long serialVersionUID = 1L;

        private List<Venue> venues;
        public List<Venue> getVenues() {
            return venues;
        }
    }

}
