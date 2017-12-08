package me.jludden.reeflifesurvey.data

import android.content.Context
import android.util.Log
import io.reactivex.Observable
import me.jludden.reeflifesurvey.data.InfoCardLoader.parseSpeciesDetailsHelper
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.data.model.InfoCard.CardDetails
import org.json.JSONObject
import kotlin.collections.HashMap
import me.jludden.reeflifesurvey.data.SurveySiteType.*
import me.jludden.reeflifesurvey.data.model.SurveySiteList
import me.jludden.reeflifesurvey.data.utils.LoaderUtils
import java.io.InputStream
import java.util.ArrayList


/**
 * Created by Jason on 11/11/2017.
 *
 * Singleton Data Source that should be accessible across activities and threads
 */
class DataRepository private constructor(context: Context) {

    //Singleton that provides the getInstance() method
    companion object : SingletonHolder<DataRepository, Context>(::DataRepository)

    private val siteList : SurveySiteList
    // private val speciesString : String
    //private var speciesJSON : JSONObject
    //private val fishSpecies : Observable<InfoCard.CardDetails>
    //  private val fishStringStream : Observable<String>
    private var allSpeciesLoaded: Boolean = false
    private val speciesStream: InputStream

    //todo possibly sorted list better
    private var speciesCards: HashMap<String, CardDetails>


    init {

        //load survey site list immediately
        val surveySites = LoaderUtils.loadFishSurveySites(context)
        siteList = LoaderUtils.parseSurveySites(surveySites)
        siteList.loadFavoritedSites(context) //Load Saved Sites

     //   speciesString = LoaderUtils.loadStringFromDisk(R.raw.api_species, context)
//        speciesJSON = JSONObject(LoaderUtils.loadStringFromDisk(R.raw.api_species, context))
       // fishSpecies = loadFromDisk(R.raw.api_species, context)
   //     fishStringStream = loadFromDisk(R.raw.api_species, context)

        //speciesCards = loadFromDiskBlocking(R.raw.api_species, context)
        speciesStream = context.resources.openRawResource(R.raw.api_species)
        speciesCards = loadFromDiskBlocking()
    }

    //use Observable.from to emit items one at a time from a iterable
    fun getSurveySitesAll(type: SurveySiteType = CODES): Observable<SurveySiteList.SurveySite> {
        if(type != CODES) TODO()
        return Observable.fromIterable(siteList.SITE_CODE_LIST)
    }

    //todo improve memory usage
    // upon init just start the inputstream:  InputStream is = context.getResources().openRawResource(id);
    // https://stackoverflow.com/questions/43442480/rxjava-read-file-to-observable
    //
    //consider wrapping this in an async call (separate rxjava-async module):
    //https://github.com/ReactiveX/RxJava/wiki/Async-Operators
    //
    //the blocking method can be called with observable.just
    //and it can be defered with observable.defer until it is actually subscribed to:
    //Observable.defer(() -> Observable.just(slowBlockingMethod()))
    //
    fun getFishSpeciesAll(): Observable<CardDetails> {
        /*if(!allSpeciesLoaded){
            speciesCards = loadFromDiskBlocking()
        }*/
        return Observable.fromIterable(speciesCards.values)
    }

    fun getFishSpeciesForSite(site: SurveySiteList.SurveySite): Observable<CardDetails>
    {
        val speciesList = site.getSpeciesFound().keys()
        val speciesIDs = ArrayList<String>()

        //add each species to the list of ids
        while (speciesList.hasNext()) {
            speciesIDs.add(speciesList.next())
        }

        //map the id to the card
        return Observable.fromIterable(speciesIDs).map { speciesCards[it] }
    }

    //NOT asynchronously load the data but oh well at least its already on disk right todo delete and do it better
    fun loadFromDiskBlocking(): HashMap<String, CardDetails> {
        val speciesJSON = JSONObject(LoaderUtils.loadStringFromDiskHelper(speciesStream))
        val iter = speciesJSON.keys()

        val species = HashMap<String, CardDetails>()

        while (iter.hasNext()){
            val curKey = iter.next()
            val speciesData = speciesJSON.getJSONArray(curKey)

            val cardDetails = CardDetails(curKey)
            species.put(curKey, parseSpeciesDetailsHelper(cardDetails, speciesData))
        }
        Log.d("DataRepository", "loaded fish species: "+species.size)

        allSpeciesLoaded = true
        return species
    }

    /*
    fun getFishSpeciesAll(context: Context): Observable<CardDetails> {

        fishStringStream
                .map { str -> JSONObject(str)}
                .flatMap { json -> Observable.range(1,3) }
                .flatMap {
                    json -> ObservableOnSubscribe {

                    subscriber.onNext("1")

                }
                }

        Observable.create<> { ObservableOnSubscribe<> {
           result : String =
            subscriber.onNext(result)
        } }

        ObservableFrom


//        fun JSONObject.iterable(): MutableIterator<String> {
//            return this.keys()
//        }
//
//        val keys : MutableIterator<String> = speciesJSON.keys()
//                .fromIterable(speciesJSON)
        //.fromIterable<String>(speciesJSON.keys())

        //Flowable.

      /*
        Observable.create<JSONArray> {
            ObservableOnSubscribe<String> { subscriber ->

            }
        }
        .create(ObservableOnSubscribe<String> { subscriber ->
            search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    //  searchFor(query)
                }

                override fun onQueryTextChange(query: String): Boolean {
                    if (TextUtils.isEmpty(query)) {
                        //        clearResults()
                    }
                    subscriber.onNext(query)
                    return true
                }
            })
*/
        /*return Observable
                .fromCallable<MutableIterator<String>>({speciesJSON.keys()})
                .flatMap { iter ->

                }
                .map<JSONArray>{ key -> speciesJSON.getJSONArray(key) }
                .map<CardDetails>{ speciesData -> parseSpeciesDetailsHelper(CardDetails("1"), speciesData)}*/
    }
        */

    //interface for providing callbacks for accessing data
    //todo add onDataNotAvailable callbacks to each interface as well
    interface LoadSurveySitesCallBack : DataLoadCallback {
        fun onSurveySitesLoaded(sites : SurveySiteList)
    }

    interface LoadFishSpeciesJSONCallBack : DataLoadCallback {
        fun onFishSpeciesJSONLoaded(speciesJSON: JSONObject)
    }

    interface LoadFishCardCallBack : DataLoadCallback {
        fun onFishCardLoaded(card: CardDetails)
    }

    fun getSurveySites(type: SurveySiteType, callback: LoadSurveySitesCallBack) {
 /*       if(type == ALL_IDS) TODO()
        else if(type == CODES) {
            callback.onSurveySitesLoaded(siteList.SITE_CODE_LIST)
        } "fail"
*/
        callback.onSurveySitesLoaded(siteList)
    }

    fun getFishSpeciesJSON(callback: LoadFishSpeciesJSONCallBack) {
        TODO("not impl")
        //callback.onFishSpeciesJSONLoaded()
    }

    fun getFishCard(id: String, callback: LoadFishCardCallBack){
        val card: CardDetails? = speciesCards[id]
        if(card != null) callback.onFishCardLoaded(card)
        else callback.onDataNotAvailable(id)
    }
}

interface DataLoadCallback {
    fun onDataNotAvailable(reason: String)
}

enum class SurveySiteType {
    ALL_IDS,    //load all sites (you probably just want Codes)
    CODES, //load only one site per code (usually more useful than having a bunch of markers at the same spot)
    FAVORITES //load only favorite sites
}


//SingletonHolder implementing a double-checked locking algorithm
open class SingletonHolder<out T, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}