package me.jludden.reeflifesurvey.search

import android.app.ActivityOptions
import android.content.Intent
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_fullscreen.*
import me.jludden.reeflifesurvey.data.model.SearchResult
import kotlinx.android.synthetic.main.search_results_item.view.*
import me.jludden.reeflifesurvey.data.model.SearchResultType
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.detailed.DetailsActivity
import me.jludden.reeflifesurvey.detailed.DetailsActivity.Companion.REQUEST_CODE
import me.jludden.reeflifesurvey.fishcards.CardViewFragment.animateView
import me.jludden.reeflifesurvey.search.SearchContract.View.Message
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Jason on 11/12/2017.
 *
 * SearchFragment Class
 *  implements the View portion of the SearchContract MVP architecture
 */
class SearchFragment : Fragment(), SearchContract.View {

    companion object {
        const val TAG: String = "SearchResultsFragment"
        const val MAX_ITEM_DISPLAY_COUNT : Long = 15
        fun newInstance() = SearchFragment()
    }

    override lateinit var presenter: SearchContract.Presenter
    override var isActive: Boolean = false
        get() = isAdded

    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: SearchResultsAdapter
    lateinit var searchChipHandler: SearchChipHandler
    internal var itemListener: SearchResultItemListener = object : SearchResultItemListener {
        override fun onItemClicked(item: SearchResult, v: View) {
            //presenter.onItemClicked(item)
            launchResultDetails(item, v) //todo handle here.. or pass presenter a view.. or setup a method (in adapter?) to return last clicked view
        }

        override fun onMaxItemsDisplayed() {
            setAdditionalMessage(Message.MAX_RESULTS_RETURNED)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.search_results_fragment, container, false)
        recyclerView = root.findViewById(R.id.search_results_cards) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        viewAdapter = SearchResultsAdapter(ArrayList(), itemListener)
        recyclerView.adapter = viewAdapter

        return root
    }

    fun setupSearchChips(handler: SearchChipHandler) {
        searchChipHandler = handler
    }

    //display some common search options
    fun displayChips(active: Boolean) {
        val chipContainer = view?.findViewById<LinearLayout>(R.id.chip_container) as LinearLayout
//        chipContainer.animate().alpha(1.0f)
        //or animateView(...

        if(!active) animateView(chipContainer, View.INVISIBLE, 0f, 200)
        else {
            // random chip contents
            //animate them in sequentially
            chipContainer.removeAllViews()
            animateView(chipContainer, View.VISIBLE, 1f, 400)

            var count = 0
            getRandomChips().forEach {
                val chip = layoutInflater.inflate(R.layout.search_chip, null) as TextView
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                params.gravity = LinearLayout.LayoutParams.
                params.setMargins(16, 16, 16, 16)
                chip.layoutParams = params
                chip.visibility = View.GONE
                val mt : String = it
                chip.text = it
//                chip.isClickable = true
                chip.setTextColor(resources.getColor(R.color.white))
                chip.setOnClickListener({
                    searchChipHandler.onChipClicked(mt)
//                    presenter.onQueryTextChange(mt)
//                    presenter.onQueryTextChange((it as TextView).text as String?)
                })
                animateView(chip, View.VISIBLE, 1f, 600 + (++count*300))
                chipContainer.addView(chip)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    //true - show an overlay loading bar, false - remove
    override fun setProgressIndicator(active: Boolean) {
        val progressBar =  view?.findViewById<View>(R.id.progress_overlay)
        if(active){
            animateView(progressBar, View.VISIBLE, 0.4f, 0)
            displayChips(false)
        }
        else animateView(progressBar, View.GONE, 0f, 200)
    }


    override fun setAdditionalMessage(message: Message) {
        val messageView = view?.findViewById<TextView>(R.id.additional_message);

        when(message){
            Message.NONE -> animateView(messageView, View.GONE, 0f, 200)
            Message.NO_RESULTS_RETURNED -> {
                animateView(messageView, View.VISIBLE, 0.4f, 200)
                messageView?.text = "No results found"
            }
            Message.MAX_RESULTS_RETURNED -> {
                Toast.makeText(context.applicationContext, "More results not loaded. Try narrowing your search criteria", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun addSearchResult(result: SearchResult) {
        //val curText = search_results_text.text
        val newResult = result.name + "_" + result.description
        //search_results_text.text = newText
        Log.d(TAG, "searchfragment addSearchResult "+newResult)

        viewAdapter.updateItems(element = result)
        setProgressIndicator(false)
    }

    override fun clearSearchResults() {
        viewAdapter.updateItems(list = ArrayList())
        setProgressIndicator(false)
        setAdditionalMessage(Message.NONE)
        displayChips(true)
    }

    /**
     * Handle launching a new activity to show item details
     *
     * todo mvp doesnt work here due to passing a view... delete from contract
     */
    override fun launchResultDetails(searchResult: SearchResult) {}

    //consider, instead of passing v, having the adapter return the imageview for the last clicked item
    fun launchResultDetails(searchResult: SearchResult, v: View) {
      Log.d(TAG, "searchfragment launchResultDetails CALLED")

        val intent = Intent()
        intent.setClass(activity, DetailsActivity::class.java)
        intent.putExtra(SearchResult.INTENT_EXTRA, searchResult)
        val options: ActivityOptions =
            ActivityOptions.makeSceneTransitionAnimation(activity,
                    Pair.create(v, getString(R.string.transition_launch_details)))
        startActivityForResult(intent, REQUEST_CODE, options.toBundle())
    }

    /**
     * SearchResultsAdapter class
     *
     * Some cool extension functions courtesy of
     * https://antonioleiva.com/extension-functions-kotlin/
     */
    class SearchResultsAdapter(
            initialList: MutableList<SearchResult>, private val itemListener: SearchResultItemListener)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var resultsList: MutableList<SearchResult> = initialList
        fun updateItems(element: SearchResult? = null, list: MutableList<SearchResult>? = null){
            if(list!=null) resultsList = list
            if(element!=null) resultsList.add(element)
            notifyDataSetChanged()

            if(itemCount >= MAX_ITEM_DISPLAY_COUNT) {
                itemListener.onMaxItemsDisplayed()
                Log.d(TAG, "searchfragment max results displayed")
            }
        }

        fun ViewGroup.inflate(layoutRes: Int) : View = LayoutInflater.from(context).inflate(layoutRes, this, false)

        override fun getItemCount() = resultsList.size

        /**
         * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
         * an item.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to
         * an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                = SearchResultsViewHolder(parent.inflate(R.layout.search_results_item))


        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the [ViewHolder.itemView] to reflect the item at the given
         * position.
         *
         * @param holder The ViewHolder which should be updated to represent the contents of the
         * item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        override fun onBindViewHolder(holder: ViewHolder, position: Int)
                = (holder as SearchResultsViewHolder).bind(resultsList.get(position), itemListener)
    }

    /**
     * ViewHolder class
     */
    class SearchResultsViewHolder(itemView: View) : ViewHolder(itemView) {
        fun bind(item: SearchResult, listener: SearchResultItemListener)
                = with(itemView) {
            results_card_image.loadSearchResultImage(item.imageURL, item.type)
            results_card_name.text = item.name
            results_card_description.text = item.description
            setOnClickListener({ listener.onItemClicked(item, itemView) })
        }

        fun ImageView.loadSearchResultImage(url: String, type: Enum<SearchResultType>){
            when {
                type == SearchResultType.SurveySiteLocation -> loadRes(R.mipmap.ic_place)
                url == "" -> loadRes(R.drawable.ic_menu_camera)
                else -> loadURL(url)
            }
        }

        fun ImageView.loadRes(resId: Int) = Picasso.with(context).load(resId).into(this)

        fun ImageView.loadURL(url: String) {
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.ic_menu_camera)
                    .error(R.drawable.ic_menu_camera)
                    .into(this)
        }

    }

    private fun getRandomChips() : Array<String> {
        val NUM_CHIPS = 3 //todo maybe more in larger / landscape
        val all = arrayOf("Spiny Lobster", "Shark", "Octopus", "Rockfish", "Stonefish", "Sphyrna lewini", "Blacktip",
                "Catshark", "Zebra Shark", "Zebrasoma", "Whaler", "Stingray", "Manta birostris", "Butterflyfish",
                "Seastar", "Urchin", "Nudibranch", "Sea slug", "Lionfish", "Rock Lobster", "Hermit Crab", "Mao Mao",
                "Maori Wrasse", "Sea Cucumber", "Blue Tang", "Clownfish", "Triplefin", "Razorfish", "Mantis Shrimp", "Abalone",
                "Angelfish", "Unicornfish", "Trumpetfish", "Sweetlips", "Parrotfish", "Triggerfish", "Klipfish", "Sally Lightfoot")
        val res = ArrayList<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Random().ints(0, all.size).distinct().limit(NUM_CHIPS.toLong()).forEach { res.add(all[it]) }
        } else {
            for(i in 0..NUM_CHIPS) {
                res.add(all[Random().nextInt(all.size)])
            }
        }

        return res.toTypedArray()
    }

    interface SearchResultItemListener {
        fun onItemClicked(item: SearchResult, v: View)
        fun onMaxItemsDisplayed()
    }

    interface SearchChipHandler {
        fun onChipClicked(chipText: String)
    }
}