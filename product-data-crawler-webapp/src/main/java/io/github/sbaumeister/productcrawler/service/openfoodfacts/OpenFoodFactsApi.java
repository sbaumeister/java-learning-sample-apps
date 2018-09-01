package io.github.sbaumeister.productcrawler.service.openfoodfacts;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface OpenFoodFactsApi {

    @GET("api/v0.1/product/{gtin}.json")
    Call<GtinSearchResult> getProductByGtin(@Path("gtin") String gtin);

    @GET("cgi/search.pl?search_simple=1&json=1&action=process&page=1")
    Call<NameSearchResult> getProductsByName(@Query("search_terms") String searchTerm);
}
