# Liquid Calculator

A native Android calculator whose controls behave like optical material above the
wallpaper. The primary renderer uses Android RuntimeShader/AGSL to resample the
exact wallpaper pixels underneath each control, bend those samples along the
control geometry, and deform the lens around the touch point.

This is not a WebView and the primary visual treatment is not a blur-and-border
glassmorphism effect.

## Current feature set

- Decimal-safe calculator engine using `BigDecimal` and 34-digit precision
- Addition, subtraction, multiplication, division, signs and contextual percent
- Chained operations and repeated equals
- Recoverable division-by-zero and invalid scientific-result handling
- Portrait calculator and automatic landscape scientific calculator
- AGSL lens magnification, edge refraction, restrained chromatic dispersion and
  touch-position-aware pressure deformation on Android 13+
- Layered compatibility renderer on Android 8–12
- Spring-driven physical press response and differentiated system haptics
- Custom wallpaper selection through Android's Photo Picker
- Pinch/drag crop positioning with a live glass-calculator preview
- Persistent crop, appearance settings and calculation history
- Tap-to-recall history and clear history
- Per-wallpaper and per-control luminance adaptation
- Optional lifecycle-aware motion parallax, disabled by default
- Clear, graphite, silver, amber and deep-blue material tints
- Adaptive, high, balanced and compatibility quality modes
- TalkBack semantics, large touch targets and system animation/haptic behavior

## Rendering pipeline

Each glass control is composed from independent layers:

1. A precisely aligned crop of the same wallpaper drawn behind the calculator.
2. A geometry-aware RuntimeShader lens that magnifies the center and bends pixels
   near the circular or pill-shaped rim.
3. Touch-local pressure displacement fed from the exact pointer coordinate.
4. A subtle channel-separated rim sample for chromatic dispersion.
5. Adaptive material tint, internal highlight, rim light and contact shadow.
6. Undistorted text drawn on the front surface.

Only the active control receives changing shader uniforms; the wallpaper and
inactive controls do not run continuous animations.

## Requirements

- Android Studio compatible with AGP 8.9.2
- JDK 17
- Android SDK 35
- Minimum device API 26 (Android 8.0)

## Build

```bash
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is generated at:

`app/build/outputs/apk/debug/app-debug.apk`

## Project map

- `calculator/` — pure state reducer, precision arithmetic and ViewModel
- `glass/` — reusable lens controls, palette adaptation and quality tiers
- `wallpaper/` — decoding, crop geometry, luminance map and live cropper
- `history/` — bounded persistent calculation history
- `data/` — DataStore-backed appearance preferences
- `ui/` — responsive calculator, history and settings surfaces
- `res/raw/liquid_lens.agsl` — optical lens shader

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the state and rendering
boundaries.

