package me.jludden.reeflifesurvey;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import me.jludden.reeflifesurvey.model.SurveySiteList;

/**
 * Created by Jason on 8/28/2017.
 *
 * Retained fragment (survives configuration changes)
 * and headless (no UI component)
 * we'll use this to store data that will persist throughout the application lifecycle
 * such as survey site data
 *
 */

public class ReefLifeDataFragment extends Fragment implements LoaderManager.LoaderCallbacks<SurveySiteList> {

    public static final String TAG = ".jludden.SurveySites";
    private ReefLifeDataUpdateCallback mCallbacks;
    private SurveySiteList mSurveySites = new SurveySiteList(); //List of survey sites, fully parsed (api_site_surveys.json)
    private JSONObject mFishSpecies;                               //Fish species in raw json form (api_species.json)

    //private JSONObject mFishData =

    /**
     * define callback to retrieve data from this fragment
     * this can be implemented by the main activity, and the reference passed to fragments
     */
    public interface ReefLifeDataRetrievalCallback {
        SurveySiteList retrieveSurveySiteList();
        JSONObject retrieveFishSpecies();
    }

    /*
     * define callback to be notified when the data changes
     */
    public interface ReefLifeDataUpdateCallback {
        void onDataFragmentLoadFinished();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        getLoaderManager().initLoader(0, null, this);
    }

    public SurveySiteList getSurveySites() {
        return mSurveySites;
    }

    public JSONObject getFishSpecies() {
        if(mFishSpecies == null) {
            try {
                String result = LoaderUtils.loadStringFromDisk(R.raw.api_species, getContext());
                mFishSpecies = new JSONObject(result);
            } catch(IOException e) {
                Log.e("jludden.reeflifesurvey","error reading fish species file (api_species.json) "+e.toString());
            } catch(JSONException e) {
                Log.e("jludden.reeflifesurvey","error parsing fish species json (api_species.json) "+e.toString());
            }
        }
        return mFishSpecies;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        String errMsg = "";

        if(context instanceof ReefLifeDataFragment.ReefLifeDataUpdateCallback){
            mCallbacks = (ReefLifeDataFragment.ReefLifeDataUpdateCallback) context;
        } else {
            errMsg += context.toString() + "must implement" +
                    ReefLifeDataFragment.ReefLifeDataUpdateCallback.class.getName();
        }

        if(errMsg.length() > 0){
            throw new ClassCastException(errMsg);
        }
    }

//    public void setSurveySites(List<SurveySiteList.SurveySite> sites, boolean clearList) {
////        if (clearList) {
////            mSurveySites.clear();
////        }
////        mSurveySites.addAll(sites);
//
//        for(SurveySiteList.SurveySite site : sites){
//            mSurveySites.add(site);
//        }
//    }


    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<SurveySiteList> onCreateLoader(int id, Bundle args) {
        Log.d("jludden.reeflifesurvey"  ,"ReefLifeDataFragment OnCreateLoader");
        return new SurveySiteListLoader(getActivity());
    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<SurveySiteList> loader, SurveySiteList data) {
        Log.d("jludden.reeflifesurvey"  ,"ReefLifeDataFragment onLoadFinished loaderid: "+loader.getId()+" data length: "+data.ITEMS.size()+" realm hashmap size: "+data.ITEM_MAP.size()+" simple realm list size: "+data.SITE_CODE_LIST.size());
        if(data!=null) mSurveySites = data;

        //notify activity
        mCallbacks.onDataFragmentLoadFinished();
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<SurveySiteList> loader) {
        Log.d("jludden.reeflifesurvey"  ,"ReefLifeDataFragment OnLoaderReset TODO");
    }
}