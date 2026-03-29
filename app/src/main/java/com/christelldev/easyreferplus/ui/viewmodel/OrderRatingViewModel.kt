package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.*
import com.christelldev.easyreferplus.data.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrderRatingState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
    // Config
    val tipAdjustmentMinutes: Int = 60,
    val tipSuggestedPercentages: List<Int> = listOf(10, 15, 20),
    val driverTagOptions: List<String> = emptyList(),
    val companyTagOptions: List<String> = emptyList(),
    // Order info
    val orderId: Int = 0,
    val deliveryFee: Double = 0.0,
    val driverName: String? = null,
    val driverSelfieUrl: String? = null,
    val companyName: String? = null,
    // User input
    val driverRating: Float = 0f,
    val companyRating: Float = 0f,
    val selectedDriverTags: Set<String> = emptySet(),
    val selectedCompanyTags: Set<String> = emptySet(),
    val comment: String = "",
    val tipAmount: Double = 0.0,
    // Existing
    val existingRating: RatingDetail? = null,
    val existingTip: TipDetail? = null,
    val tipMinutesRemaining: Int? = null,
)

class OrderRatingViewModel(
    private val apiService: ApiService,
    private val getToken: () -> String,
) : ViewModel() {

    private val _state = MutableStateFlow(OrderRatingState())
    val state: StateFlow<OrderRatingState> = _state.asStateFlow()

    fun loadData(orderId: Int, deliveryFee: Double, driverName: String?, driverSelfieUrl: String?, companyName: String?) {
        _state.value = _state.value.copy(
            orderId = orderId,
            deliveryFee = deliveryFee,
            driverName = driverName,
            driverSelfieUrl = driverSelfieUrl,
            companyName = companyName,
        )
        viewModelScope.launch {
            try {
                val token = "Bearer ${getToken()}"
                // Load config, existing rating, and existing tip in parallel
                val configResp = apiService.getRatingConfig(token)
                val ratingResp = apiService.getMyRating(token, orderId)
                val tipResp = apiService.getTip(token, orderId)

                if (configResp.isSuccessful) {
                    val cfg = configResp.body()!!
                    _state.value = _state.value.copy(
                        tipAdjustmentMinutes = cfg.tipAdjustmentMinutes,
                        tipSuggestedPercentages = cfg.tipSuggestedPercentages,
                        driverTagOptions = cfg.driverTags,
                        companyTagOptions = cfg.companyTags,
                    )
                }

                if (ratingResp.isSuccessful) {
                    val r = ratingResp.body()?.rating
                    if (r != null) {
                        _state.value = _state.value.copy(
                            existingRating = r,
                            driverRating = r.driverRating ?: 0f,
                            companyRating = r.companyRating ?: 0f,
                            selectedDriverTags = r.driverTags.toSet(),
                            selectedCompanyTags = r.companyTags.toSet(),
                            comment = r.comment ?: "",
                        )
                    }
                }

                if (tipResp.isSuccessful) {
                    val t = tipResp.body()?.tip
                    if (t != null) {
                        _state.value = _state.value.copy(
                            existingTip = t,
                            tipAmount = t.amount,
                            tipMinutesRemaining = t.minutesRemaining,
                        )
                    }
                }

                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Error: ${e.message}")
            }
        }
    }

    fun setDriverRating(rating: Float) { _state.value = _state.value.copy(driverRating = rating) }
    fun setCompanyRating(rating: Float) { _state.value = _state.value.copy(companyRating = rating) }
    fun setComment(text: String) { _state.value = _state.value.copy(comment = text) }
    fun setTipAmount(amount: Double) { _state.value = _state.value.copy(tipAmount = amount) }

    fun toggleDriverTag(tag: String) {
        val current = _state.value.selectedDriverTags.toMutableSet()
        if (tag in current) current.remove(tag) else current.add(tag)
        _state.value = _state.value.copy(selectedDriverTags = current)
    }

    fun toggleCompanyTag(tag: String) {
        val current = _state.value.selectedCompanyTags.toMutableSet()
        if (tag in current) current.remove(tag) else current.add(tag)
        _state.value = _state.value.copy(selectedCompanyTags = current)
    }

    fun submit() {
        val s = _state.value
        if (s.driverRating == 0f && s.companyRating == 0f) return

        _state.value = s.copy(isSaving = true, error = null)
        viewModelScope.launch {
            try {
                val token = "Bearer ${getToken()}"

                // Submit rating
                val ratingResp = apiService.createRating(token, s.orderId, RatingRequest(
                    driverRating = if (s.driverRating > 0) s.driverRating else null,
                    companyRating = if (s.companyRating > 0) s.companyRating else null,
                    driverTags = s.selectedDriverTags.toList(),
                    companyTags = s.selectedCompanyTags.toList(),
                    comment = s.comment.ifBlank { null },
                ))

                // Submit tip if > 0
                if (s.tipAmount > 0) {
                    apiService.createOrUpdateTip(token, s.orderId, TipRequest(s.tipAmount))
                }

                if (ratingResp.isSuccessful) {
                    _state.value = _state.value.copy(isSaving = false, saved = true)
                } else {
                    _state.value = _state.value.copy(isSaving = false, error = "Error al guardar")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = "Error: ${e.message}")
            }
        }
    }

    class Factory(
        private val apiService: ApiService,
        private val getToken: () -> String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OrderRatingViewModel(apiService, getToken) as T
    }
}
