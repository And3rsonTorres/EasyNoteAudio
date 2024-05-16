/***********************************************
 Authors: Arthur Rosa,Tashia Boddu, Anderson Torres
 Date: 12/18/23
 Purpose: Our mission is to develop an app that makes notetaking convenient and simple.
 We want to create an app that is accessible to people therefore we have features such as speech-to-text and text-to-speech so the user can take and listen to notes mostly hand-free.
 We also value organization, so users can create and name multiple notes all in one place.
 What Learned: SharedPreference
 One way to store simple data inside the android ecosystem without affecting other apps.
 Integrating Google’s Speech To Text API
 Learned how to use Google’s API for Speech To Text
 Learned about adjustResize
 AdjustResize is a parameter for windowSoftInputMode and it allows the noteTaking Features View to be on top of the keyboard because the layout is adjusted when the keyboard is present.
 SQlite
 Learned how to integrate SQLite for database handling
 Team Management
 We were able to communicate and assist each other with the project and designate task for each of us to complete
 Github
 We got more practice on collaborating with github and we are sure this will help us in the future

 Sources of Help: All previous assignments and android dev website.
 for help: tutors, web sites, lab assistants etc.> Time Spent (Hours): <5hr discussing features, 48hr of coding and 60hr debugging and optimizing> ***********************************************/
/*
Mobile App Development I -- COMP.4630 Honor Statement
The practice of good ethical behavior is essential for maintaining good order in the classroom, providing an enriching learning experience for students, and training as a practicing computing professional upon graduation. This practice is manifested in the University's Academic Integrity policy. Students are expected to strictly avoid academic dishonesty and adhere to the Academic Integrity policy as outlined in the course catalog. Violations will be dealt with as outlined therein. All programming assignments in this class are to be done by the student alone unless otherwise specified. No outside help is permitted except the instructor and approved tutors.
I certify that the work submitted with this assignment is mine and was generated in a manner consistent with this document, the course academic policy on the course website on Blackboard, and the UMass Lowell academic code.
Date:12/18/23
Names: Arthur Rosa,Tashia Boddu, Anderson Torres
*/
package com.mobileapp.easynoteaudio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);

        //get a reference to the navigation controller from navigation host
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        //create an “AppBarConfiguration” object
        AppBarConfiguration.Builder builder = new
                AppBarConfiguration.Builder(navController.getGraph());

        AppBarConfiguration appBarConfiguration = builder.build();

        //link “AppBarConfiguration” to the toolbar
        NavigationUI.setupWithNavController(mToolbar, navController,
                appBarConfiguration);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.topbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.helpFragment || item.getItemId() == R.id.ToDoList || item.getItemId() == R.id.noteManager) {
            NavController navController =
                    Navigation.findNavController(this, R.id.nav_host_fragment);
            return NavigationUI.onNavDestinationSelected(item,
                    navController) || super.onOptionsItemSelected(item);
        }
        // Handle action bar item clicks here. The action bar will
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}