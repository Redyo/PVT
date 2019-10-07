package controllers.tools;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SecuredAction extends Action.Simple {

    private static final String AUTHORIZATION = "authorization";
    //private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    //private static final String REALM = "Basic realm=\"Your Realm Here\"";

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {

        String accessToken = ctx.request().getHeader(AUTHORIZATION);
        JsonNode userId = null;

        if (accessToken == null) {
            //context.response().setHeader(WWW_AUTHENTICATE, REALM);
            return CompletableFuture.completedFuture(badRequest("Not authorized."));
        }

//        try {
//            userId = sendGet(accessToken);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return CompletableFuture.completedFuture(ok());

        //return delegate.call(ctx);
    }


    // HTTP GET request
//    private JsonNode sendGet(String token) throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//
//        String url = "https://graph.facebook.com/me?access_token=EAATGQcJVZA7oBAFoKyzZCLbWPwyMQbZA5k9LZAuY493hl546GcVXf7ImDiKWi9bUrbHlvVLBBsCfPVA3JYlKBH9RAhYS0nTkf4BZBOmMZCi5AJG9NHM8ZBEeqacsJeuy1JquBbA96ybbSSX2LNMlSHXbOKGguneRznADrmCJcZAwAFYrZChzrPpLG4wl79TC98F8ZD";
//
//        URL obj = new URL(url);
//        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
//
//        con.setRequestMethod("GET");
//        con.setRequestProperty("accept", "application/json");
//
//        int responseCode = con.getResponseCode();
//        //System.out.println("\nSending 'GET' request to URL : " + url);
//        //System.out.println("Response Code : " + responseCode);
//
//        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();
//        con.disconnect();
//
//        return mapper.readTree(response.toString());
//    }
}
