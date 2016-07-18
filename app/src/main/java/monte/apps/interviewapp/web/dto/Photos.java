package monte.apps.interviewapp.web.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by monte on 2016-07-18.
 */
class Photos implements Serializable {
    private static final long serialVersionUID = 1L;

    private int count;
    private List<Photo> items;

    public int getCount() {
        return count;
    }

    public List<Photo> getItems() {
        return items;
    }

    public class Source implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }
}
