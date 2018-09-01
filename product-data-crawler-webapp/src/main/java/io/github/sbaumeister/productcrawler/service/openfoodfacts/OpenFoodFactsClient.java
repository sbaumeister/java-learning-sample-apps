package io.github.sbaumeister.productcrawler.service.openfoodfacts;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class OpenFoodFactsClient {

    private static final Logger LOG = LoggerFactory.getLogger(OpenFoodFactsClient.class);

    private static final String BASE_URL = "https://ssl-api.openfoodfacts.org/";

    private final OpenFoodFactsApi openFoodFactsApi;

    public OpenFoodFactsClient() {
        ObjectMapper mapper = createJacksonObjectMapper();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();
        openFoodFactsApi = retrofit.create(OpenFoodFactsApi.class);
    }


    public Optional<Product> getProductByGtin(String gtin) {
        Call<GtinSearchResult> call = openFoodFactsApi.getProductByGtin(gtin);
        try {
            Response<GtinSearchResult> response = call.execute();
            GtinSearchResult body = response.body();
            if (response.isSuccessful() && body != null) {
                return Optional.ofNullable(body.getProduct());
            }
        } catch (IOException e) {
            LOG.error("Request failed", e);
        }
        return Optional.empty();
    }

    public List<Product> getProductsByName(String name) {
        Call<NameSearchResult> call = openFoodFactsApi.getProductsByName(name);
        try {
            Response<NameSearchResult> response = call.execute();
            NameSearchResult body = response.body();
            if (response.isSuccessful() && body != null) {
                return body.getProducts();
            }
        } catch (IOException e) {
            LOG.error("Request failed", e);
        }
        return Collections.emptyList();
    }


    private ObjectMapper createJacksonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
