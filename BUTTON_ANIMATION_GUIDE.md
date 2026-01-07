# 🎨 Button Width Animation - Material 3 Expressive

## Animation Type: **Interactive Width Animation**

This animation is part of **Material 3 Expressive API** that creates a smooth morphing effect where buttons reshape when pressed.

---

## 📋 **Complete Code Example:**

```kotlin
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package your.package.name

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedButtonExample() {
    // Create interaction sources for each button
    val interactionSources = remember {
        List(2) { MutableInteractionSource() }
    }
    
    ButtonGroup(modifier = Modifier.fillMaxWidth()) {
        // Cancel Button with Animation
        OutlinedButton(
            modifier = Modifier
                .weight(1f)
                .animateWidth(interactionSources[0]),  // ← THE ANIMATION!
            shapes = ButtonDefaults.shapes(),
            interactionSource = interactionSources[0],
            onClick = { /* Cancel action */ },
            content = { Text("Cancel") }
        )
        
        // Add Button with Animation
        Button(
            modifier = Modifier
                .weight(1f)
                .animateWidth(interactionSources[1]),  // ← THE ANIMATION!
            shapes = ButtonDefaults.shapes(),
            interactionSource = interactionSources[1],
            onClick = { /* Add action */ },
            content = { Text("Add") }
        )
    }
}
```

---

## 🔑 **Key Components:**

### 1. **@OptIn(ExperimentalMaterial3ExpressiveApi::class)**
- This annotation enables Material 3 Expressive features
- Must be added at file level or function level

### 2. **ButtonGroup**
- Container that groups buttons together
- Enables shared animation context

### 3. **MutableInteractionSource**
- Tracks button press/release states
- Each button needs its own interaction source
- Created as a list: `List(2) { MutableInteractionSource() }`

### 4. **`.animateWidth(interactionSource)`**
- The actual animation modifier
- Automatically morphs button shape on press/release
- Smooth transition from rounded to rectangle

### 5. **`ButtonDefaults.shapes()`**
- Provides Material 3 Expressive shapes
- Handles the morphing effect

---

## 🎬 **How It Works:**

1. **At Rest (Unpressed):**
   - Button has rounded corners (pill shape)
   - Default Material 3 shape

2. **On Press:**
   - `animateWidth()` detects the press interaction
   - Button smoothly animates to a **rectangle**
   - Width slightly compresses

3. **On Release:**
   - Button smoothly animates back to **rounded corners**
   - Width returns to normal

---

## 📐 **Animation Specifications:**

| Property | Value |
|----------|-------|
| **Duration** | ~300ms (automatic) |
| **Easing** | Material Motion easing curve |
| **Trigger** | Press/Release interaction |
| **Effect** | Border radius: Rounded → 0dp → Rounded |
| **Width** | Subtle compression on press |

---

## 🛠️ **Required Dependencies:**

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    implementation("androidx.compose.material3:material3:1.3.0") // or latest
    implementation("androidx.compose.material3.adaptive:adaptive:1.0.0") // for Expressive
}
```

---

## 💡 **Usage in Your AddSubjectDialog:**

```kotlin
// Create interaction sources for 2 buttons
val interactionSources = remember {
    List(2) { MutableInteractionSource() }
}

ButtonGroup(modifier = Modifier.fillMaxWidth()) {
    // CANCEL button
    OutlinedButton(
        modifier = Modifier
            .weight(1f)
            .animateWidth(interactionSources[0]),
        shapes = ButtonDefaults.shapes(),
        interactionSource = interactionSources[0],
        onClick = {
            viewModel.resetInputFields()
            onDismiss()
        },
        content = { Text("Cancel") }
    )

    // ADD button
    Button(
        modifier = Modifier
            .weight(1f)
            .animateWidth(interactionSources[1]),
        shapes = ButtonDefaults.shapes(),
        interactionSource = interactionSources[1],
        onClick = {
            // Add subject logic
        },
        content = { Text("Add") }
    )
}
```

---

## 🎯 **Key Rules:**

1. ✅ **Always use ButtonGroup** - Required for animation to work
2. ✅ **Unique InteractionSource per button** - Don't reuse the same one
3. ✅ **Use ButtonDefaults.shapes()** - Provides the morphing shapes
4. ✅ **Apply .weight(1f)** - For equal width distribution
5. ✅ **Add @OptIn annotation** - At file or function level

---

## 🔬 **Behind the Scenes:**

The `.animateWidth()` modifier:
- Listens to `InteractionSource` events
- Detects `PressInteraction.Press` and `PressInteraction.Release`
- Uses `animateFloatAsState()` internally
- Interpolates between two shape states
- Applies `Modifier.graphicsLayer()` for smooth rendering

---

## 🌟 **Benefits:**

✅ **Rich User Feedback** - Visual confirmation of button press
✅ **Material Design 3** - Modern, Google-approved animation
✅ **Zero Configuration** - Works out of the box
✅ **Performance** - Hardware accelerated
✅ **Accessibility** - Maintains touch target size

---

## 🚀 **Pro Tips:**

### Customize Animation Duration:
Material 3 Expressive animations use predefined durations, but you can customize by creating your own modifier:

```kotlin
fun Modifier.customAnimateWidth(
    interactionSource: InteractionSource,
    durationMillis: Int = 300
): Modifier = composed {
    val isPressed = interactionSource.collectIsPressedAsState().value
    val width by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = durationMillis)
    )
    this.then(Modifier.fillMaxWidth(width))
}
```

### Multiple Buttons:
For more than 2 buttons:
```kotlin
val interactionSources = remember { List(3) { MutableInteractionSource() } }
```

---

## 📚 **Documentation:**
- [Material 3 Expressive](https://m3.material.io/)
- [Compose Interaction Source](https://developer.android.com/develop/ui/compose/touch-input/user-interactions/user-interactions-modifiers)

This animation is what makes Material 3 feel **alive and responsive**! 🎉
