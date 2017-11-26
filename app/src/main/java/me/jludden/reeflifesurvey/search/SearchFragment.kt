package me.jludden.reeflifesurvey.search

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat.startActivity
import android.support.v4.app.ActivityCompat.startActivityForResult
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
import com.squareup.picasso.Picasso
import me.jludden.reeflifesurvey.data.SearchResult
import kotlinx.android.synthetic.main.activity_search_results_item.view.*
import kotlinx.android.synthetic.main.app_bar_main.*
import me.jludden.reeflifesurvey.data.SearchResultType
import me.jludden.reeflifesurvey.fishcards.DetailsViewFragment
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.detailed.DetailsActivity


/**
 * Created by Jason on 11/12/2017.
 *
 * SearchFragment Class
 *  implements the View portion of the SearchContract MVP architecture
 */
class SearchFragment : Fragment(), SearchContract.View {
    companion object {
        const val TAG: String = "SearchResultsFragment"
        fun newInstance() = SearchFragment()
    }

    override lateinit var presenter: SearchContract.Presenter
    override var isActive: Boolean = false
        get() = isAdded

    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: SearchResultsAdapter
    internal var itemListener: SearchResultItemListener = object : SearchResultItemListener {
        override fun onItemClicked(item: SearchResult, v: View) {
            //presenter.onItemClicked(item)
            launchResultDetails(item, v) //todo handle here.. or pass presenter a view.. or setup a method (in adapter?) to return last clicked view
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.activity_search_results_fragment, container, false)
        recyclerView = root.findViewById(R.id.search_results_cards) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        viewAdapter = SearchResultsAdapter(ArrayList(), itemListener)
        recyclerView.adapter = viewAdapter

        return root
    }

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    //TODO animateView when loading/clear data (see cardviewfragment.animateView)
    override fun setProgressIndicator(active: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //
    override fun addSearchResult(result: SearchResult) {
        //val curText = search_results_text.text
        val newResult = result.name + "_" + result.description
        //val newText = "$curText \n $newResult"
        //search_results_text.text = newText
        Log.d(TAG, "searchfragment addSearchResult "+newResult)

        viewAdapter.updateItems(element = result)
    }

    //todo
    override fun clearSearchResults() {
        viewAdapter.updateItems(list = ArrayList())
    }

    /**
     * Handle launching a new activity to show item details
     *
     * todo mvp doesnt work here due to passing a view... delete from contract
     */
    override fun launchResultDetails(searchResult: SearchResult) {}

    //todo should v be the imageview and not the whole item?
    //consider, instead of passing v, having the adapter return the imageview for the last clicked item
    fun launchResultDetails(searchResult: SearchResult, v: View) {
      Log.d(TAG, "searchfragment launchResultDetails CALLED")

        val intent = Intent()
        intent.setClass(activity,DetailsActivity::class.java)
        intent.putExtra(SearchResult.INTENT_EXTRA, searchResult)
        val options: ActivityOptions =
            ActivityOptions.makeSceneTransitionAnimation(activity,
                    Pair.create(v, getString(R.string.transition_launch_details)))
        startActivityForResult(intent, 123, options.toBundle()) //todo req code



       /* if(searchResult.type == SearchResultType.SurveySiteLocation){
            //val bottomSheet = findViewById(R.id.bottom_sheet) as BottomSheet
            val bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        else {
            (activity as SearchActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.search_results_container,
                            DetailsViewFragment.newInstance(searchResult), DetailsViewFragment.TAG)
                    .addToBackStack(null)
                    .commit()
        }*/
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
        }

        fun ViewGroup.inflate(layoutRes: Int): View {
            return LayoutInflater.from(context).inflate(layoutRes, this, false)
        }

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
                = SearchResultsViewHolder(parent.inflate(R.layout.activity_search_results_item))


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
            if(type == SearchResultType.SurveySiteLocation) loadRes(R.mipmap.ic_place)
            else if(url == "") loadRes(R.drawable.ic_menu_camera)
            else loadURL(url)
        }

        fun ImageView.loadRes(resId: Int) {
            Picasso.with(context).load(resId)
                    .into(this)
        /*   Glide.with(context)
                    .load(resId)
                    .transition(withCrossFade())
                    .into(this)*/
        }

        fun ImageView.loadURL(url: String) {
            Picasso.with(context)
                    .load(url)
                    .error(R.drawable.ic_menu_camera)
                    .into(this)

         /*   Glide.with(context)
                    .load(url)
                    .transition(withCrossFade())
                    .into(this)*/
        }

    }

    interface SearchResultItemListener {
        fun onItemClicked(item: SearchResult, v: View)
    }
}