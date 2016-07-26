package monte.apps.interviewapp.web;

import monte.apps.interviewapp.web.dto.PhotosDto;
import monte.apps.interviewapp.web.dto.VenueDto;
import monte.apps.interviewapp.web.dto.VenuesDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by monte on 2016-07-17.
 */

public interface FourSquareApi {
    @GET("venues/search")
    Call<VenuesDto> findVenues(
            @Query("ll") String latLng,
            @Query("query") String query);

    @GET("venues/search")
    Call<VenuesDto> findVenuesNear(
            @Query("near") String near,
            @Query("query") String query);

    @GET("venues/{venueId}/photos")
    Call<PhotosDto> getVenuePhotos(
            @Path("venueId") String venueId);

    @GET("venues/{venueId}")
    Call<VenueDto> getVenue(
            @Path("venueId") String venueId);
}
