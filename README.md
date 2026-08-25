# HUD

## Adding Custom Fonts

### Quick start

1. drop your `.ttf` font file into:

    ```text
    src/main/resources/assets/meowtils/fonts
    ```

2. build the project:

    ```bash
    ./gradlew clean build
    ```

the font will be included in the built project automatically. use only fonts you are legally allowed to redistribute.

<details>
<summary>Advanced font information</summary>

custom fonts are managed by the classes in:

```text
src/main/java/meowtils/font/
```

reference:
https://github.com/Godwhitelight/FontRenderer

the main font-related classes are:

- `CFont` — represents a loaded font.
- `FontManager` — stores and provides fonts.
- `FontRenderer` — draws text using a font.
- `FontUtil` — provides font-related helper methods.

<details>
<summary>Troubleshooting</summary>

### Font cannot be found

check that:

- the font is inside `src/main/resources/assets/meowtils/fonts`.
- the resource path is spelled correctly.
- the path uses `/` instead of `\`.
- the file extension is included.

</details>
