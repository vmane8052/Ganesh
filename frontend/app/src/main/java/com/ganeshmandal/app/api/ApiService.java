package com.ganeshmandal.app.api;

import com.ganeshmandal.app.models.LoginResponse;
import com.ganeshmandal.app.models.Transaction;
import com.ganeshmandal.app.models.TransactionResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/login")
    Call<LoginResponse> login(@Body Map<String, String> credentials);

    @GET("api/transactions")
    Call<TransactionResponse> getTransactions(@Query("type") String type);

    @POST("api/transactions")
    Call<TransactionResponse> addTransaction(@Body Transaction transaction);

    @retrofit2.http.DELETE("api/transactions/{id}")
    Call<Void> deleteTransaction(@retrofit2.http.Path("id") String id);

    @POST("api/users")
    Call<LoginResponse> addUser(@Body com.ganeshmandal.app.models.User user);

    @GET("api/users")
    Call<com.ganeshmandal.app.models.UserListResponse> getUsers();

    @retrofit2.http.PUT("api/users/phone/{phone}")
    Call<LoginResponse> updateUser(@retrofit2.http.Path("phone") String phone, @Body com.ganeshmandal.app.models.User user);

    @retrofit2.http.DELETE("api/users/phone/{phone}")
    Call<Void> deleteUser(@retrofit2.http.Path("phone") String phone);
}
