package com.example.smartwaiter.ui.restaurant.qr

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.smartwaiter.R
import com.example.smartwaiter.repository.Add_mealRepository
import com.example.smartwaiter.repository.StolRepostiory
import com.example.smartwaiter.ui.auth.login.HomeFragmentDirections
import com.example.smartwaiter.ui.guest.GuestActivity
import com.example.smartwaiter.ui.restaurant.RestaurantActivity
import com.example.smartwaiter.ui.restaurant.editMeal.EditMealModelFactory
import com.example.smartwaiter.ui.restaurant.editMeal.EditMealViewModel
import com.example.smartwaiter.ui.restaurant.menu.MenuFragment
import com.example.smartwaiter.ui.restaurant.restaurant_list.RestaurantListDirections
import com.example.smartwaiter.ui.waiter.WaiterActivity
import com.example.smartwaiter.util.handleApiError
import com.example.smartwaiter.util.startNewActivity
import com.example.smartwaiter.util.visible
import com.google.common.primitives.UnsignedBytes.toInt
import hr.foi.air.webservice.util.Resource
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch

class QrFragment : Fragment(R.layout.fragment_qrscanner) {
    private lateinit var viewModel: QrViewModel
    private lateinit var repostiory: StolRepostiory
    private lateinit var viewModelFactory: QrModelFactory
    private lateinit var codeScanner: CodeScanner

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_qrscanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {

                var tableHash=it.text.removePrefix("https://smartwaiter.app/app.php?")
                load(tableHash)
                //val action = QrFragmentDirections.actionQrFragmentToMenuGuestFragment2(it.text.toInt())
                //findNavController().navigate(action)
                Toast.makeText(activity, tableHash, Toast.LENGTH_LONG).show()
               // val action = QrFragmentDirections.actionQrFragmentToHomeFragment()
               // findNavController().navigate(action)
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    fun load(hash : String){
        repostiory= StolRepostiory()
        viewModelFactory=QrModelFactory(repostiory)
        viewModel= ViewModelProvider(this, viewModelFactory).get(QrViewModel::class.java)
        decodeFromWeb(hash)
        viewModel.myResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    Log.d("REEEEEEEEEEEEE", response.value.toString())
                    val action = QrFragmentDirections.actionQrFragmentToMenuGuestFragment2(response.value[0].lokal_id.toInt())
                    findNavController().navigate(action)

                }
                is Resource.Loading -> { }
                is Resource.Failure -> {
                    handleApiError(response) { decodeFromWeb(hash) }

                }
            }
        }

    }

    fun decodeFromWeb(hash: String){
        viewModel.getTableByHash("Stol", "select", hash)

    }

}