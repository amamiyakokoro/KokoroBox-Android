# YAML TextMate grammars

Source: https://github.com/RedCMD/YAML-Syntax-Highlighter

Revision: `c42cf86959ba238dc8a825bdd07bed6f5e97c978`, matching the bundled
`yaml.tmLanguage.json`. See `LICENSE.md` for the MIT license.

All six scopes are registered in `../languages.json`. The root grammar includes
the YAML 1.2 and embedded grammars; their directive rules also reference YAML
1.0, 1.1, and 1.3.

The `comment` metadata entries inside `beginCaptures` in YAML 1.1, YAML 1.2,
and the embedded grammar were removed for Sora Editor's TM4E parser. Capture
maps must contain numeric keys with rule objects; a string metadata value
causes the analyzer to fail. The syntax rules are otherwise unchanged.

The editor module includes Sora's `oniguruma-native` engine. These grammars use
variable-length lookbehind patterns that the default Joni engine does not
support; falling back to Joni causes valid mappings to be misidentified.

Validate asset dependencies and capture maps with:

```sh
python3 -m unittest discover -s scripts/tests -p 'test_editor_grammars.py'
```
