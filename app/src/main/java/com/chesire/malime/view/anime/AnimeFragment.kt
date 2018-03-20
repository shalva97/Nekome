package com.chesire.malime.view.anime

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chesire.malime.AnimeStates
import com.chesire.malime.view.MalModelInteractionListener
import com.chesire.malime.R
import com.chesire.malime.mal.MalManager
import com.chesire.malime.models.Anime
import com.chesire.malime.models.UpdateAnime
import com.chesire.malime.util.SharedPref
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AnimeFragment : Fragment(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    MalModelInteractionListener<Anime> {

    private val animeItemsBundleId = "animeItems"

    private var disposables = CompositeDisposable()
    private lateinit var sharedPref: SharedPref
    private lateinit var username: String
    private lateinit var malManager: MalManager
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: AnimeViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = SharedPref(context!!)
        username = sharedPref.getUsername()
        malManager = MalManager(sharedPref.getAuth())

        viewManager = LinearLayoutManager(context!!)
        viewAdapter =
                AnimeViewAdapter(
                    ArrayList(),
                    ArrayList(),
                    sharedPref,
                    this
                )

        sharedPref.registerOnChangeListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maldisplay, container, false)

        swipeRefreshLayout = view.findViewById(R.id.maldisplay_swipe_refresh)
        swipeRefreshLayout.setOnRefreshListener {
            executeLoadAnime()
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.maldisplay_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        if (savedInstanceState == null) {
            executeLoadAnime()
        } else {
            viewAdapter.addAll(savedInstanceState.getParcelableArrayList(animeItemsBundleId))
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        disposables = CompositeDisposable()
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(animeItemsBundleId, viewAdapter.getAll())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        sharedPref.unregisterOnChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, id: String?) {
        if (id != null && id.contains(sharedPref.preferenceAnimeFilter)) {
            viewAdapter.filter.filter("")
        }
    }

    override fun onImageClicked(model: Anime) {
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(context, Uri.parse(model.malUrl))
    }

    override fun onPlusOneClicked(model: Anime, callback: () -> Unit) {
        val updateModel = UpdateAnime(model)
        updateModel.episode++

        // TODO: should have a preference to never ask
        if (updateModel.episode == updateModel.totalEpisodes && updateModel.status != AnimeStates.COMPLETED.id) {
            AlertDialog.Builder(context!!)
                .setTitle(R.string.malitem_update_series_complete_title)
                .setMessage(R.string.malitem_update_series_complete_body)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    updateModel.setToCompleteState()
                })
                .setOnDismissListener {
                    executeUpdateAnime(updateModel, callback)
                }
                .show()
        } else {
            executeUpdateAnime(updateModel, callback)
        }
    }

    override fun onNegativeOneClicked(model: Anime, callback: () -> Unit) {
        val updateModel = UpdateAnime(model)
        updateModel.episode--

        // TODO: should have a preference to never ask
        if (updateModel.episode < updateModel.totalEpisodes && updateModel.status == AnimeStates.COMPLETED.id) {
            AlertDialog.Builder(context!!)
                .setTitle(R.string.malitem_update_series_reverted_title)
                .setMessage(R.string.malitem_update_series_reverted_body)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    updateModel.setToWatchingState()
                })
                .setOnDismissListener {
                    executeUpdateAnime(updateModel, callback)
                }
                .show()
        } else {
            executeUpdateAnime(updateModel, callback)
        }
    }

    private fun executeLoadAnime() {
        disposables.add(malManager.getAllAnime(username)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    viewAdapter.addAll(result.second)
                    swipeRefreshLayout.isRefreshing = false
                },
                { _ ->
                    swipeRefreshLayout.isRefreshing = false
                }
            ))
    }

    private fun executeUpdateAnime(model: UpdateAnime, callback: () -> Unit) {
        disposables.add(malManager.updateAnime(model)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { _ ->
                    callback()
                    viewAdapter.updateItem(model)
                },
                { _ ->
                    callback()
                    Snackbar.make(
                        recyclerView,
                        String.format(
                            getString(R.string.malitem_update_series_failure),
                            model.title
                        ), Snackbar.LENGTH_LONG
                    ).show()
                }
            ))
    }

    companion object {
        const val tag = "AnimeFragment"
        fun newInstance(): AnimeFragment {
            val animeFragment = AnimeFragment()
            val args = Bundle()
            animeFragment.arguments = args
            return animeFragment
        }
    }
}