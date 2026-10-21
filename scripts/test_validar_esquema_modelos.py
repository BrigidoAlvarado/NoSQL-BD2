import subprocess
from pathlib import Path
import unittest


class EsquemaModelosTests(unittest.TestCase):
    def test_scripts_de_modelo_declaran_invariantes_requeridas(self):
        root = Path(__file__).resolve().parents[1]
        process = subprocess.run(
            ['python3', 'scripts/validar_esquema_modelos.py'], cwd=root,
            text=True, capture_output=True, check=False,
        )
        self.assertEqual(process.returncode, 0, process.stderr)
        self.assertIn('invariantes estáticas correctas', process.stdout)

    def test_validador_real_usa_una_marca_aislada_y_limpieza(self):
        root = Path(__file__).resolve().parents[1]
        source = (root / 'scripts/validar_modelos_reales.py').read_text(encoding='utf-8')
        self.assertIn("MARCADOR = 'VALIDACION_MODELO_B01'", source)
        self.assertIn("identificador = f'{MARCADOR}:{key(fila)}'", source)
        self.assertIn("deleteOne({_id: d._id, marcaValidacion: 'VALIDACION_MODELO_B01'})", source)
        self.assertIn("MATCH (n {marcaValidacion: 'VALIDACION_MODELO_B01'}) DETACH DELETE n", source)


if __name__ == '__main__':
    unittest.main()
