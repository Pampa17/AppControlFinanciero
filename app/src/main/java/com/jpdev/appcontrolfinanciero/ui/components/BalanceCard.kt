package com.jpdev.appcontrolfinanciero.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(Spacing.md)) {
            Text(
                text = "SALDO DISPONIBLE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "$${balance.formatMoney()}",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "Ingresos: $${totalIncome.formatMoney()} · Egresos: $${totalExpenses.formatMoney()}",
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
    data class BannerStyle(val bg: Color, val fg: Color, val icon: ImageVector, val label: String)
    val style = when (status) {
        BalanceStatus.PRECAUCION -> BannerStyle(
            Color(0xFFFFE0B2), Color(0xFF8A5300), Icons.Filled.Warning, "Precaución: tu saldo se está agotando"
        )
        BalanceStatus.CRITICO -> BannerStyle(
            Color(0xFFFFCDD2), PoError, Icons.Filled.Error, "Crítico: tu saldo casi se agota"
        )
        BalanceStatus.NORMAL -> return
    }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = style.bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(style.icon, contentDescription = null, tint = style.fg, modifier = Modifier.size(20.dp))
            Text(
                text = style.label,
                style = MaterialTheme.typography.bodyMedium,
                color = style.fg,
                modifier = Modifier.padding(start = Spacing.xs)
            )
        }
    }
}
