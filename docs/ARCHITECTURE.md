# Architecture

## Boundaries

The calculator reducer has no Android UI dependency. Every input is represented
by a `CalculatorAction`, and `CalculatorEngine.reduce` returns both the next
immutable state and an optional completed calculation for history. This keeps
arithmetic deterministic and directly unit-testable.

`CalculatorViewModel` coordinates four independent sources:

- calculator state
- DataStore appearance preferences
- DataStore calculation history
- decoded wallpaper assets

The UI consumes one combined `CalculatorUiState`. Neither the glass renderer nor
wallpaper code performs arithmetic.

## Wallpaper coordinate contract

The full-screen background and every lens call the same
`calculateWallpaperPlacement` function. It applies center-crop, user zoom,
normalized crop translation, and optional parallax exactly once. A button then
subtracts its root position from that shared destination rectangle before
drawing. Consequently, the pixels at the lens boundary line up with the
unrefracted wallpaper around it.

The button's wallpaper copy is rendered on its own clipped layer. On Android 13+
that layer is passed into `RuntimeShader` as the `content` shader. Labels live
on a separate front-surface layer and remain sharp.

## Quality tiers

| Tier | Refraction | Touch deformation | Dispersion | Fallback blur |
|---|---:|---:|---:|---:|
| High | Full | Full | Subtle | No |
| Balanced | Reduced | Full | Minimal | No |
| Compatibility | No AGSL | Layer motion | None | Android 12+ |
| Adaptive | Device-selected | Device-selected | Device-selected | As needed |

RuntimeShader is available from API 33. Requests for an unsupported tier resolve
to Compatibility without exposing a non-working control.

## Persistence

Appearance values and history share one process-safe Preferences DataStore.
History records are URL-safe Base64 fields separated by tabs/newlines, which
preserves Unicode calculator symbols without adding a reflection-based serializer.
The newest 100 entries are kept.

Photo Picker URIs are persisted when the provider permits it. If access later
fails, startup recovers to the built-in procedural wallpaper and clears the stale
reference.

## Performance decisions

- The source wallpaper is downsampled to a maximum edge of 3072 pixels.
- The default wallpaper is generated once and cached.
- Luminance adaptation uses a small 12×20 sampling map.
- Motion sensors are registered only while enabled and resumed.
- Sensor UI updates are throttled to roughly 30 Hz.
- No idle shader clock or infinite animation is used.
- Each button owns only its small clipped render layer.

