package yesmen.cs2340.shoppingwithfriends;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class DatabaseInterfacer, is a wrapper for the database. Communicates with the database.
 *
 * @author Luka Antolic-Soban, Resse Aitken, Ratchapong Tangkijvorakul, Matty Attokaren, Sunny Patel
 * @version 1.1
 */
public class DatabaseInterfacer {


    private static final String REMOTE_IP = "http://wtfizlinux.com";

    private static final String LOGIN_URL = "/yesmen/login.php";
    private static final String REGISTER_URL = "/yesmen/register.php";
    private static final String ADD_FRIEND_URL = "/yesmen/add_friend.php";
    private static final String VIEW_FRIEND_URL = "/yesmen/view_friends.php";
    private static final String RETRIEVE_PROFILE_URL = "/yesmen/profile_retrieve.php";

    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_USER = "get_user";

    /**
     * Attempts to log in to the system
     * @param username Username
     * @param password User's Password
     * @return "Login Successful!" if successful, appropriate error message otherwise
     */
    public static String login(String username, String password) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        JSONObject json = queryDatabase(LOGIN_URL, params);
        try {
            if (json != null) {
                if (json.getInt(TAG_SUCCESS) == 1) {
                    String usernameID = json.getString(TAG_USER);
                    CurrentUser.setCurrentUser(new User(usernameID));
                }
                return json.getString(TAG_MESSAGE);
            } else return "Login Failure (Connection)";
        } catch (JSONException e) {
            e.printStackTrace();
            return "Login Failure (Technical)";
        }
    }

    /**
     * Registers a new user
     * @param username Desired Username
     * @param password Desired Password
     * @return "Username Successfully Added!" if successful, appropriate error otherwise
     */
    public static String register(String username, String password) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        JSONObject json = queryDatabase(REGISTER_URL, params);
        try {
            if (json != null) {
                return json.getString(TAG_MESSAGE);
            } else return "Register Failure (Connection)";
        } catch (JSONException e) {
            return "Register Failure (Technical)";
        }
    }

    /**
     *
     * @param friendUser User friend to add
     * @param myUser User self
     * @return String result of addFriend operation
     */
    public static String addFriend(String friendUser, String myUser) {
        friendUser = friendUser.toLowerCase();
        myUser = myUser.toLowerCase();

        // Building Parameters
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("FriendID", friendUser));
        params.add(new BasicNameValuePair("UserID", myUser));

        JSONObject json = queryDatabase(ADD_FRIEND_URL, params);

        try {
            if (json != null) {
                return json.getString(TAG_MESSAGE);
            } else return "Database error";
        } catch (JSONException e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    /**
     * Returns a String[] with String[0] being success or failure message
     * In event of success, String[1] - String[length-1] will contain the list of friends
     * @param myUser Current username
     * @return String[], String[0] will be "Success" in event of success, otherwise will contain
     * and error message.
     */
    public static String[] viewFriends(String myUser) {
        myUser = myUser.toLowerCase();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("UserID", myUser));
        String[] ret = new String[1];
        try {
            JSONObject json = queryDatabase(VIEW_FRIEND_URL, params);
            if (json == null) {
                ret[0] = "Database Error: no json object returned";
                return ret;
            }
            // check your log for json response
            JSONArray jArray = json.getJSONArray(TAG_MESSAGE);
            if (jArray == null) {
                ret[0] = "Database Error: No Friends Found";
                return ret;
            }

            if (json.getInt(TAG_SUCCESS) == 1) {
                ret = new String[jArray.length() + 1];
                for(int i = 0; i < jArray.length(); i++) {
                    ret[i + 1] = jArray.getString(i);
                }

                ret[0] = "Success";
            } else {
                ret[0] =  "No Friends.";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            ret[0] = "Database Error";
        }
        return ret;
    }



/*
    public static String[] retriveProfile(String myUser) {
        myUser = myUser.toLowerCase();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("UserID", myUser));
        String[] ret = new String[1];
        try {
            JSONObject json = queryDatabase(RETRIEVE_PROFILE_URL, params);
            if (json == null) {
                ret[0] = "Database Error: no json object returned";
                return ret;
            }
            // check your log for json response
            JSONArray jArray = json.getJSONArray(TAG_MESSAGE);
            if (jArray == null) {
                ret[0] = "Database Error: Something went wrong.";
                return ret;
            }

            if (json.getInt(TAG_SUCCESS) == 1) {
                ret = new String[jArray.length() + 1];
                for(int i = 0; i < jArray.length(); i++) {
                    ret[i + 1] = jArray.getString(i);
                }

                ret[0] = "Success";
            } else {
                ret[0] =  "Something went wrong.";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            ret[0] = "Database Error";
        }
        return ret;
    }*/


    public static User retrieveProfile(String username) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        JSONObject json = queryDatabase(RETRIEVE_PROFILE_URL, params);

        if (json != null) {
            try {
                JSONObject friend = json.getJSONObject(TAG_MESSAGE);
                User ret = new User(friend.getString("Username"));
                ret.setName(friend.getString("Name"));
                ret.setBiography(friend.getString("Biography"));
                ret.setEmail(friend.getString("Email"));
                ret.setLocation(friend.getString("Location"));
                ret.setPhoneNumber(friend.getString("Phonenum"));

                //Log.d("Database Query Successful!", friend);
                return ret;
            } catch (JSONException e) {
                Log.d("FUCK THIS SHIT", e.getMessage());
                e.printStackTrace();
            }
        }
        Log.d("FUCK THIS SHIT", "FUCK THIS SHIT");
        return new User("Fuck","Fuck","Fuck","Fuck","Fuck","Fuck");
    }

    /**
     * Validates the login attempt with the help of the query database.
     * @param URL URL ending of the desired php page to connect to
     * @param params List f Name Value pairs to pass to the database
     * @return json Returns a Json object from which to pull the pertinent information
     */
    private static JSONObject queryDatabase(String URL, List<NameValuePair> params) {
        try {

            JSONParser jsonParser = new JSONParser();
            JSONObject json = jsonParser.makeHttpRequest(REMOTE_IP + URL, "POST", params);

            Log.d("json Tag Message", json.getString(TAG_MESSAGE));

            if (json.getInt(TAG_SUCCESS) == 1) {
                Log.d("Database Query Successful!", json.toString());
                return json;
            } else {
                Log.d("Database Query Failure!", json.toString());
                return json;

            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
