import json
from pathlib import Path
import unittest


ASSETS = Path(__file__).resolve().parents[2] / "feature/editor/assets"


def objects(node):
    if isinstance(node, dict):
        yield node
        for child in node.values():
            yield from objects(child)
    elif isinstance(node, list):
        for child in node:
            yield from objects(child)


class EditorGrammarTests(unittest.TestCase):
    def test_registered_grammars_have_resolvable_dependencies(self):
        definitions = json.loads((ASSETS / "textmate/languages.json").read_text())["languages"]
        grammars = {}
        for definition in definitions:
            with self.subTest(grammar=definition["grammar"]):
                grammar = json.loads((ASSETS / definition["grammar"]).read_text())
                self.assertEqual(grammar["scopeName"], definition["scopeName"])
                self.assertNotIn(grammar["scopeName"], grammars)
                grammars[grammar["scopeName"]] = grammar
                if configuration := definition.get("languageConfiguration"):
                    self.assertTrue((ASSETS / configuration).is_file())

        for scope, grammar in grammars.items():
            for node in objects(grammar):
                include = node.get("include")
                if not isinstance(include, str):
                    continue
                with self.subTest(scope=scope, include=include):
                    if include in ("$self", "$base"):
                        continue
                    target, separator, rule = include.partition("#")
                    target = target or scope
                    self.assertIn(target, grammars, f"Unregistered grammar: {target}")
                    if separator:
                        self.assertIn(rule, grammars[target].get("repository", {}))

    def test_capture_maps_are_compatible_with_tm4e(self):
        for path in (ASSETS / "textmate").rglob("*.tmLanguage.json"):
            for node in objects(json.loads(path.read_text())):
                for key in ("captures", "beginCaptures", "endCaptures", "whileCaptures"):
                    if key not in node:
                        continue
                    with self.subTest(grammar=path.name, capture=key):
                        self.assertIsInstance(node[key], dict)
                        for index, capture in node[key].items():
                            self.assertTrue(index.isdecimal(), f"Non-numeric capture: {index}")
                            self.assertIsInstance(capture, dict)


if __name__ == "__main__":
    unittest.main()
