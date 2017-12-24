package me.jludden.reeflifesurvey;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import me.jludden.reeflifesurvey.about.AboutActivity;
import me.jludden.reeflifesurvey.data.utils.SharedPreferencesUtils;
import me.jludden.reeflifesurvey.data.utils.StorageUtils;
import me.jludden.reeflifesurvey.detailed.DetailsActivity;
import me.jludden.reeflifesurvey.fishcards.CardViewFragment;
import me.jludden.reeflifesurvey.fishcards.CardViewFragment.CardViewSettings;

import me.jludden.reeflifesurvey.fullscreenquiz.FullScreenImageActivity;
import me.jludden.reeflifesurvey.data.model.InfoCard;
import me.jludden.reeflifesurvey.customviews.BottomSheet;
import me.jludden.reeflifesurvey.search.SearchActivity;
import me.jludden.reeflifesurvey.data.model.SurveySiteList;
import me.jludden.reeflifesurvey.mapsites.MapViewFragment;

import org.jetbrains.annotations.NotNull;

import static com.daimajia.androidanimations.library.Techniques.SlideInUp;
import static com.daimajia.androidanimations.library.Techniques.SlideOutDown;

public class MainActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener,
        CardViewFragment.OnCardViewFragmentInteractionListener,
        MapViewFragment.MapViewFragmentInteractionListener,
        BottomSheet.OnBottomSheetInteractionListener {


    private static final String TAG = "MainActivity";
    private FloatingActionButton mFAB, mBottomSheetButton;
    private FloatingActionButton[] mFABmenu;
    private boolean mFabMenuVisible = false;
    public static final int FAB_ONCLICK_ANIMATION_DURATION = 150; //snappy fab animations
    private SurveySiteList mSurveySiteList;

    //private ReefLifeDataFragment mDataFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.app_bar_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        /*Caused by: java.lang.IllegalStateException: This Activity already has an action bar supplied by the window decor. Do not request Window.FEATURE_SUPPORT_ACTION_BAR and set windowActionBar to false in your theme to use a Toolbar instead.*/
        setSupportActionBar(toolbar); //todo could this be the collapsing toolbar?


        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
//This will set Expanded text to transparent so it wount overlap the content of the toolbar
        collapsingToolbar.setExpandedTitleColor(Color.parseColor("#00FF0000"));//ContextCompat.getColor(this, R.color.transparent));

       // getSupportActionBar().setDisplayHomeAsUpEnabled(true); //todo testing
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        getSupportFragmentManager().addOnBackStackChangedListener(
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button
                    } else {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    }
                }
        });


        //  collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(this, R.color.black_semi_transparent));

////        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams) collapsingToolbar.getLayoutParams();
//
       // AppBarLayout toolbar_layout = (AppBarLayout) findViewById(R.id.app_bar);
        //toolbar_layout.setMinimumHeight(400);
//        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams) toolbar_layout.getLayoutParams();
//        params.height = 400;
//        toolbar_layout.setLayoutParams(params);

     //   toolbar_layout.setExpanded(false);
        // collapsingToolbar.setexpanded
        //collapsingToolbar.

        //set The custom text
     //   collapsingToolbar.setTitle("TEXTTTs");

//Set the color of collapsed toolbar text
     //   collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.white));

        Button quizButton = (Button) findViewById(R.id.toolbar_button_quiz);
        quizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchFullScreenQuizModeActivity();
            }
        });
        ((Button) findViewById(R.id.toolbar_button_store_offline)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardViewFragment viewFragment = (CardViewFragment) getSupportFragmentManager().findFragmentByTag(CardViewFragment.TAG);
                if (viewFragment != null && viewFragment.isVisible()) {
                    viewFragment.storeInLocal();
                }
            }
        });

        ToggleButton mToolbarButton_starred = (ToggleButton) findViewById(R.id.toolbar_button_filter_favorites);
        mToolbarButton_starred.setOnCheckedChangeListener(this);

        mBottomSheetButton = (FloatingActionButton) findViewById(R.id.bottom_sheet_fab);
        mBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO consolidate fab logic, doesnt make sense to pass the event when they could just override on the onclicklistener
                //If mapfragment is shown, pass the click event to the fragment to
                MapViewFragment mapFrag = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
                if (mapFrag != null && mapFrag.isVisible()) {
                    mapFrag.onFABclick();
                }
                //else, show a fab menu definitely
                else {
                    if(mFabMenuVisible) hideFABmenu();
                    else showFABmenu();
                }
            }
        });

        mFAB = (FloatingActionButton) findViewById(R.id.fab_menu);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO consolidate fab logic, doesnt make sense to pass the event when they could just override on the onclicklistener
                //If mapfragment is shown, pass the click event to the fragment to
                MapViewFragment mapFrag = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
                if (mapFrag != null && mapFrag.isVisible()) {
                    mapFrag.onFABclick();
                }
                //else, show a fab menu definitely
                else {
                    if(mFabMenuVisible) hideFABmenu();
                    else showFABmenu();
                }
            }
        });

        //Set up FAB Menu. make sure these are being cleaned up in onDestroy TODO
        FloatingActionButton mMiniFab1 = (FloatingActionButton) findViewById(R.id.fab_1);
        FloatingActionButton mMiniFab2 = (FloatingActionButton) findViewById(R.id.fab_2);
        FloatingActionButton mMiniFab3 = (FloatingActionButton) findViewById(R.id.fab_3);
        mFABmenu = new FloatingActionButton[]{mMiniFab1, mMiniFab2, mMiniFab3};
        mMiniFab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackbar.make(v, "FAB1 Launching Quiz Mode", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                launchFullScreenQuizModeActivity();
                hideFABmenu();
            }
        });
        mMiniFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "FAB2 Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //expandBottomSheet(null);
                hideFABmenu();
            }
        });
        mMiniFab3.setOnClickListener(new View.OnClickListener() { //TODO mehhh web view tho
            @Override
            public void onClick(final View v) {
                Snackbar.make(v, "FAB3 Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
              /*  WebView mWebview  = new WebView(getApplication());

               // mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript

                mWebview.setWebViewClient(new WebViewClient() {
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        Snackbar.make(v, "FAB3 Webview test", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                });

                mWebview .loadUrl("http://www.reddit.com");
                setContentView(mWebview );*/
                hideFABmenu();
            }
        });

        //Set up the bottom sheet
        BottomSheet bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            /**
             * Called when the bottom sheet changes its state.
             *
             * @param bottomSheet The bottom sheet view.
             * @param newState    The new state
             */
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d("jludden.reeflifesurvey"  , "Bottom Sheet OnStateChanged: "+newState);
            }

            /**
             * Called when the bottom sheet is being dragged.
             *
             * @param bottomSheet The bottom sheet view.
             * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset
             *                    increases as this bottom sheet is moving upward. From 0 to 1 the sheet
             *                    is between collapsed and expanded states and from -1 to 0 it is
             */
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                Log.d("jludden.reeflifesurvey"  , "Bottom Sheet onSlide");
            }
        });
        //bottomSheet.addTouchables();

 /*       DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/

        //start the home fragment
        hideClutter();
        launchUIFragment(new HomeFragment(), HomeFragment.TAG, true, false);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideFABmenu();
        return true;
    }

    @Override
    public void onBackPressed() {
        AppBarLayout toolbar = (AppBarLayout) findViewById(R.id.app_bar);
        toolbar.setExpanded(false,true);
        super.onBackPressed();
    }

    @Override
    protected void onPause(){
        Log.d(TAG,"MainActivity onPause");

        //onPause is called when details activity is launched
        //todo if you do this, make sure to regenerate the bottom sheet when we come back
        ((BottomSheet) findViewById(R.id.bottom_sheet)).clearView();//todo dont do this in onstop and onpause, probably

        super.onPause();
    /*    if(isFinishing()){ getSupportFragmentManager().beginTransaction()
                .remove(mDataFragment).commit();
        }*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
//        final MenuItem settings_item = menu.findItem(R.id.action_settings);
//        settings_item.getac


        // TODO: 11/9/2017 instead launch a new activity
        //final MenuItem search_item = menu.findItem(R.id.action_search);
        //final SearchView searchView = (SearchView) MenuItemCompat.getActionView(search_item);
        /*  searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                CardViewSettings.SEARCH_CONSTRAINT = newText.toLowerCase();
                CardViewFragment viewFragment = (CardViewFragment) getSupportFragmentManager().findFragmentByTag(CardViewFragment.TAG);
                if (viewFragment != null && viewFragment.isVisible()) {
                    viewFragment.onFilterApplied();
                }
                return true;
            }
        });
        //        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("jludden.reeflifesurvey"  , "Searchview OnFocusChange. hasfocus: " +hasFocus);

                if(!hasFocus) hideKeyboard(v);
            }
        });*/
        return true;
    }

    public void hideKeyboard(View v){
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d("jludden.reeflifesurvey"  , "menu item selected: " + id);
        Bundle options;

        switch (id) {
            // TODO: 11/9/2017 catch the search clicked item here
            case R.id.action_search: //Inspiration and implementation from the Plaid App
                View searchMenuView = findViewById(R.id.action_search);
                options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                            getString(R.string.transition_search_back)).toBundle();

//                startActivity(new Intent(this, SearchActivity.class), options);
                Log.d(TAG,"STARTING SEARCH ACTIVITY");

                startActivityForResult(new Intent(this, SearchActivity.class), SearchActivity.REQUEST_CODE, options);
                return true;
/*            case R.id.settings_opt_hide_menus:
                hideClutter();
                return true;*/
            case R.id.settings_opt_del_favorite_sites:
                showOkCancelDialog(this, getString(R.string.del_favorite_sites_message), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { // User clicked OK button;
                        SharedPreferencesUtils.clearFavSites(getApplicationContext());
                    }
                });
                return true;
         /*   case R.id.settings_opt_del_favorite_species: //todo impl
                showOkCancelDialog(this, getString(R.string.del_favorite_sites_message), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { // User clicked OK button;
                        SharedPreferencesUtils.clearFavSpecies(getApplicationContext());
                    }
                });
                return true;*/
            case R.id.settings_opt_del_offline_sites: //todo probably refactor to download settings activity
                showOkCancelDialog(this, getString(R.string.del_favorite_sites_message), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { // User clicked OK button;
                        StorageUtils.Companion.clearOfflineSites(MainActivity.this);
                    }
                });
                return true;
            case R.id.settings_opt_about:
                options =  ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
                startActivity(new Intent(this, AboutActivity.class), options);
                return true;
            case android.R.id.home: //todo support back button in action bar
                Log.d(TAG,"HOME OPTION SELECTED ");
                hideClutter();
                onBackPressed();
                // Navigate to parent activity
                // NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case(SearchActivity.REQUEST_CODE):
                Log.d(TAG,"main activity onactivity result FROM SEARCH ACTIVITY");
                break;
            case(DetailsActivity.REQUEST_CODE):
                Log.d(TAG, "main activity onactivity result FROM DETAIL ACTIVITY");
                CardViewFragment viewFragment = (CardViewFragment) getSupportFragmentManager().findFragmentByTag(CardViewFragment.TAG);
                if(viewFragment != null) viewFragment.mRecyclerView.getAdapter().notifyDataSetChanged(); //possibly update favorites icon
                break;
        }
    }

    /**
     * public method to launch a new fragment
     * @param fragmentClass
     */
    public void launchNewFragment(Class fragmentClass){
        if(fragmentClass == CardViewFragment.class){
            launchNewCardViewFragment("");
        }
        else if(fragmentClass == MapViewFragment.class){
            String tag = MapViewFragment.TAG;
            Fragment newFragment = getSupportFragmentManager().findFragmentByTag(tag);
            if (newFragment == null) {
                newFragment = MapViewFragment.newInstance();
            }

            //todo cleanup
            mFAB.hide();
            hideFABmenu();
            mBottomSheetButton.setVisibility(View.VISIBLE);
            AppBarLayout toolbar = (AppBarLayout) findViewById(R.id.app_bar);
            toolbar.setExpanded(false,true);
            launchUIFragment(newFragment, tag); //todo make it so there aren't multiple map fragments on the backstack
        }
    }

    //special method to launch this fragment because we are passing in a parameter
    private void launchNewCardViewFragment(@Nullable String code) {
        Fragment cardViewFrag = CardViewFragment.newInstance(CardViewFragment.CardType.Fish, code);
        launchUIFragment(cardViewFrag, CardViewFragment.TAG);
    }

    private void launchUIFragment(Fragment newFragment, String tag){
        launchUIFragment(newFragment, tag, false, true);
    }

    private void launchUIFragment(Fragment newFragment, String tag, boolean noAnim, boolean addToBackstack) {
        android.support.v4.app.FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        if(!noAnim) {
            tx.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right); }

        tx.replace(R.id.content_frame, newFragment, tag);
        if(addToBackstack) { tx.addToBackStack(null); }

        tx.commit();
    }

  /*  @Override
    public SurveySiteList retrieveSurveySiteList(){
        return mDataFragment.getSurveySites();
    }

    @Override
    public JSONObject retrieveFishSpecies() {
        return mDataFragment.getFishSpecies();
    }
*/
   /*  @Override
    public void onDataFragmentLoadFinished() {
       MapViewFragment mapFrag = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
        if (mapFrag != null && mapFrag.isVisible()) {
            mapFrag.onDataFragmentLoadFinished();
        }*/

        /*CardViewFragment viewFragment = (CardViewFragment) getSupportFragmentManager().findFragmentByTag(CardViewFragment.TAG);
        if (viewFragment != null && viewFragment.isVisible()) {
            viewFragment.();
        }
    }*/

    //todo - launch activity - right now just the full screen quiz mode
    public void launchFullScreenQuizModeActivity(){
        //get data from CardViewFragment TODO whole list of card data?
        Intent i = getIntent();
        InfoCard.CardDetails cardInfo = (InfoCard.CardDetails) i.getParcelableExtra("cardInfo");
        //
        //                if(cardInfo != null)   Log.d("jludden.reeflifesurvey"  ,"mFAB onclick. cardinfo name"+cardInfo.cardName);
        //
        //                //try passing some data to the new fragment
        //                ImageViewFragment imgFragment = new ImageViewFragment();
        //                Bundle b = new Bundle();
        //                b.putParcelable("cardInfo", cardInfo);
        //                imgFragment.setArguments(b);
        //                launchUIFragment(imgFragment);
        //launch full screen image activity on button click
        Intent intent = new Intent(MainActivity.this,
                FullScreenImageActivity.class);
        //intent.putExtra("cardInfo", cardInfo);
        startActivity(intent);
    }

    //region public accessor functions
    //public way for other fragments to get a reference to the floating action button
    public FloatingActionButton getFloatingActionButton(){
        //todo return different bottom sheet button depending on what fragment we're looking at
        //right now the mapfragment uses this. mapfrag != null but, during onCreate, is !isVisible()
//        MapViewFragment mapFrag = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
//        if (mapFrag != null && mapFrag.isVisible()) {
        return mBottomSheetButton;
//        }
//        else {
//            return mFAB;
//        }
    }

    /**
     *
     * @return true if the menu will be shown
     *      false if the menu is already shown
     *
     *      TODO disabling button
     */
    public boolean showFABmenu() {
        return false;
        /*
        if(!mFabMenuVisible){
            mFabMenuVisible = true;
            animateFAB(mFABmenu, true);
            return true;
        }
        else return false;*/
    }

    /**
     *
     * @return true if the menu will be hidden
     *      false if the menu is already hidden
     */
    public boolean hideFABmenu() {
        Log.d("jludden.reeflifesurvey"  , "Main Activity hideFabMenu()");
        if(mFabMenuVisible){
            mFabMenuVisible = false;
            animateFAB(mFABmenu, false);
            return true;
        }
        else return false;
    }

    /**
     * Animate and show/hide the floating action button menu
     * @param fabArray array of floating action buttons
     * @param animateIn whether to display the menu and make items clickable,
     *                     or to animate out the items and hide them
     */
    private void animateFAB(final FloatingActionButton[] fabArray, final boolean animateIn){
        int index = 0;
        Techniques anim = animateIn ? SlideInUp : SlideOutDown;
        int rotation = animateIn ? 90 : 0;
        ObjectAnimator.ofFloat(mFAB, "rotation", rotation).setDuration(FAB_ONCLICK_ANIMATION_DURATION).start(); //rotate the main FAB
        for(FloatingActionButton fab : fabArray) {
            YoYo.with(anim)
                    .interpolate(new AnticipateOvershootInterpolator()) //overshoots and bounces back
                    .duration(FAB_ONCLICK_ANIMATION_DURATION)
                    .withListener(getAnimatorListenerForFAB(fabArray,++index,animateIn))
                    .playOn(fab);
        }
    }

    /**
     *  Set up an animator listener to set the menu button visibility
     *      when it animates in, we show the buttons immediately
     *      when it animates out, we hide the buttons at the end of the animation
     * @param fabArray array of floating action buttons
     * @param fabIndex the index of this button in the array
     * @param animateIn true - the buttons will be shown, false - they will be hidden
     * @return
     */
    private Animator.AnimatorListener getAnimatorListenerForFAB(final FloatingActionButton[] fabArray, final int fabIndex, final boolean animateIn) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (animateIn) setFabMenuVisibleHelper(fabArray, fabIndex, true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!animateIn) setFabMenuVisibleHelper(fabArray, fabIndex, false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        };
    }

    /**
     * Helper function to set the locations and visibility of each floating menu button
     * @param fabArray array of floating action buttons
     * @param fabIndex the index of this button in the array
     * @param animateIn true - the buttons will be shown, false - they will be hidden
     */
    private void setFabMenuVisibleHelper(final FloatingActionButton[] fabArray, final int fabIndex, final boolean animateIn){
        int direction = animateIn ? 1 : -1;

        //todo remove: testing to try to animate fab menu from wherever the bottom sheet floating action button is. seems viable but not fantastic
//        CoordinatorLayout.LayoutParams mainFABparams = (CoordinatorLayout.LayoutParams) mFAB.getLayoutParams();
//        Log.d("jludden.reeflifesurvey"  ,"mainFAB bottomMargin: "+ mainFABparams.bottomMargin + " keyline: "+mainFABparams.keyline
//            +"  height: "+mainFABparams.height + " topMargin: "+ mainFABparams.topMargin + " measuredheight: "+mFAB.getMeasuredHeight() );
//
//        View btmSheet = findViewById(R.id.bottom_sheet);
//        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(btmSheet);
//
//        CoordinatorLayout.LayoutParams btmSheetFABparams = (CoordinatorLayout.LayoutParams) btmSheet.getLayoutParams();
//        Log.d("jludden.reeflifesurvey"  ,"btmSheetFABparams bottomMargin: "+ btmSheetFABparams.bottomMargin + " keyline: "+btmSheetFABparams.keyline
//                +"  height: "+btmSheetFABparams.height + " baseline: " + btmSheet.getBaseline() + "topMargin: "+ mainFABparams.topMargin
//                +" state: "+bottomSheetBehavior.getState() + " measured height: " +btmSheet.getMeasuredHeight()
//        );
//
//        int offset = 110;//mFAB.getMeasuredHeight();
//        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) offset += btmSheet.getMeasuredHeight();//340; //bottomSheetBehavior.getMeasuredHeight();
//        else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) offset += 80; //bottomSheetBehavior.getPeekHeight();
//        Log.d("jludden.reeflifesurvey"  ,"btmSheetFABparams offset: "+offset);

        FloatingActionButton fab = fabArray[fabIndex-1];
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fab.getLayoutParams();
//        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) fab.getLayoutParams();

        Log.d("jludden.reeflifesurvey"  ,"bottomMargin before: "+ layoutParams.bottomMargin);
        layoutParams.bottomMargin = (int) (layoutParams.bottomMargin + direction * fab.getHeight() * (1.7 * (fabIndex))); //add or subtract from the current bottom margin
        //layoutParams.bottomMargin = (int) (offset + direction * fab.getHeight() * (1.7 * (fabIndex))); //add or subtract from an offset - the estimated position of the main FAB

        //if(!animateIn) layoutParams.bottomMargin = 40;
        Log.d("jludden.reeflifesurvey"  ,"bottomMargin after: "+ layoutParams.bottomMargin);
        fab.setLayoutParams(layoutParams);
        fab.setClickable(animateIn);
        fab.setVisibility(animateIn ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Show the initial survey site location in just the top part of the bottom sheet
     * TODO merge with expand bottom sheet? they should at the very least set the same info
     * @param siteInfo
     */
    public void peekBottomSheet(final SurveySiteList.SurveySite siteInfo) {
        //Set up the bottom sheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        final Button launch_species = (Button) findViewById(R.id.bottom_sheet_launch_species);
        launch_species.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Bottom Sheet Launch_Species clicked.. shown?: " + v.isShown());

                launchNewCardViewFragment(siteInfo.getCode());
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//                mBottomSheetButton.hide();
                mBottomSheetButton.setVisibility(View.GONE);


                //todo refactor this
                //todo this does not work properly if they are in details view
                //if the detailsviewfragment is showing, and they click the top of bottom sheet,
                //navigate them back to the mapview
                /*MapViewFragment mapFrag = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(MapViewFragment.TAG);
                if (mapFrag != null && !mapFrag.isVisible()) {
                    Log.d(TAG, "Bottom Sheet TEST1 PASSED");
                    mBottomSheetButton.setVisibility(View.VISIBLE);
                    launch_species.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_view_in_list, 0, 0, 0);
                    launch_species.setText(R.string.bottom_sheet_secondary_button_browse);
//                    launchUIFragment(mapFrag, MapViewFragment.TAG);

                    hideClutter();
                    onBackPressed();
                } else if (mapFrag != null) { //launch browse fish details for this site
                   //AppBarLayout toolbar = (AppBarLayout) findViewById(R.id.app_bar);
                    //toolbar.setExpanded(true,true);
                    //addSiteLocationsToToolbar();
                    //mFAB.show();
                    launch_species.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_open_in_map, 0, 0, 0);
                    launch_species.setText(R.string.bottom_sheet_secondary_button_map);
                    launchNewCardViewFragment(siteInfo.getCode());
                }*/
            }
        });
        //if added to favs, set the fab to remove

        ((BottomSheet) bottomSheet).loadNewSite(siteInfo);


        //show some info like num sites etc

        final TextView topText = (TextView) findViewById(R.id.bottom_sheet_top);
        topText.setText(siteInfo.getDisplayName());
        topText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo state_expanded or whole fullscreen view?
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            }
        });

    }

    public FloatingActionButton[] getFABmenu(){
        return mFABmenu;
    }

    /**
     * Called from CardViewFragment
     * And from Carousel Entries
     * to launch a details mode for the fish card
     * @param cardDetails
     * @param sharedElement view to animate transition
     */
    @Override
    public void onFishDetailsRequested(InfoCard.CardDetails cardDetails, @Nullable View sharedElement) {
        Log.d(TAG, "MainActivity onFishDetailsRequested: "+cardDetails.toString());

        //launch details
        Intent intent = new Intent(MainActivity.this,
                DetailsActivity.class);
        intent.putExtra(InfoCard.CardDetails.INTENT_EXTRA, cardDetails);
        if(sharedElement != null) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    new Pair<View, String>(sharedElement, getString(R.string.transition_launch_details)));
            startActivityForResult(intent, DetailsActivity.REQUEST_CODE, options.toBundle());
        } else {
            startActivityForResult(intent, DetailsActivity.REQUEST_CODE);
        }
    }

    //todo move to bottomsheet class
    //todo restart image sliders when power on (onResume, see IntroPageTwoFragment)
    @Override
    protected void onStop() {
        ((BottomSheet) findViewById(R.id.bottom_sheet)).clearView();
        super.onStop();
    }


    /**
     * 11/9/17 No longer using default searchview widget
     *
     * Override the default dispatchTouchEvent to hide the keyboard from the search box, if it is up
     * todo there is another touch listener in this class that handles hiding the fab menu. reconcile?
     * @param ev motion event passed on to super
     * @return result from super.dispatch
     */
  /*  @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SearchView searchView = (SearchView) findViewById(R.id.action_search);
        searchView.clearFocus();
        return super.dispatchTouchEvent(ev);
    }*/

    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG,"button pressed: "+buttonView.getId()+" is checked: "+isChecked);
        CardViewFragment viewFragment = (CardViewFragment) getSupportFragmentManager().findFragmentByTag(CardViewFragment.TAG);

        switch(buttonView.getId()){
            case(R.id.toolbar_button_filter_favorites):
                CardViewSettings.FILTER_FAVORITES = isChecked;
                if (viewFragment != null && viewFragment.isVisible()) {
                    if(CardViewSettings.LOAD_ALL) viewFragment.onFilterApplied(); //already have everything loaded, just apply filters
                    else {
                        CardViewSettings.LOAD_ALL = true;
                        viewFragment.onLoadMore(false);
                    }
                }
                break;
/*            case(R.id.toolbar_button_store_offline):
                if (viewFragment != null && viewFragment.isVisible()) {
                    viewFragment.storeInLocal();
                }
                break;*/
            default: //handle survey site location button pressed
                Log.d(TAG,"unhandled toggle button (#"+buttonView.getId()+") pressed: "+buttonView.getText());

        }
    }

    // jump to full screen mode yeeaah
    public void hideClutter(){
        mFAB.hide();
        hideFABmenu();
        mBottomSheetButton.setVisibility(View.GONE);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        AppBarLayout toolbar = (AppBarLayout) findViewById(R.id.app_bar);
        toolbar.setExpanded(false,true);

        //todo stuff below works, but I think i want to keep the toolbar for now

        //trying everything to get this bar to hide
        //getSupportActionBar().hide();
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        //collapsingToolbar.setVisibility(View.GONE);

        //toolbar.setVisibility(View.GONE);

        /*View mDecorView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mDecorView = getWindow().getDecorView();
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            );
        }*/
               /* | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                * | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                * | View.SYSTEM_UI_FLAG_IMMERSIVE
                */
    }

    @Override
    public void onImageSliderClick(@NotNull InfoCard.CardDetails card, @NotNull View sharedElement) {
        //todo the imageview from the image slider does not transition well into new activity, so implement an animation later if you want
        onFishDetailsRequested(card, null);
    }

    /**
     * Shows a dialog with a positive and negative button, and you can pass a clicklistener for the positive button
     * @param activity current activity
     * @param message
     * @param onPositiveClick
     */
    public static void showOkCancelDialog(Activity activity, String message, DialogInterface.OnClickListener onPositiveClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setTitle(R.string.app_name);
        builder.setPositiveButton(R.string.ok, onPositiveClick);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //Shows a dialog with just an OK button and a message. TODO consider using snackbars instead
    public static void showSimpleDialogMessage(Activity activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setTitle(R.string.app_name);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
