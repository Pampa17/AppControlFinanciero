package com.jpdev.appcontrolfinanciero.ui.categorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jpdev.appcontrolfinanciero.AppControlFinancieroApplication
import com.jpdev.appcontrolfinanciero.data.CategoryTotal
import com.jpdev.appcontrolfinanciero.data.FinanceRepository
import com.jpdev.appcontrolfinanciero.domain.EntryType
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoriasUiState(
    val incomeTotals: List<CategoryTotal> = emptyList(),
    val expenseTotals: List<CategoryTotal> = emptyList()
)

class CategoriasViewModel(private val repository: FinanceRepository) : ViewModel() {

    val uiState: StateFlow<CategoriasUiState> = combine(
        repository.observeCategoryTotals(EntryType.INGRESO, YearMonth.now()),
        repository.observeCategoryTotals(EntryType.EGRESO, YearMonth.now())
    ) { income, expense -> CategoriasUiState(income, expense) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoriasUiState())

    fun addCategory(name: String, type: EntryType) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addCategory(name, type) }
    }

    fun renameCategory(categoryId: Long, type: EntryType, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val category = repository.getCategories(type).firstOrNull { it.id == categoryId } ?: return@launch
            repository.renameCategory(category, newName)
        }
    }

    /** Call before showing the delete dialog, so it can warn about affected movements. */
    suspend fun countUsage(categoryId: Long, type: EntryType): Int {
        val category = repository.getCategories(type).firstOrNull { it.id == categoryId } ?: return 0
        return repository.countMovementsUsing(category)
    }

    fun deleteCategory(categoryId: Long, type: EntryType) {
        viewModelScope.launch {
            val category = repository.getCategories(type).firstOrNull { it.id == categoryId } ?: return@launch
            repository.deleteCategoryReassigning(category)
        }
    }

    companion object {
        fun factory(application: AppControlFinancieroApplication) = viewModelFactory {
            initializer { CategoriasViewModel(application.repository) }
        }
    }
}
