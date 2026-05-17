# Notas de base de datos - perfil de usuario, biometría y cálculos nutricionales

## Objetivo
El módulo de perfil almacena información biométrica del usuario y calcula sus necesidades calóricas diarias mediante la fórmula de Harris-Benedict (TMB) y TDEE (Total Daily Energy Expenditure), que sirven como objetivos para el seguimiento nutricional.

---

## Tablas relacionadas

### `usuarios` (perfil principal)
Tabla que contiene información de cada usuario registrado.

Campos principales:
- `id`: identificador único
- `email`: correo electrónico, único
- `password_hash`: contraseña hasheada con bcrypt
- `nombre`: nombre del usuario
- `edad_anos`: edad en años
- `sexo`: `M` (masculino) o `F` (femenino)
- `altura_cm`: altura en centímetros
- `peso_actual_kg`: peso corporal actual en kilogramos
- `factor_actividad`: factor de actividad diaria (1.2 a 1.9, ver tabla abajo)
- `objetivo`: tipo de objetivo (`PERDER_PESO`, `MANTENER`, `GANAR_MUSCULO`)
- `created_at`: fecha de creación del perfil
- `updated_at`: fecha última actualización

Ejemplo:

| id | email | nombre | edad_anos | sexo | altura_cm | peso_actual_kg | factor_actividad | objetivo |
|----|-------|--------|-----------|------|-----------|----------------|-----------------|----------|
| 5 | juan@mail.com | Juan García | 28 | M | 175 | 75 | 1.55 | PERDER_PESO |
| 6 | maria@mail.com | María López | 32 | F | 165 | 60 | 1.375 | GANAR_MUSCULO |

---

### `peso_historial` (histórico de peso)
Tabla que registra los cambios de peso del usuario a lo largo del tiempo, permitiendo seguimiento de progreso.

Campos principales:
- `id`: identificador único
- `usuario_id`: usuario propietario del registro
- `peso_kg`: peso registrado en ese momento
- `fecha`: fecha del registro
- `notas`: anotaciones opcionales ("Después de vacaciones", "Inicio dieta", etc.)
- `created_at`: timestamp de creación del registro

Relación:
- `peso_historial` N:1 `usuarios`

Ejemplo:

| id | usuario_id | peso_kg | fecha | notas |
|----|-----------|---------|-------|-------|
| 1 | 5 | 82 | 2026-04-01 | Inicio del programa |
| 2 | 5 | 80.5 | 2026-04-15 | Después de 2 semanas |
| 3 | 5 | 78 | 2026-05-01 | Fin del mes |
| 4 | 5 | 75 | 2026-05-17 | Peso actual |

---

## Cálculos nutricionales

### TMB (Tasa Metabólica Basal)
Es la cantidad de calorías que el cuerpo gasta en reposo solo por mantener funciones vitales.

**Fórmula de Harris-Benedict (revisada 1984):**

Hombres:
```
TMB = 88.362 + (13.397 × peso_kg) + (4.799 × altura_cm) - (5.677 × edad_anos)
```

Mujeres:
```
TMB = 447.593 + (9.247 × peso_kg) + (3.098 × altura_cm) - (4.330 × edad_anos)
```

Ejemplo (usuario masculino):
- Peso: 75 kg
- Altura: 175 cm
- Edad: 28 años

```
TMB = 88.362 + (13.397 × 75) + (4.799 × 175) - (5.677 × 28)
    = 88.362 + 1004.775 + 839.825 - 158.956
    = 1774.01 kcal/día
```

---

### TDEE (Gasto Energético Total Diario)
Es la cantidad total de calorías que el cuerpo gasta en un día, incluyendo actividad física.

**Fórmula:**
```
TDEE = TMB × factor_actividad
```

Factores de actividad estándar:

| Nivel | Factor | Descripción |
|-------|--------|-------------|
| Sedentario | 1.200 | Poco o ningún ejercicio |
| Ligero | 1.375 | Ejercicio 1-3 días/semana |
| Moderado | 1.550 | Ejercicio 3-5 días/semana |
| Activo | 1.725 | Ejercicio 6-7 días/semana |
| Muy activo | 1.900 | Ejercicio intenso diariamente |

Continuando el ejemplo anterior con factor_actividad = 1.55:
```
TDEE = 1774.01 × 1.55 = 2749.72 kcal/día
```

---

## Cálculo en SQL

```sql
-- Calcular TMB para un usuario
SELECT
    u.id,
    u.nombre,
    u.peso_actual_kg,
    u.altura_cm,
    u.edad_anos,
    u.sexo,
    CASE
        WHEN u.sexo = 'M' THEN
            88.362 + (13.397 * u.peso_actual_kg) + (4.799 * u.altura_cm) - (5.677 * u.edad_anos)
        WHEN u.sexo = 'F' THEN
            447.593 + (9.247 * u.peso_actual_kg) + (3.098 * u.altura_cm) - (4.330 * u.edad_anos)
    END AS tmb_kcal,
    u.factor_actividad,
    CASE
        WHEN u.sexo = 'M' THEN
            (88.362 + (13.397 * u.peso_actual_kg) + (4.799 * u.altura_cm) - (5.677 * u.edad_anos)) * u.factor_actividad
        WHEN u.sexo = 'F' THEN
            (447.593 + (9.247 * u.peso_actual_kg) + (3.098 * u.altura_cm) - (4.330 * u.edad_anos)) * u.factor_actividad
    END AS tdee_kcal
FROM usuarios u
WHERE u.id = ?;
```

---

## Implementación en Java

```java
// PerfilService.java
public class PerfilService {
    
    /**
     * Calcula la Tasa Metabólica Basal (TMB) usando Harris-Benedict
     */
    public double calcularTMB(Usuario usuario) {
        double peso = usuario.getPesoActualKg();
        double altura = usuario.getAlturaCm();
        double edad = usuario.getEdadAnos();
        char sexo = usuario.getSexo();
        
        if (sexo == 'M') {
            return 88.362 + (13.397 * peso) + (4.799 * altura) - (5.677 * edad);
        } else {
            return 447.593 + (9.247 * peso) + (3.098 * altura) - (4.330 * edad);
        }
    }
    
    /**
     * Calcula el Gasto Energético Total Diario (TDEE)
     */
    public double calcularTDEE(Usuario usuario) {
        double tmb = calcularTMB(usuario);
        return tmb * usuario.getFactorActividad();
    }
    
    /**
     * Calcula déficit/superávit calórico
     * consumo_diario = calorías comidas + calorías ejercicio
     * saldo = consumo_diario - tdee
     * Si saldo < 0 : déficit (pérdida de peso)
     * Si saldo > 0 : superávit (ganancia de peso)
     */
    public double calcularSaldoNeto(long usuarioId, LocalDate fecha) {
        double consumo = resumenRepository.obtenerKcalConsumidas(usuarioId, fecha);
        double gasto = ejerciciosRepository.calcularGastoTotal(usuarioId, fecha);
        
        Usuario usuario = usuarioRepository.obtenerPorId(usuarioId);
        double tdee = calcularTDEE(usuario);
        
        // Ingesta neta = consumo - gasto por ejercicio - tdee
        // Si es negativo: déficit
        // Si es positivo: superávit
        return consumo - gasto - tdee;
    }
}
```

---

## Objetivos y recomendaciones

Según el objetivo del usuario, se sugieren rangos de calorías diarias:

| Objetivo | Recomendación | Detalle |
|----------|--------------|--------|
| PERDER_PESO | TDEE - 500 | Déficit de 500 kcal/día ≈ 250g peso/semana |
| MANTENER | TDEE ±100 | Mantener peso actual |
| GANAR_MUSCULO | TDEE + 300 | Superávit moderado para ganancia muscular |

Ejemplo (usuario con TDEE = 2749.72):
- Objetivo PERDER_PESO → Target = 2249.72 kcal/día
- Objetivo MANTENER → Target = 2749.72 kcal/día
- Objetivo GANAR_MUSCULO → Target = 3049.72 kcal/día

---

## Endpoints del módulo

```http
# Obtener perfil del usuario autenticado
GET /api/usuario/perfil

# Actualizar perfil
PATCH /api/usuario/perfil
{
  "nombre": "Juan García",
  "edad_anos": 28,
  "sexo": "M",
  "altura_cm": 175,
  "peso_actual_kg": 75,
  "factor_actividad": 1.55,
  "objetivo": "PERDER_PESO"
}

# Registrar pesaje
POST /api/usuario/peso-historial
{
  "peso_kg": 74.5,
  "fecha": "2026-05-17",
  "notas": "Después de entrenamiento intenso"
}

# Obtener histórico de pesos
GET /api/usuario/peso-historial?limite=30

# Calcular TMB/TDEE (incluido en endpoint perfil)
GET /api/usuario/calculos?usuarioId=5
→ {
  "tmb_kcal": 1774.01,
  "tdee_kcal": 2749.72,
  "objetivo_kcal": 2249.72,
  "recomendacion": "Déficit de 500 kcal para perder 250g/semana"
}
```

---

## Validación y reglas de negocio

### Validaciones de perfil

- `edad_anos` > 0 y < 150
- `altura_cm` > 100 y < 300
- `peso_actual_kg` > 20 y < 500
- `sexo` ∈ {'M', 'F'}
- `factor_actividad` ∈ [1.2, 1.9]
- `objetivo` ∈ {'PERDER_PESO', 'MANTENER', 'GANAR_MUSCULO'}

### Validaciones de peso

- `peso_kg` > 20 y < 500
- `fecha` <= hoy (no pesos futuros)
- Un pesaje por usuario por día máximo (o permitir múltiples, última es la actual)

### Cálculos

- TMB se recalcula en cada consulta (no se almacena)
- TDEE = TMB × factor_actividad (no se almacena)
- Si usuario actualiza peso, TDEE cambia automáticamente

---

## Reporte de progreso

El frontend puede construir reportes de progreso:

```json
{
  "usuario_id": 5,
  "nombre": "Juan García",
  "perfil": {
    "edad": 28,
    "altura_cm": 175,
    "peso_actual_kg": 75,
    "peso_inicial_kg": 82,
    "progreso_kg": -7,
    "objetivo": "PERDER_PESO"
  },
  "calculos": {
    "tmb_kcal": 1774.01,
    "tdee_kcal": 2749.72,
    "meta_diaria_kcal": 2249.72
  },
  "progreso_semanal": {
    "promedio_consumo": 2150,
    "promedio_ejercicio_gasto": 200,
    "promedio_neto": -350,
    "semanas_hasta_objetivo": 8
  }
}
```

---

## Estado actual

✅ Tabla `usuarios` con biometría completa
✅ Tabla `peso_historial` para seguimiento
✅ Cálculos TMB/TDEE en backend (Java y SQL)
✅ Endpoints para actualizar perfil
✅ Endpoints para registrar peso
✅ Integración con resumen diario
✅ Frontend React con pantalla de perfil
✅ Cálculo automático de déficit/superávit

---

## Limitaciones actuales

- No hay ajuste por metabolismo individual (se usa formula estándar)
- No se registra cambios en factor_actividad (valor fijo por usuario)
- TMB no se recalcula automáticamente al cambiar peso (se calcula en tiempo real)
- No hay integración con pulseras/smartwatches para gasto calórico real

---

## Próximos pasos previstos

- Exportar histórico de peso en CSV
- Gráficos de progreso de peso (línea temporal)
- Ajustes automáticos de TDEE según desviación semanal
- Integración con APIs de fitness (Fitbit, Apple Health)
- Alertas si el progreso se estanca
