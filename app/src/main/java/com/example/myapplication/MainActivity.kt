package com.example.myapplication


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

private enum class DemoMode { A_CONDITIONAL, B_OVERLAY }
private val MODE = DemoMode.A_CONDITIONAL   // <- cambia aquí para probar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TwoTabsApp() }
    }
}

@Composable
fun TwoTabsApp() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            when (MODE) {
                DemoMode.A_CONDITIONAL -> TabsConditional()
                DemoMode.B_OVERLAY     -> TabsOverlay()
            }
        }
    }
}

/* ======================= VARIANTE A: CONDICIONAL ============================
   Solo existe el tab visible. La lista del Tab 2 NO se crea hasta abrirlo. */
@Composable
fun TabsConditional() {
    var selected by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selected) {
            Tab(selected == 0, onClick = { selected = 0 }, text = { Text("Tab 1") })
            Tab(selected == 1, onClick = { selected = 1 }, text = { Text("Tab 2") })
        }
        when (selected) {
            0 -> Tab1Content()
            1 -> Tab2List()  // solo se compone aquí cuando lo seleccionas
        }
    }
}

/* ======================= VARIANTE B: OVERLAY/ALPHA ==========================
   Ambos tabs se montan desde el inicio. La lista del Tab 2 SÍ existe aunque
   no se vea (alpha=0f). */
@Composable
fun TabsOverlay() {
    var selected by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selected) {
            Tab(selected == 0, onClick = { selected = 0 }, text = { Text("Tab 1") })
            Tab(selected == 1, onClick = { selected = 1 }, text = { Text("Tab 2") })
        }
        Box(Modifier.fillMaxSize()) {
            Tab1Content(Modifier.alpha(if (selected == 0) 1f else 0f))
            // OJO: aunque alpha=0f, esta LazyColumn ya está compuesta y viva
            Tab2List(Modifier.alpha(if (selected == 1) 1f else 0f))
        }
    }
}

/* ============================== CONTENIDOS ================================== */

@Composable
fun Tab1Content(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Contenido Tab 1")
    }
}

@Composable
fun Tab2List(modifier: Modifier = Modifier) {
    val datos = remember { List(100) { "Fruta #$it" } }
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
        items(datos, key = { it }) { item ->
            // Logs para ver cuándo se compone y se libera cada fila
            DisposableEffect(item) {
                println("⏩ compose $item")
                onDispose { println("⏹ dispose $item") }
            }
            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(item, Modifier.padding(16.dp))
            }
        }
    }
}
