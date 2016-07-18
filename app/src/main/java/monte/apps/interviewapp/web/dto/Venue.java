package monte.apps.interviewapp.web.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by monte on 2016-07-18.
 */
public class Venue implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private Location location;
    private List<Category> categories;
    private Contact contact;
    private String url;
    private Stats stats;
    private HereNow hereNow;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public List<Category> getContact() {
        return categories;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public class HereNow implements Serializable {
        private static final long serialVersionUID = 1L;

        private int count;
        private String summary;
    }

    public class Stats implements Serializable {
        private static final long serialVersionUID = 1L;

        private long checkinsCount;
        private long usersCount;
        private long tipCount;

        public long getUsersCount() {
            return usersCount;
        }

        public long getTipCount() {
            return tipCount;
        }

        public long getCheckinsCount() {
            return checkinsCount;
        }
    }

    public class Location implements Serializable {
        private static final long serialVersionUID = 1L;

        private String address;
        private double lat;
        private double lng;
        private List<String> formattedAddress;
        private long distance;

        public String getAddress() {
            return address;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        public List<String> getFormattedAddress() {
            return formattedAddress;
        }

        public long getDistance() {
            return distance;
        }
    }

    public class Category implements Serializable {
        private static final long serialVersionUID = 1L;

        private Icon icon;
        private String name;
        private String shortName;

        public Icon getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public String getShortName() {
            return shortName;
        }
    }

    public class Contact implements Serializable {
        private static final long serialVersionUID = 1L;

        private String phone;
        private String formattedPhone;
        private String twitter;
        private String facebook;
        private String facebookUsername;
        private String facebookName;

        public String getPhone() {
            return phone;
        }

        public String getFormattedPhone() {
            return formattedPhone;
        }

        public String getTwitter() {
            return twitter;
        }

        public String getFacebook() {
            return facebook;
        }

        public String getFacebookUsername() {
            return facebookUsername;
        }

        public String getFacebookName() {
            return facebookName;
        }
    }

    public class Icon implements Serializable {
        private static final long serialVersionUID = 1L;

        private String prefix;
        private String suffix;

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }
    }
}
