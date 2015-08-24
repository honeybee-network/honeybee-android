package in.ureport.network;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.List;

import in.ureport.helpers.GsonDateDeserializer;
import in.ureport.models.rapidpro.Contact;
import in.ureport.models.rapidpro.Group;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by johncordeiro on 18/08/15.
 */
public class RapidProServices {

    private static final String TAG = "RapidProServices";

    private static final String ENDPOINT = "https://api.rapidpro.io/api/v1";
    public static final String API_KEY = "Token 8cbcf1080cac0455311f5fce636ab4cf7cc75eb9";

    private final RapidProApi service;

    public RapidProServices() {
        RestAdapter restAdapter = buildRestAdapter();
        service = restAdapter.create(RapidProApi.class);
    }

    public List<Group> loadGroups() {
        RapidProApi.Response<Group> response = service.listGroups(API_KEY);
        return response.getResults();
    }

    public Contact saveContact(Contact contact) {
        return service.saveContact(API_KEY, contact);
    }

    private RestAdapter buildRestAdapter() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new GsonDateDeserializer())
                .create();

        return new RestAdapter.Builder()
                    .setEndpoint(ENDPOINT)
                    .setConverter(new GsonConverter(gson))
                    .build();
    }

}