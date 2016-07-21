package monte.apps.interviewapp.web.dto;

import android.view.Menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by monte on 2016-07-20.
 */

public class VenueComplete implements Serializable {
    private String id;
    private String name;
    private Contact contact;
    private Location location;
    private String canonicalUrl;
    private List<Category> categories = new ArrayList<>();
    private boolean verified;
    private Stats stats;
    private String url;
    private Price price;
    private boolean hasMenu;
    private Likes likes;
    private boolean like;
    private boolean dislike;
    private boolean ok;
    private double rating;
    private String ratingColor;
    private int ratingSignals;
    private Menu menu;
    private boolean allowMenuUrlEdit;
    private Specials specials;
    private Photos photos;
    private VenuePage venuePage;
    private Reasons reasons;
    private Page page;
    private HereNow hereNow;
    private int createdAt;
    private Tips tips;
    private List<String> tags = new ArrayList<>();
    private String shortUrl;
    private String timeZone;
    private Listed listed;
    private List<Phrase> phrases = new ArrayList<>();
    private Hours hours;
    private Popular popular;
    private PageUpdates pageUpdates;
    private Inbox inbox;
    private List<Object> venueChains = new ArrayList<>();
    private Attributes attributes;
    private Photo bestPhoto;

    public String getName() {
        return name;
    }

    public Contact getContact() {
        return contact;
    }

    public Location getLocation() {
        return location;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Stats getStats() {
        return stats;
    }

    public String getUrl() {
        return url;
    }

    public Price getPrice() {
        return price;
    }

    public boolean isHasMenu() {
        return hasMenu;
    }

    public Likes getLikes() {
        return likes;
    }

    public double getRating() {
        return rating;
    }

    public String getRatingColor() {
        return ratingColor;
    }

    public Menu getMenu() {
        return menu;
    }

    public Photos getPhotos() {
        return photos;
    }

    public VenuePage getVenuePage() {
        return venuePage;
    }

    public Page getPage() {
        return page;
    }

    public HereNow getHereNow() {
        return hereNow;
    }

    public Tips getTips() {
        return tips;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public Hours getHours() {
        return hours;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public Photo getBestPhoto() {
        return bestPhoto;
    }

    public class HereNow implements Serializable {
        private int count;
        private String summary;
    }

    public class Stats implements Serializable {
        private long checkinsCount;
        private long usersCount;
        private long tipCount;
        private long visitsCount;

        public long getUsersCount() {
            return usersCount;
        }

        public long getTipCount() {
            return tipCount;
        }

        public long getCheckinsCount() {
            return checkinsCount;
        }

        public long getVisitsCount() {
            return visitsCount;
        }
    }

    public class Location implements Serializable {
        private String address;
        private double lat;
        private double lng;
        private List<String> formattedAddress = new ArrayList<>();
        private String crossStreet;
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

        public String getCrossStreet() {
            return crossStreet;
        }
    }

    public class Category implements Serializable {
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

    public class Photos implements Serializable {
        private int count;
        private List<Group<Photo>> groups = new ArrayList<>();

        public int getCount() {
            return count;
        }

        public List<Group<Photo>> getGroups() {
            return groups;
        }
    }

    public class Icon implements Serializable {
        private String prefix;
        private String suffix;

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }
    }

    public class Price implements Serializable {
        private int tier;
        private String message;
        private String currency;

        public String getMessage() {
            return message;
        }
    }

    public class Likes implements Serializable {
        private int count;
        private String summary;

        public int getCount() {
            return count;
        }

        public String getSummary() {
            return summary;
        }
    }

    public class Hours implements Serializable {
        private String status;
        private boolean isOpen;
        private boolean isLocalHoliday;
        private List<Timeframe> timeframes = new ArrayList<Timeframe>();

        public String getStatus() {
            return status;
        }

        public boolean isOpen() {
            return isOpen;
        }

        public boolean isLocalHoliday() {
            return isLocalHoliday;
        }

        public List<Timeframe> getTimeframes() {
            return timeframes;
        }
    }

    public class Timeframe implements Serializable {
        public String getDays() {
            return days;
        }

        public boolean isIncludesToday() {
            return includesToday;
        }

        public List<Open> getOpen() {
            return open;
        }

        private String days;
        private boolean includesToday;
        private List<Open> open = new ArrayList<Open>();
    }

    public class Open implements Serializable {
        public String getRenderedTime() {
            return renderedTime;
        }

        private String renderedTime;
    }

    public class VenuePage implements Serializable {
        private String id;
    }

    public class Menu {
        private String type;
        private String label;

        public String getType() {
            return type;
        }

        public String getLabel() {
            return label;
        }

        public String getAnchor() {
            return anchor;
        }

        public String getUrl() {
            return url;
        }

        public String getMobileUrl() {
            return mobileUrl;
        }

        private String anchor;
        private String url;
        private String mobileUrl;
    }

    public class Reasons implements Serializable {
        private int count;
        private List<Reason> items = new ArrayList<>();

        public class Reason implements Serializable {
            private String summary;
            private String general;
            private String reasonName;
        }
    }

    public class User {
        private String id;
        private String firstName;
        private String lastName;
        private String gender;
        private Photo photo;

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public Photo getPhoto() {
            return photo;
        }

        public String getId() {
            return id;
        }
    }

    public class Group<T> implements Serializable {
        private String type;

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        private String name;
        private String summary;
        private int count;
        private List<T> items = new ArrayList<>();

        public List<T> getItems() {
            return items;
        }
    }

    public class Specials implements Serializable {
    }

    public class Page implements Serializable {
    }

    public class Tips implements Serializable {
        private int count;

        public List<Group<Tip>> getGroups() {
            return groups;
        }

        public int getCount() {
            return count;
        }

        private List<Group<Tip>> groups = new ArrayList<>();
    }

    public class Tip implements Serializable {
        private String id;
        private int createdAt;
        private String text;
        private String type;
        private String url;
        private String canonicalUrl;
        private Likes likes;
        private boolean like;
        private boolean logView;
        private int editedAt;
        private int agreeCount;
        private int disagreeCount;
        private User user;

        public String getText() {
            return text;
        }

        public User getUser() {
            return user;
        }

        public int getAgreeCount() {
            return agreeCount;
        }

        public int getDisagreeCount() {
            return disagreeCount;
        }

        public Likes getLikes() {
            return likes;
        }
    }

    public class Listed implements Serializable {
    }

    public class Phrase implements Serializable {
    }

    private class Popular implements Serializable {
    }

    private class PageUpdates implements Serializable {
    }

    private class Inbox implements Serializable {
    }

    private class Attributes implements Serializable {
        private List<Group<Attribute>> groups;
    }

    public class Attribute implements Serializable {
        private String displayName;
        private String displayValue;
    }
}
