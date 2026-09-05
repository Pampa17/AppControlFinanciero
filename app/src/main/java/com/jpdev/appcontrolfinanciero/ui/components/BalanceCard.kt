package com.jpdev.appcontrolfinanciero.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.jpdev.appcontrolfinanciero.domain.BalanceStatus
import com.jpdev.appcontrolfinanciero.ui.theme.PoError
import com.jpdev.appcontrolfinanciero.ui.theme.Spacing

@Composable
fun BalanceCard(
    totalIncome: Long,
    totalExpenses: Long,
    balance: Long,
    status: BalanceStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(Modifier.padding(Spacing.md)) {
            Text(
                text = "SALDO DISPONIBLE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "$$balance",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "Ingresos: $$totalIncome · Egresos: $$totalExpenses",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    if (status != BalanceStatus.NORMAL) {
        BudgetStatusBanner(status, Modifier.fillMaxWidth().padding(top = Spacing.xs))
    }
}

@Composable
fun BudgetStatusBanner(status: BalanceStatus, modifier: Modifier = Modifier) {
    if (status == BalanceStatus.NORMAL) return
    val (bg, fg, label) = when (status) {
        BalanceStatus.PRECAUCION -> Triple(Color(0xFFFFE0B2), Color(0xFF8A5300), "⚠ Precaución: tu saldo se está agotando")
        BalanceStatus.CRITICO -> Triple(Color(0xFFFFCDD2), PoError, "⛔ Crítico: tu saldo casi se agota")
        BalanceStatus.NORMAL -> return
    }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = fg,
            modifier = Modifier.padding(Spacing.sm)
        )
    }
}
