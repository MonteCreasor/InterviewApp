package monte.apps.interviewapp.web.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by monte on 2016-07-18.
 */
public class Photos implements Serializable {
    private int count;
    private List<Photo> items;

    public int getCount() {
        return count;
    }

    public List<Photo> getItems() {
        return items;
    }
}
