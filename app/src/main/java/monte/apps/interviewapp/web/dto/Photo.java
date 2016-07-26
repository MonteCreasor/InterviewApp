package monte.apps.interviewapp.web.dto;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by monte on 2016-07-18.
 */
public class Photo implements Serializable {
    private static final long serialVersionUID = -5095449683709540676L;
    private String id;
    private Source source;
    private String prefix;
    private String suffix;
    private int width;
    private int height;
    private String visibility;

    public class Source implements Serializable {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }

    public String getId() {
        return id;
    }

    public Source getSource() {
        return source;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getVisibility() {
        return visibility;
    }

    public boolean isVisible() {
        return TextUtils.equals(visibility, "public");
    }
}
