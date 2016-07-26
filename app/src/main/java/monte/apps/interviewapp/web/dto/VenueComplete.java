package monte.apps.interviewapp.web.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by monte on 2016-07-20.
 */

public class VenueComplete implements Serializable {
    private static final long serialVersionUID = -8165901645396923383L;
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
        private static final long serialVersionUID = -7738955143567843530L;
        private int count;
        private String summary;
    }

    public class Stats implements Serializable {
        private static final long serialVersionUID = 4940392415991093272L;
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
        private static final long serialVersionUID = 6033652655012150142L;
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
        private static final long serialVersionUID = 1962744813915409497L;
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
        private static final long serialVersionUID = -8892723721987822240L;
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
        private static final long serialVersionUID = 5880731576440273527L;
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
        private static final long serialVersionUID = -8233351081593039965L;
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
        private static final long serialVersionUID = 1032344688431325206L;
        private int tier;
        private String message;
        private String currency;

        public String getMessage() {
            return message;
        }
    }

    public class Likes implements Serializable {
        private static final long serialVersionUID = 1566367643330856851L;
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
        private static final long serialVersionUID = -5364521215311641745L;
        private String status;
        private boolean isOpen;
        private boolean isLocalHoliday;
        private List<Timeframe> timeframes = new ArrayList<>();

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
        private static final long serialVersionUID = -2244139314566078272L;

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
        private List<Open> open = new ArrayList<>();
    }

    public class Open implements Serializable {
        private static final long serialVersionUID = 5868951526275370174L;

        public String getRenderedTime() {
            return renderedTime;
        }

        private String renderedTime;
    }

    public class VenuePage implements Serializable {
        private static final long serialVersionUID = 3543857117470885074L;
        private String id;
    }

    public class Menu implements Serializable {
        private static final long serialVersionUID = 8460508878205397423L;
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
        private static final long serialVersionUID = 1881285556172438577L;
        private int count;
        private List<Reason> items = new ArrayList<>();

        public class Reason implements Serializable {
            private static final long serialVersionUID = 9001827949189317932L;
            private String summary;
            private String general;
            private String reasonName;
        }
    }

    public class User implements Serializable {
        private static final long serialVersionUID = 8169794388975655013L;
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
        private static final long serialVersionUID = 4221334956704712058L;
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
        private static final long serialVersionUID = 2091146143722219126L;
    }

    public class Page implements Serializable {
        private static final long serialVersionUID = -91461859029984458L;
    }

    public class Tips implements Serializable {
        private static final long serialVersionUID = -8213524720352475622L;
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
        private static final long serialVersionUID = 6802784343334813315L;
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
        private static final long serialVersionUID = -5207598998661603751L;
    }

    public class Phrase implements Serializable {
        private static final long serialVersionUID = -2964727966916008329L;
    }

    private class Popular implements Serializable {
        private static final long serialVersionUID = 1040874526780472482L;
    }

    private class PageUpdates implements Serializable {
        private static final long serialVersionUID = -4236923222594663082L;
    }

    private class Inbox implements Serializable {
        private static final long serialVersionUID = 7349850979311670839L;
    }

    private class Attributes implements Serializable {
        private static final long serialVersionUID = -3701353814952440769L;
        private List<Group<Attribute>> groups;
    }

    public class Attribute implements Serializable {
        private static final long serialVersionUID = 1088640567588083890L;
        private String displayName;
        private String displayValue;
    }
}
