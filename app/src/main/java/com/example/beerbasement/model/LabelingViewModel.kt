package com.example.beerbasement.model

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel

class LabelingViewModel : ViewModel() {
    private val _labels = mutableStateOf<List<String>>(emptyList())
    val labels: State<List<String>> get() = _labels

    fun updateLabels(newLabels: List<String>) {
        _labels.value = newLabels
    }
}
