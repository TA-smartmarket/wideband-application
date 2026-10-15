package com.smartmarket.kiosk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.smartmarket.kiosk.R
import com.smartmarket.kiosk.ShopNavigator
import com.smartmarket.kiosk.appContainer
import com.smartmarket.kiosk.databinding.FragmentIdleBinding
import com.smartmarket.kiosk.data.uwb.statusLabel
import com.smartmarket.kiosk.data.uwb.sourceLabel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Welcome screen shown whenever no shopping session is active. No login and no
 * authentication — "Mulai Belanja" starts a local session. A small status line
 * reflects UWB connectivity without inventing a position.
 */
class IdleFragment : Fragment() {

    private var _binding: FragmentIdleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentIdleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStartShopping.setOnClickListener {
            (requireActivity() as ShopNavigator).showBeranda()
        }

        val uwb = appContainer.uwbRepository
        uwb.start()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                uwb.state.collectLatest { s ->
                    binding.tvIdleStatus.text = buildString {
                        append(s.sourceLabel())
                        append(" · ")
                        append(s.statusLabel())
                    }
                    binding.idleDot.background.setTint(
                        ContextCompat.getColor(requireContext(), dotColor(s)),
                    )
                }
            }
        }
    }

    private fun dotColor(s: com.smartmarket.kiosk.data.uwb.UwbState): Int = when {
        s.connected && s.tagOnline && !s.stale -> R.color.sm_green
        s.connected -> R.color.sm_warning
        else -> R.color.sm_muted
    }

    override fun onStop() {
        super.onStop()
        appContainer.uwbRepository.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}