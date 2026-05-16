# Documentación de Testing

En este documento detallo la estrategia de pruebas que he llevado a cabo para la aplicación FemVerd. Para asegurar que la app es robusta y cumple con los requisitos del proyecto, he dividido las pruebas en dos grandes bloques: la lógica de negocio (Unit Tests) y la interfaz de usuario (UI Tests).

## 1. Pruebas Unitarias (Lógica de Negocio)
Aquí he querido comprobar que los cálculos matemáticos internos de la aplicación funcionan a la perfección, sin necesidad de cargar toda la interfaz gráfica. Son tests que se ejecutan al instante.

* **Archivo:** `app/src/test/java/com/example/femverd/BusinessLogicTest.kt`
* **Comprobación::** Verifica que el algoritmo que calcula el nivel del usuario funciona bien. Comprueba que, dándole una cantidad de Eco-Puntos concreta (ej. 750 puntos), la fórmula asigna el nivel correcto al usuario teniendo en cuenta que se sube de nivel cada 500 puntos.
* **Ejecución:** Basta con abrir el archivo en Android Studio y darle al botón verde de "Play" (Run) que aparece a la izquierda de la clase.

## 2. Pruebas de Interfaz (UI Testing)
Para la parte visual, he programado dos pruebas usando las herramientas de Jetpack Compose. El objetivo aquí es comprobar que el "árbol de nodos" se genera correctamente, es decir, que los textos y botones realmente aparecen en la pantalla cuando deben.

* **Test A: La pantalla de Login (`LoginUiTest.kt`)**
    * **Ubicación:** `app/src/androidTest/java/com/example/femverd/LoginUiTest.kt`
    * **Comprobación::** Aísla la pantalla de inicio de sesión y verifica que los elementos críticos (el título de la app "FemVerd" y el botón principal de "LOG IN") se renderizan y están visibles para el usuario.

* **Test B: Las tarjetas de Ayuda (`FaqCardUiTest.kt`)**
    * **Ubicación:** `app/src/androidTest/java/com/example/femverd/FaqCardUiTest.kt`
    * **Comprobación::** Comprueba el comportamiento de un componente más pequeño y aislado: la tarjeta de Preguntas Frecuentes. Le inyecta una pregunta de prueba y se asegura de que el texto se pinta en pantalla.

* Para ambos la ejecución se realiza abriendo el emulador desde el Device Manager (no la app) y dandole al botón verde de "Play" (Run) exactamente igual que con las pruebas unitarias.