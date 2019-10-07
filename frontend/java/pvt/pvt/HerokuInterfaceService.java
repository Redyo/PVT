package pvt.pvt;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface HerokuInterfaceService {

    @POST("user/create")
    Call<ResponseBody> createUser(@Body JSONObject jsonObject);

    @POST("event/attend")
    Call<ResponseBody> addEventAttendee(@Body JSONObject jsonObject);


    @POST("event/unattend")
    Call<ResponseBody> deleteEventAttendee(@Body JSONObject jsonObject);

    @POST("event/chat-insert")
    Call<ResponseBody> insertEventChat(@Body JSONObject obj);

    @POST("event/create")
    Call<ResponseBody> createEvent(@Body JSONObject eventObject);

    @POST("user/location")
    Call<ResponseBody> createFavoriteLocation(@Body JSONObject obj);

    @POST("user/deleteFavLocation")
    Call<ResponseBody> deleteFavoriteLocation(@Body JSONObject obj);

    @POST("user/like")
    Call<ResponseBody> createLike(@Body JSONObject obj);

    @POST("user/unlike")
    Call<ResponseBody> deleteLike(@Body JSONObject obj);

    @POST("user/createChild")
    Call<ResponseBody> createChild(@Body JSONObject obj);

    @POST("user/deleteChild")
    Call<ResponseBody> deleteChild(@Body JSONObject obj);

    @POST("user/deleteAccount")
    Call<ResponseBody> deleteAccount(@Body JSONObject obj);

    @POST("event/deleteEvent")
    Call<ResponseBody> deleteEvent(@Body JSONObject obj);

    @GET("user/getUser")
    Call<ResponseBody> getUser(@Query("userID") long userID );

    @GET("user/getLikedUsers")
    Call<ResponseBody> getLikedUsers(@Query("userID") long userID);

    @GET("user/getLikers")
    Call<ResponseBody> getLikers(@Query("userID") long userID);

    @GET("user/getChildAge")
    Call<ResponseBody> getChildAge(@Query("childID") int childID);

    @GET("event/select-by-user")
    Call<ResponseBody> selectEventByUser(@Query("userId") long userId);

    @GET("user/children")
    Call<ResponseBody> getUserChildren(@Query("userID") long userID );

    @GET("user/getUserLocations")
    Call<ResponseBody> getUserLocations(@Query("userID") long userID );

    @GET("event/events-by-user")
    Call<ResponseBody> selectEventsCreatedByUser(@Query("userId") long userId );

    @GET("event/eventactivity")
    Call<ResponseBody> getEverythingForEventActivity(@Query("userId") long userId, @Query("eventId") int eventId );

    @GET("user/getLikes")
    Call<ResponseBody> getAmountOfLikes (@Query("id") long id );

    @GET("location/getLocation")
    Call<ResponseBody> getLocation(@Query("locationId") int locationId);

    @GET("location/userfavourite")
    Call<ResponseBody> checkIfFavourite(@Query("userId") long userId, @Query("locationId") int locationId);

    @GET("location/userfavourite3")
    Call<ResponseBody> checkIfFavourite3(@Query("userId") long userId, @Query("locationId") int locationId);

    @GET("location/nearYou")
    Call<ResponseBody> getLocationsNearYou(@Query("lat") double lat, @Query("lng") double lng);

    @GET("location/search")
    Call<ResponseBody> searchLocation(@Query("search") String locationName);

    @GET("event/select-by-location")
    Call<ResponseBody> selectEventsByLocation(@Query("locationId") int parkID);

    @GET("event/chat-select")
    Call<ResponseBody> selectEventChat(@Query("eventId") int eventId);

    @GET("event/select")
    Call<ResponseBody> selectEvent(@Query("eventId") int eventId);

    @GET("/event/select-by-user")
    Call<ResponseBody> selectEventsByUser(@Query("userId") long userID);

    @GET("/event/select-all")
    Call<ResponseBody> selectAllEvents();

    @GET("/user/getUserLocationsEvents")
    Call<ResponseBody> getUserLocationsEvents(@Query("userID") long userID);

    @GET("/user/getUserLocationsEventsTest")
    Call<ResponseBody> getUserLocationsEventsTest(@Query("userID") long userID);

    @GET("event/select-attendees")
    Call<ResponseBody> selectEventAttendees(@Query("eventId") int eventID);

    @GET("event/selectMaxEventId")
    Call<ResponseBody> getMaxEventId();

    @PUT("put")
    Call<ResponseBody> put();

    @DELETE("delete")
    Call<ResponseBody> delete();

    @HEAD("head")
    Call<ResponseBody> head();

}


