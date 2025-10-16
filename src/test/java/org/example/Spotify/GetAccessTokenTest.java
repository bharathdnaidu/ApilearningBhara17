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
    private String authorizationCode="AQCn1QE-EboUvL5US0FEkwHu3UW0G2BjqfJ4ui7iF5QlYp74kSApzPoIW6JVPcV5C9Rd2ktlAQLq0SjcwxRbnmeTOWEzzo580vTrDyRfCXMTAGXGeYYIjEGRfNHPQjvtKOb-b9liOuEuPDotpCWDCaP-tvsKFFV_DRH8kl5-AqksWvbo1e63__S3fbp3CB5TWymVuzXsHWhVx8qH3uXdJNLZSrewXNAO8QwpZekwbBCA79G-LlXPcSnWbIhbs9q55LBp4081VACt5wUcDOws2U3DA9EXa7iU1amFOEpHTg";
    private String client_id="d33768f0a3a94c88955ff29dcccd828a";
    private String client_secret="bdeda19f414d4463b82c535b24d4b18c";
    private String refresh_token="";
    
    
    @Test
    public void getAccessTokenFromSpotify()
    {
        ResponseBody responseBody = given().baseUri("https://accounts.spotify.com").
                header("Content-Type", "application/x-www-form-urlencoded").
                formParam("grant_type", "authorization_code").
                formParam("client_id", "d33768f0a3a94c88955ff29dcccd828a").
                formParam("client_secret", "bdeda19f414d4463b82c535b24d4b18c")
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
                formParam("code","AQBJa8ko4k3ax3JkWvJIeohSsSYh3v0s7jRcfrGq2tcDfHjr9E9K52MSAKWBh-c3k00550UWyd1uNPtts6qJBAQ40Z0_IvAhVmXufMkYDEqeysXfdG7sYAkrf-7sVIv8Wbo8wk6TcMP-y6apR60AaCBArD5HwcKGv3MJQTnc2ph1dUKMAuWQgUSB6UbUM6PcUHcbUjkW46i8zJS0-uZ7iuxXBnlQ7YJw2SvWBXhQNinTH2gPxyMOWQrQtPtTx4zK-xNaRwPlVD8Cy340G6UzJWVgXp1mDIJ9GYvZfmocAA").log().all().
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
