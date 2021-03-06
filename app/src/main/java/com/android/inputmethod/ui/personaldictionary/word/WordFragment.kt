package com.android.inputmethod.ui.personaldictionary.word

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.inputmethod.latin.R
import com.android.inputmethod.latin.databinding.FragmentWordBinding
import com.android.inputmethod.ui.components.recycleradapter.EventAdapter
import com.android.inputmethod.ui.personaldictionary.word.adapter.WordContextViewHolder
import com.android.inputmethod.usecases.RemoveWordContextUseCase
import com.android.inputmethod.usecases.WordContextUseCase
import com.elevate.rxbinding3.swipes
import com.rawa.recyclerswipes.RecyclerSwipes
import com.rawa.recyclerswipes.SwipeDirection
import com.rawa.recyclerswipes.attachTo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class WordFragment : Fragment(), WordView {

    private lateinit var disposable: Disposable
    private val factory = WordContextViewHolder.DictionaryWordViewHolderFactory
    private val adapter = EventAdapter(factory)

    private lateinit var database: PersonalDictionaryDatabase
    private lateinit var presenter: WordPresenter

    private lateinit var binding: FragmentWordBinding

    private val args by navArgs<WordFragmentArgs>()
    override val wordId by lazy { args.wordNavArg.wordId }

    private lateinit var swipeCallback: RecyclerSwipes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = PersonalDictionaryDatabase.getInstance(context!!)

        val wordContextUseCase = WordContextUseCase(database)
        val deleteWordContextUseCase = RemoveWordContextUseCase(database)
        presenter = WordPresenter(this, wordContextUseCase, deleteWordContextUseCase)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentWordBinding.inflate(inflater, container, false).also {
                binding = it
            }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvWordWordcontexts.layoutManager = LinearLayoutManager(context!!)
        binding.rvWordWordcontexts.adapter = adapter

        swipeCallback = RecyclerSwipes(SwipeDirection.RIGHT to R.layout.swipe_right_delete)
        swipeCallback.attachTo(binding.rvWordWordcontexts)
    }

    override fun onResume() {
        super.onResume()
        disposable = presenter.start().observeOn(AndroidSchedulers.mainThread()).subscribe(::render)
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    override fun events(): Observable<WordEvent> {
        return swipeCallback.swipes().flatMap {
            when (it.direction) {
                SwipeDirection.RIGHT -> {
                    val wordContextId = adapter.items[it.viewHolder.adapterPosition].wordContextId
                    Observable.just(WordEvent.DeleteContext(wordContextId))
                }
                else -> {
                    Observable.empty()
                }
            }
        }
    }

    override fun render(viewState: WordViewState) {
        adapter.update(viewState.contexts)
        binding.gWordEmpty.isInvisible = viewState.contexts.isNotEmpty()
        binding.gWordEmpty.requestLayout()

    }
}

