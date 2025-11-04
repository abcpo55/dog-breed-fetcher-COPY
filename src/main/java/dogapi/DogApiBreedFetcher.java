package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     *
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) {

        String url = "https://dog.ceo/api/breed/" + breed + "/list";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new BreedNotFoundException(
                        "Unexpected HTTP status " + response.code() + " for " + url);
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new BreedNotFoundException("Empty response body from " + url);
            }

            String json = body.string();
            // Parse JSON using org.json
            JSONObject root = new JSONObject(json);
            String status = root.optString("status", "error");

            if (!"success".equalsIgnoreCase(status)) {
                throw new BreedNotFoundException("API returned status: " + status);
            }

            JSONArray message = root.optJSONArray("message");
            List<String> result = new ArrayList<>();

            if (message != null) {
                for (int i = 0; i < message.length(); i++) {
                    result.add(message.getString(i));  // elements are simple strings
                }
            }

            return result;

        } catch (IOException | JSONException e) {
            // Wrap any low-level failure as BreedNotFoundException, per your interface contract
            throw new BreedNotFoundException("Failed to fetch sub-breeds for breed: " + breed);
        }
    }
}
