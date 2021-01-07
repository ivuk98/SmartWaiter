package com.example.smartwaiter.ui.guest.menu_guest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.database.UserPreferences
import com.example.smartwaiter.R
import com.example.smartwaiter.repository.Add_mealRepository
import com.example.smartwaiter.ui.restaurant.menu.MealListAdapter
import com.example.smartwaiter.ui.restaurant.menu.TagListAdapter
import com.example.smartwaiter.util.handleApiError
import com.example.smartwaiter.util.visible
import hr.foi.air.webservice.model.Tag
import hr.foi.air.webservice.util.Resource
import kotlinx.android.synthetic.main.fragment_meni.*
import kotlinx.android.synthetic.main.fragment_meni_guest.*
import kotlinx.android.synthetic.main.menu_tag_item.view.*

class MenuGuestFragment : Fragment(R.layout.fragment_meni_guest) {
    private lateinit var lokal: String
    private lateinit var stol: String

    private lateinit var viewModel: MenuGuestViewModel
    private lateinit var repository: Add_mealRepository
    private lateinit var viewModelFactory: MenuGuestModelFactory
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_meni_guest, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        repository = Add_mealRepository()
        viewModelFactory = MenuGuestModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MenuGuestViewModel::class.java)

        userPreferences = UserPreferences(requireContext())
        Log.d("restoran","1")
        userPreferences.activeRestaurant.asLiveData().observe(viewLifecycleOwner, {
            it?.let {
                lokal = it
                load()
                loadTags()
            }
        })
        //lokal = requireArguments().getInt("restaurant_id").toString()



        viewModel.myResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    progressBarMenuGuest.visible(false)
                    if (response != null) {
                        Log.d("tagovi",response.value.toString())
                        val listMeals = response.value
                        recycleViewMenuGuest.layoutManager = LinearLayoutManager(activity)
                        recycleViewMenuGuest.adapter = MealGuestListAdapter(listMeals, this)
                    }
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
        viewModel.myResponse2.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    progressBarMenuGuest.visible(false)
                    if (response != null) {
                        val listTags: MutableList<Tag> = response.value as MutableList<Tag>
                        listTags.add(0, Tag("-1", resources.getString(R.string.all_items)))
                        val layoutManager: LinearLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                        recyclerViewMenuGuestTags.layoutManager = layoutManager
                        recyclerViewMenuGuestTags.adapter = TagGuestListAdapter(listTags, this)
                        Log.d("tagovi" , response.value.toString())
                    }
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

    fun callOrderMeal(mealId: String){
        val meal = mealId

        //val action = MenuFragmentDirections.actionMeniFragmentToEditMealFragment2(meal)
        //findNavController().navigate(action)
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

                    if (response != null) {
                        val odgovor = response.value

                        recycleViewMenuGuest.layoutManager = LinearLayoutManager(activity)
                        recycleViewMenuGuest.adapter = MealGuestListAdapter(odgovor, this)
                    }
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