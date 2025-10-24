# 📘 Proyecto Jetpack Compose – Ejemplo de Tabs y LazyColumn  
**Autor:** André David Delgado Allpan  

---

## 🧩 Descripción del proyecto  
En este proyecto implementé una interfaz sencilla en **Android Jetpack Compose** para observar cómo se comporta una lista dinámica (`LazyColumn`) dentro de diferentes estructuras de pestañas (**Tabs**).  

Mi objetivo fue comprobar qué sucede cuando la lista se encuentra dentro de un tab que **no está visible** y cómo eso afecta el rendimiento de la aplicación.  

Para lograrlo, desarrollé **dos versiones** de tabs:  
1. 🟢 **Condicional (A_CONDITIONAL)** → donde solo se compone el tab visible.  
2. 🟣 **Overlay (B_OVERLAY)** → donde ambos tabs se componen desde el inicio, aunque uno esté oculto con transparencia.  

En la consola (`Logcat`) puedo ver cuándo cada ítem de la lista se **compone** o se **elimina**, lo que me permite observar el proceso de composición y descarte en Compose.

---

## ⚙️ Estructura principal del código

### 🧠 `TabsConditional()`  
En esta versión controlo los tabs con una estructura `when`.  
Solo el tab activo se compone, y cuando el usuario cambia de tab, Compose **elimina** el anterior y **crea** el nuevo.  

De esta manera, la lista del Tab 2 **no existe en memoria** hasta que el usuario realmente la abre.

```kotlin
when (selectedTab) {
    0 -> Tab1Content()
    1 -> Tab2List()
}
```

---

### 🧠 `TabsOverlay()`  
En esta versión ambos tabs están **montados desde el inicio** en el árbol de composición, pero uno está oculto visualmente usando `alpha(0f)`.  

Esto significa que la lista del Tab 2 **ya está creada** aunque todavía no la vea.  
Pude comprobarlo fácilmente en el Logcat, ya que aparecen los registros de los ítems del Tab 2 apenas ejecuto la app.

```kotlin
Box(Modifier.fillMaxSize()) {
    Tab1Content(Modifier.alpha(if (selected == 0) 1f else 0f))
    Tab2List(Modifier.alpha(if (selected == 1) 1f else 0f))
}
```

---

### 🎨 `Tab2List()`  
La lista muestra 100 elementos de ejemplo (Fruta #0, Fruta #1, etc.).  
Utilicé un `LazyColumn` porque solo compone los elementos visibles y algunos más en un pequeño buffer para hacer el scroll fluido.

Cada ítem tiene un `DisposableEffect` que imprime en consola cuándo el elemento entra o sale del árbol de composición, por ejemplo:

```
⏩ compose Fruta #5
⏹ dispose Fruta #0
```

De esta forma pude ver claramente cómo los ítems se crean y destruyen mientras hago scroll.

---

## 🧱 Componentes que utilicé

### 🔹 `LazyColumn`
Usé este componente para mostrar listas largas de forma eficiente.  
Solo mantiene en memoria los elementos que se ven en pantalla, lo cual mejora el rendimiento y evita usar demasiados recursos.

### 🔹 `DisposableEffect`
Me sirvió para registrar en consola cuándo Compose **crea** y **elimina** cada ítem.  
Con esto pude comprobar el comportamiento real del renderizado perezoso (*lazy rendering*).

---

## 🔄 Lo que observé en las pruebas

| Modo | ¿El Tab 2 se carga al inicio? | ¿LazyColumn se crea? | ¿Cuándo aparecen los logs? |
|------|-------------------------------|-----------------------|-----------------------------|
| `A_CONDITIONAL` | ❌ No | Solo cuando abro el Tab 2 | Al cambiar de pestaña |
| `B_OVERLAY` | ✅ Sí | Desde el inicio | Apenas inicio la app |

---

## 🧠 Mi interpretación técnica

Cuando probé el modo **A_CONDITIONAL**, vi que Compose solo crea el contenido del tab seleccionado.  
El tab inactivo no existe en el árbol de composición, así que la lista del Tab 2 **no ocupa memoria ni ejecuta código** hasta que lo abro.  

En cambio, con el modo **B_OVERLAY**, el tab oculto **sí está en memoria** aunque no se muestre.  
Por eso la lista del Tab 2 se crea desde el inicio y aparece en los logs, lo que demuestra que el código sigue activo en segundo plano.  

Esto explica por qué algunas apps pueden **volverse lentas o calentar el dispositivo**, incluso si el usuario no está viendo la lista: la lista ya se encuentra compuesta y viva en memoria.

---

## 🧪 Mis pruebas  
1. Al ejecutar con `MODE = DemoMode.A_CONDITIONAL`:
   - Inicié en **Tab 1** y no se mostró nada en Logcat.  
   - Al cambiar a **Tab 2**, aparecieron los logs “compose Fruta…” indicando que recién se creó la lista.  

2. Con `MODE = DemoMode.B_OVERLAY`:
   - Al iniciar la app en **Tab 1**, ya se mostraban los logs del Tab 2.  
   - Esto confirmó que su lista estaba activa desde el inicio.

---

## 💬 Conclusión personal  
Pude comprobar que en Jetpack Compose un `LazyColumn` **solo renderiza los elementos visibles**, pero **puede existir en memoria** aunque no esté en pantalla si la pestaña que lo contiene está montada.

Por eso, cuando trabajo con pestañas o con un `HorizontalPager`, siempre trato de:
- Componer el contenido **solo cuando está visible**.  
- Evitar mantener listas grandes activas si no se muestran.  
- Usar condiciones (`if (visible) ...`) o `when` para controlar cuándo se crea cada parte de la interfaz.
