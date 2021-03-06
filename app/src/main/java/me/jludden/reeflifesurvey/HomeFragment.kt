package me.jludden.reeflifesurvey

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.content.res.AppCompatResources.getDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import me.jludden.reeflifesurvey.fishcards.CardViewFragment
import me.jludden.reeflifesurvey.mapsites.MapViewFragment


/**
 * Created by Jason on 11/10/2017.
 *
 * Picture this
 * big ole whale comes into screen
 *
 * post-handler-runnable will delay a transition:
 * animate two buttons coming into the screen
 *  browse species
 *  select reefs
 *
 * swipe up on the screen at this point could jump straight into browse species, maybe, idk
 *
 */
class HomeFragment : Fragment() {
    companion object {
        const val TAG  = "me.jludden.ReefLifeSurvey.HomeFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.home_fragment, container, false)

        with(root) {
            findViewById<Button>(R.id.button_launch_browse_species).setOnClickListener({
                (activity as MainActivity).launchNewFragment(CardViewFragment::class.java)
                //todo make sure that there is at least some survey site or rando survey sites loading
            })
            findViewById<Button>(R.id.button_launch_select_sites).setOnClickListener({
                (activity as MainActivity).launchNewFragment(MapViewFragment::class.java)
            })
        }


        return root
    }

}