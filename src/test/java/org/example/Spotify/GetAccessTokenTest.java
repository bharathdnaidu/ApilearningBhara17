package org.example.Spotify;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import org.testng.annotations.Test;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

public class GetAccessTokenTest {
    
    public String access_token="";
    public String token_type="";
    public static final String ARTIST_ID="4Z8W4fKeB5YxbusRsdQVPb";
    private String authorizationCode="";
    private String client_id="";
    private String client_secret="";
    private String refresh_token="";
    
    
    @Test
    public void getAccessTokenFromSpotify()
    {
        ResponseBody responseBody = given().baseUri("https://accounts.spotify.com").
                header("Content-Type", "application/x-www-form-urlencoded").
                formParam("grant_type", "authorization_code").
                formParam("client_id", client_id).
                formParam("client_secret", client_secret)
                .when().post("/api/token").
                then().log().all().
                statusCode(200)
                .extract().response().getBody();
          access_token= responseBody.path("access_token").toString();
          token_type =responseBody.path("token_type").toString();
    }
    
    @Test(dependsOnMethods = "getAccessTokenFromSpotify")
    public void getArtistData()
    {
        System.out.println(ARTIST_ID);
        System.out.println(access_token);
        
        given().baseUri("https://api.spotify.com").
                pathParam("artistId",ARTIST_ID).
                header("Authorization","Bearer "+access_token).
        header("Content-Type", "application/json") // optional, match Postman
                .header("Accept", "application/json").log().all().
                when().get("/v1/artists/{artistId}").then().statusCode(200).log().all();
                
    }
    
    
    @Test
    public void ValidateAccessTokenResponseFromAuthorizationCode()
    {
        RestAssured.config=RestAssured.config().logConfig(LogConfig.logConfig().blacklistHeader("Authorization"));
      String base64Encoder=Base64.getEncoder().encodeToString((client_id+":"+client_secret).getBytes());
        Response respnse= given().baseUri("https://accounts.spotify.com").
        header("Authorization","Basic "+base64Encoder).
                header("Content-Type","application/x-www-form-urlencoded").
                formParam("grant_type","authorization_code").
                formParam("code",authorizationCode).log().all().
                formParam("redirect_uri","https://localhost:8080").
                
                when().post("/api/token").then().log().all().statusCode(200).extract().response();
        
        JsonPath path =respnse.getBody().jsonPath();
        access_token=path.get("access_token");
        refresh_token=path.get("refresh_token");
        token_type=path.get("Bearer");
    }
    
    @Test(dependsOnMethods = "ValidateAccessTokenResponseFromAuthorizationCode")
    public void getAccessTokenUsingRefreshToken()
    {
       RestAssured.config=RestAssured.config().logConfig(LogConfig.logConfig().blacklistHeader("Authorization"));
       String encodedClientidandClientSecret= Base64.getEncoder().encodeToString((client_id +":"+ client_secret).getBytes());
       Response response=given().baseUri("https://accounts.spotify.com").
                header("Content-Type","application/x-www-form-urlencoded").
                header("Authorization","Basic "+encodedClientidandClientSecret).
                formParam("grant_type","refresh_token").
                formParam("refresh_token",refresh_token).log().all().
                when().post("/api/token").then().log().all().statusCode(200).extract().response();
       
       access_token=response.jsonPath().get("access_token");
    }
    
    
    
    
}
