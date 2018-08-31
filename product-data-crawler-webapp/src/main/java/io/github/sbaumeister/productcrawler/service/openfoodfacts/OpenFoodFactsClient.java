package io.github.sbaumeister.productcrawler.service.openfoodfacts;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

@Service
public class OpenFoodFactsClient {

    private static final String BASE_URL = "https://ssl-api.openfoodfacts.org/api/v0.1/";

    private final Retrofit retrofit;
    private final OpenFoodFactsApi openFoodFactsApi;

    public OpenFoodFactsClient() {
        ObjectMapper mapper = createObjectMapper();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();
        openFoodFactsApi = retrofit.create(OpenFoodFactsApi.class);
    }

    public Product getProduct(String gtin) {
        Call<ProductMessage> call = openFoodFactsApi.getProduct(gtin);
        try {
            Response<ProductMessage> response = call.execute();
            return response.body().getProduct();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
