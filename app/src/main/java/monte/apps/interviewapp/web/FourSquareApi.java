package monte.apps.interviewapp.web;

import monte.apps.interviewapp.web.dto.PhotosDto;
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
            @Query("query") String query,
            @Query("v") long v);

    @GET("venues/{venueId}/photos")
    Call<PhotosDto> getVenuePhotos(
            @Path("venueId") String venueId,
            @Query("v") long v);
}
