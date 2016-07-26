package monte.apps.interviewapp.web.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by monte on 2016-07-17.
 */

public class VenuesDto implements Serializable {
    private static final long serialVersionUID = -9001087918228589033L;
    private Meta meta;
    private Response response;

    public Meta getMeta() {
        return meta;
    }

    public Response getResponse() {
        return response;
    }

    public List<VenueCompact> getVenues() {
        return getResponse().getVenues();
    }

    public class Response implements Serializable {
        private static final long serialVersionUID = -885753634566237193L;
        private List<VenueCompact> venues;
        public List<VenueCompact> getVenues() {
            return venues;
        }
    }
}
