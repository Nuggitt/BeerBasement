package com.example.beerbasement.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LabelingViewModel : ViewModel() {
    var labels: List<String> by mutableStateOf(emptyList())


}