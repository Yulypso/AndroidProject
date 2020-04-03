package com.github.androidproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity { //main activity

    private static final String BASE_URL = "https://api.epicsevendb.com/";

    //defining variables
    private RecyclerView recyclerView; //To display a collection of data
    private ListAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SharedPreferences sharedPreferences; //to save data
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Main view related to activity_main.xml

        sharedPreferences = getSharedPreferences(Constants.KEY_APPLICATION_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder() //create gson object to convert List into String (json type)
                .setLenient()
                .create();


        List<Hero> HeroList = getDataInCache(); //get data from cache

        if(HeroList != null){ //if data from cache is not null, we have data, we shows it
            showList(HeroList);
            Toast.makeText(getApplicationContext(),"Load from Cache", Toast.LENGTH_SHORT).show();
        } else {
            makeApiCall(); //if no data from cache, we make an ApiCall to get Data from API
        }
    }

    private List<Hero> getDataInCache() {
        String jsonHero = sharedPreferences.getString(Constants.KEY_HERO_LIST, null);

        if(jsonHero == null){
            return null;
        } else {
            Type listType = new TypeToken<List<Hero>>(){}.getType(); //deserialize list
            return gson.fromJson(jsonHero, listType);
        }
    }

    private void showList(List<Hero> heroList) {
        recyclerView = findViewById(R.id.recycler_view); //search for recycler_view in activity main by id
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this); //horizontal / vertical
        recyclerView.setLayoutManager(layoutManager);


        // define an adapter and give input into ListAdapter
        mAdapter = new ListAdapter(heroList); //Manages the data model and adapts it to the individual entries in the widget
        recyclerView.setAdapter(mAdapter); //Assigning it to the recycler
    }

    private void makeApiCall(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        EpicSevenApi EpicSevenApi = retrofit.create(EpicSevenApi.class);

        Call<RestEpicSevenResponse> call = EpicSevenApi.getHeroResponse();
        call.enqueue(new Callback<RestEpicSevenResponse>() {
            @Override
            public void onResponse(Call<RestEpicSevenResponse> call, Response<RestEpicSevenResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<Hero> heroList = response.body().getResults();
                    //Toast.makeText(getApplicationContext(),"API Success", Toast.LENGTH_SHORT).show();
                    saveList(heroList);
                    showList(heroList);
                }
            }

            @Override
            public void onFailure(Call<RestEpicSevenResponse> call, Throwable t) {
                showError();
            }
        });
    }

    private void saveList(List<Hero> heroList) {
        String jsonString = gson.toJson(heroList); //convert hero list into json format which is a String type

        sharedPreferences
                .edit()
                .putString(Constants.KEY_HERO_LIST, jsonString)  //clé, String
                .apply();

        Toast.makeText(getApplicationContext(),"List saved", Toast.LENGTH_SHORT).show();
    }

    private void showError(){
        Toast.makeText(getApplicationContext(),"API Error", Toast.LENGTH_SHORT).show();
    }
}

