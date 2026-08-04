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
}
