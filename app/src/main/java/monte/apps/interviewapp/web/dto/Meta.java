package monte.apps.interviewapp.web.dto;

import java.io.Serializable;

/**
 * Created by monte on 2016-07-18.
 */
public class Meta implements Serializable {
    private int code;
    private String requestId;

    public int getCode() {
        return code;
    }

    public String getRequestId() {
        return requestId;
    }
}
