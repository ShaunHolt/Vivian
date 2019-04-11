package in.digibuddies.vivian;

/**
 * Created by Vikram on 18-01-2018.
 */

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Vikram on 11-01-2018.
 */
public class Message {
    public Message() {}

    public Message(String jsonString) throws JSONException {
        this(new JSONObject(jsonString));
    }

    public Message(JSONObject json) throws JSONException {
        if(json.isNull("id") == false) {
            this.id = json.getString("id");
        }

        if(json.isNull("conversationId") == false) {
            this.conversationId = json.getString("conversationId");
        }

        if(json.isNull("created") == false) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                this.created = dateFormat.parse(json.getString("created"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(json.isNull("from") == false) {
            this.from = json.getJSONObject("from").getString("id");
        }

        if(json.isNull("text") == false) {
            this.text = json.getString("text");
        }

        if(json.isNull("eTag") == false) {
            this.eTag = json.getString("eTag");
        }

        // TODO: Implement channelData, images, and attachments
    }


    public String id = null;
    public String conversationId = null;
    public Date created = null;
    public String from = null;
    public String text = null;
    public Object channelData = null;
    public List<String> images = null;
    public List attachments = null;
    public String eTag = null;

    public String toJSON() {
        String jsonString = "";

        try {
            JSONObject json = new JSONObject();
            json.put("id", this.id);
            json.put("conversationId", this.conversationId);
            if(this.created != null) {
                json.put("created", this.created.toString());
            }

            json.put("from", this.from);
            json.put("text", this.text);
            json.put("eTag", this.eTag);

            // channelData, images, and attachments are never encoded to JSON by this object.

            jsonString = json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonString;
    }
}