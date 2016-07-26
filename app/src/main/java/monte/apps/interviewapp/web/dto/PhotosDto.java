package monte.apps.interviewapp.web.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by monte on 2016-07-18.
 */
public class PhotosDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Meta meta;
    private Response response;

    public Meta getMeta() {
        return meta;
    }

    public Response getResponse() {
        return response;
    }

    public List<Photo> getPhotos() {
        return getResponse() != null && getResponse().getPhotos() != null
               ? getResponse().getPhotos().getItems()
               : new ArrayList<>();
    }

    public class Response implements Serializable {
        private static final long serialVersionUID = 1L;

        private Photos photos;

        public Photos getPhotos() {
            return photos;
        }
    }
}
