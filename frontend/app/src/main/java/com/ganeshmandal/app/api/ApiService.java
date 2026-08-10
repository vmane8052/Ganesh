package com.ganeshmandal.app.api;

import com.ganeshmandal.app.models.Donation;
import com.ganeshmandal.app.models.DonationListResponse;
import com.ganeshmandal.app.models.EventListResponse;
import com.ganeshmandal.app.models.LoginResponse;
import com.ganeshmandal.app.models.MandalEvent;
import com.ganeshmandal.app.models.SingleDonationResponse;
import com.ganeshmandal.app.models.SingleEventResponse;
import com.ganeshmandal.app.models.SingleTransactionResponse;
import com.ganeshmandal.app.models.Transaction;
import com.ganeshmandal.app.models.TransactionResponse;
import com.ganeshmandal.app.models.UploadResponse;
import com.ganeshmandal.app.models.User;
import com.ganeshmandal.app.models.UserListResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/login")
    Call<LoginResponse> login(@Body Map<String, String> credentials);

    @GET("api/transactions")
    Call<TransactionResponse> getTransactions(@Query("type") String type);

    @POST("api/transactions")
    Call<SingleTransactionResponse> addTransaction(@Body Transaction transaction);

    @DELETE("api/transactions/{id}")
    Call<Void> deleteTransaction(@Path("id") String id);

    @POST("api/users")
    Call<LoginResponse> addUser(@Body User user);

    @GET("api/users")
    Call<UserListResponse> getUsers();

    @PUT("api/users/phone/{phone}")
    Call<LoginResponse> updateUser(@Path("phone") String phone, @Body User user);

    @DELETE("api/users/phone/{phone}")
    Call<Void> deleteUser(@Path("phone") String phone);

    // --- Daily Events & Aarti Schedule ---
    @GET("api/events")
    Call<EventListResponse> getEvents();

    @POST("api/events")
    Call<SingleEventResponse> addEvent(@Body MandalEvent event);

    @PUT("api/events/{id}")
    Call<SingleEventResponse> updateEvent(@Path("id") String id, @Body MandalEvent event);

    @DELETE("api/events/{id}")
    Call<Void> deleteEvent(@Path("id") String id);

    // --- Donations (देणगीदार - रोख व वस्तू देणगी) ---
    @GET("api/donations")
    Call<DonationListResponse> getDonations();

    @POST("api/donations")
    Call<SingleDonationResponse> addDonation(@Body Donation donation);

    @PUT("api/donations/{id}")
    Call<SingleDonationResponse> updateDonation(@Path("id") String id, @Body Donation donation);

    @DELETE("api/donations/{id}")
    Call<Void> deleteDonation(@Path("id") String id);

    // --- Photo Upload (Cloudinary) ---
    @POST("api/upload")
    Call<UploadResponse> uploadPhoto(@Body Map<String, String> payload);
}
