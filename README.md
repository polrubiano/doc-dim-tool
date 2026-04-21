# Doc Dim Tool

> A designer-focused Android app that resolves document dimensions from partial input — given a known side, an aspect ratio, and a target DPI, it returns all the measurements you need for both screen and print work.

---

## Motivation

Every designer has been there: you know one side of a document in pixels, you have an aspect ratio requirement, and you need to figure out the physical dimensions at a specific resolution — fast, and without reaching for a spreadsheet.

Doc Dim Tool solves exactly that. Fill in what you know, and it returns a complete dimensional spec: pixels, millimeters, centimeters, and inches, across multiple DPI values at once, with optional bleed support for print.

---

## Features

- **Aspect ratio presets** — 16:9, 4:3, 1:1, 9:16 (reel/story), A4, and custom ratios
- **Flexible input** — enter your known side in px, mm, cm, or inches
- **Multi-DPI output** — get results for 72, 150, 300, 600 dpi (or any custom value) in one tap
- **Unit coverage** — every result shown in px, mm, cm, and inches simultaneously
- **Print bleed** — add bleed margin (in mm) and see adjusted dimensions alongside the base spec
- **Orientation toggle** — switch between landscape and portrait instantly
- **Contextual warnings** — alerts when selected DPI is unsuitable for the intended use (e.g. 72 dpi for print)
- **Visual proportion preview** — a scaled rectangle gives you an immediate feel for the document shape
- **Calculation history** — past specs are saved locally so you can revisit or reuse them
- **Export** — share results as plain text or image to paste into Notion, Slack, or wherever you work

---

## Tech Stack

| Layer         | Technology                              |
|---------------|-----------------------------------------|
| Language      | Kotlin                                  |
| UI            | Jetpack Compose                         |
| Architecture  | Clean Architecture (domain / data / ui) |
| DI            | Koin                                    |
| Local storage | DataStore / Room                        |
| Min SDK       | 26 (Android 8.0)                        |

---

## Project Structure

```
app/
├── ui/
│   ├── calculator/        # Input form — CalculatorScreen + ViewModel
│   └── result/            # Output cards — ResultCard composable
domain/
├── model/
│   ├── DocumentSpec.kt    # Encapsulates all user input
│   └── DocumentResult.kt  # Holds all calculated output values
└── usecase/
    └── CalculateDocumentUseCase.kt
di/
└── AppModule.kt
```

---

## How It Works

1. Enter the known side (width or height) and its unit
2. Select or define an aspect ratio
3. Choose one or more target DPI values
4. Optionally set a bleed margin and orientation
5. Instantly get a full dimensional breakdown per DPI, with a visual proportion preview

Core formula:

```
physical_size (inches) = pixels / dpi
physical_size (mm)     = (pixels / dpi) × 25.4
missing_side (px)      = known_side × (ratio_b / ratio_a)
```

---

## Roadmap

TODO - ERROR MANAGEMENT

### 🧮 F1 — Core Calculation Engine
The domain heart of the app. No UI, pure logic.

- [ ] Define `DocumentSpec` and `DocumentResult` domain models
- [ ] Implement `CalculateDocumentUseCase` (px ↔ physical units, ratio resolution)
- [ ] Support input in px, mm, cm, and inches
- [ ] Support multi-DPI output in a single calculation
- [ ] Orientation handling (landscape / portrait)
- [ ] Unit tests for all calculation paths and edge cases

---

### 🖊️ F2 — Input Form
The user-facing side of the calculator.

- [ ] Aspect ratio selector with presets (16:9, 4:3, 1:1, 9:16, A4)
- [ ] Custom ratio input (free W:H fields)
- [ ] Known side selector (width or height) with unit picker (px / mm / cm / inch)
- [ ] Multi-DPI selector (preset chips + free input field)
- [ ] Orientation toggle (landscape / portrait)
- [ ] Input validation and error states

---

### 📐 F3 — Results Display
How calculated data is presented to the user.

- [ ] Result card per DPI showing px, mm, cm, and inches
- [ ] Visual proportion preview (scaled rectangle reflecting the actual ratio)
- [ ] Contextual DPI warnings (e.g. 72 dpi flagged as unsuitable for print)
- [ ] Usage context label per DPI ("Screen", "Draft", "Print", "High-res print")

---

### 🖨️ F4 — Print Support
Extra considerations for physical output.

- [ ] Bleed margin input (mm)
- [ ] Bleed-adjusted dimensions shown alongside base dimensions in result card
- [ ] Visual bleed indicator in proportion preview

---

### 🕓 F5 — Calculation History
Persistence for past specs.

- [ ] Save each calculation automatically on result
- [ ] History list screen with summary per entry (ratio, DPI range, dimensions)
- [ ] Restore a past calculation back into the form
- [ ] Delete individual entries or clear all history

---

### 📤 F6 — Export
Getting results out of the app.

- [ ] Share result as formatted plain text
- [ ] Share result as image (card screenshot)
- [ ] Copy individual values to clipboard with a single tap

---

### 🔁 F7 — Reverse Mode
The inverse calculation flow.

- [ ] Separate input form: physical dimensions (mm/cm/inch) + DPI → pixels
- [ ] Reuse `CalculateDocumentUseCase` logic (inverse path)
- [ ] Integrate into main navigation as a second tab or mode toggle

---

### ⚙️ F8 — Customization
Power-user features for repeated workflows.

- [ ] Save custom aspect ratio presets with a user-defined name
- [ ] Save custom DPI sets as named profiles
- [ ] Manage (edit / delete) saved presets and profiles

---

### 🏠 F9 — Home Screen Widget
Quick access without opening the app.

- [ ] Minimal widget showing last calculation result
- [ ] Tap to open the app at that calculation

---

## Contributing

Contributions are welcome. If you're a designer who uses the app and has ideas, open an issue — design feedback is as valuable as code here.

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes
4. Open a pull request

Please follow the existing code style and architecture patterns.

---

## License

MIT License — see [LICENSE](LICENSE) for details.

---

*Built by a developer, for designers.*