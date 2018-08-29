package io.github.sbaumeister.productcrawler.clients.openfoodfacts;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

interface OpenFoodFactsApi {

    @GET("product/{gtin}.json")
    Call<ProductMessage> getProduct(@Path("gtin") String gtin);
}
