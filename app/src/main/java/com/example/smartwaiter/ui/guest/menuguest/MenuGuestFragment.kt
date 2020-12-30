package com.example.smartwaiter.ui.guest.menu_guest

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartwaiter.R
import com.example.smartwaiter.repository.Add_mealRepository
import com.example.smartwaiter.ui.guest.menuguest.MealGuestListAdapter
import com.example.smartwaiter.ui.guest.menuguest.MenuGuestModelFactory
import com.example.smartwaiter.ui.guest.menuguest.MenuGuestViewModel
import com.example.smartwaiter.util.handleApiError
import com.example.smartwaiter.util.visible
import hr.foi.air.webservice.model.Meal
import hr.foi.air.webservice.model.Tag
import hr.foi.air.webservice.util.Resource
import kotlinx.android.synthetic.main.fragment_meni_guest.*

class MenuGuestFragment : Fragment(R.layout.fragment_meni_guest) {
    private lateinit var lokal: String
    private lateinit var stol: String

    private lateinit var viewModel: MenuGuestViewModel
    private lateinit var repository: Add_mealRepository
    private lateinit var viewModelFactory: MenuGuestModelFactory

    private val args: MenuGuestFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        repository = Add_mealRepository()
        viewModelFactory = MenuGuestModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MenuGuestViewModel::class.java)

        floatingActionButtonBasket.visible(false)
        progressBarMenuGuest.visible(false)

        if(args.ordered == true){
            floatingActionButtonBasket.visible(true)
        }

        floatingActionButtonBasket.setOnClickListener {
            val action = MenuGuestFragmentDirections.actionMenuGuestFragmentToMenuGuestDialogFragment()
            findNavController().navigate(action)
        }

        lokal = requireArguments().getInt("restaurant_id").toString()

        Log.d("qr", lokal)

        load()
        loadTags()
        viewModel.myResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    progressBarMenuGuest.visible(false)
                    Log.d("tagovi",response.value.toString())
                    val listMeals = response.value
                    recycleViewMenuGuest.layoutManager = LinearLayoutManager(activity)
                    recycleViewMenuGuest.adapter = MealGuestListAdapter(listMeals, this)
                }
                is Resource.Loading -> {
                    progressBarMenuGuest.visible(true)
                }
                is Resource.Failure -> {
                    handleApiError(response) { load() }
                    progressBarMenuGuest.visible(false)
                    Log.d("Response", response.toString())
                }
            }
        })
        viewModel.myResponse2.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    progressBarMenuGuest.visible(false)
                    val listTags: MutableList<Tag> = response.value as MutableList<Tag>
                    listTags.add(0, Tag("-1", resources.getString(R.string.all_items)))
                    val layoutManager: LinearLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    recyclerViewMenuGuestTags.layoutManager = layoutManager
                    recyclerViewMenuGuestTags.adapter = TagGuestListAdapter(listTags, this)
                    Log.d("tagovi" , response.value.toString())
                }
                is Resource.Loading -> {
                    progressBarMenuGuest.visible(true)
                }
                is Resource.Failure -> {
                    handleApiError(response) { load() }
                    progressBarMenuGuest.visible(true)
                    Log.d("Response", response.toString())
                }
            }
        })


    }

    fun callOrderMeal(meal: Meal){
        val action = MenuGuestFragmentDirections.actionMenuGuestFragmentToMenuDetailsFragment(meal)
        findNavController().navigate(action)
    }

    fun load(){
        viewModel.getMeal(table = "Stavka_jelovnika", method = "select", lokal)
    }
    fun loadTags(){
        viewModel.tagsByRestaurant(method= "tagoviPoRestoranu", lokal)
    }
    fun callMenuByTag(id_tag: String){
        viewModel.menuByTag(method = "meniPoTagu", id_tag=id_tag, lokal_id = lokal)
    }

    fun loadMenuByTag(id_tag: String){
        callMenuByTag(id_tag)
        viewModel.myResponse3.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {

                    val odgovor = response.value
                    recycleViewMenuGuest.layoutManager = LinearLayoutManager(activity)
                    recycleViewMenuGuest.adapter = MealGuestListAdapter(odgovor, this)
                }
                is Resource.Loading -> {

                }
                is Resource.Failure -> {

                    handleApiError(response) { callMenuByTag(id_tag) }
                    Log.d("Response", response.toString())
                }
            }
        })
    }

    fun getActivityContext(): FragmentActivity? {
        return activity
    }
}