package in.youngpioneer.dps.notificationMessages;

/**
 * Created by vyomkeshjha on 08/08/16.
 * this is the data struture that stores the data retrieved from the sqlite database of notifications
 * stores in form of hashmaps stored in arrayList
 */
public class DbDataMap {


        private String timestamp;
        private String title;
        private String message;
        private String ImageUrl;
        private String ImageRedirectUrl;


        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getImageUrl() {
            return ImageUrl;
        }

        public void setImageUrl(String imageUrl) {
            ImageUrl = imageUrl;
        }
        public int getTimestampAsInteger()
        {
            return Integer.parseInt(this.timestamp);
        }

    public String getImageRedirectUrl() {
        return ImageRedirectUrl;
    }

    public void setImageRedirectUrl(String imageRedirectUrl) {
        ImageRedirectUrl = imageRedirectUrl;
    }
}
