# 5.5 Perfil y cálculo de TMB/TDEE

## 5.5.1 Problema que resuelve el módulo

El resumen diario muestra cuántas kilocalorías y macronutrientes ha consumido el usuario en un día, pero ese dato por sí solo no permite juzgar si el consumo es adecuado. Para eso es necesario un valor de referencia: el gasto energético estimado del usuario en función de sus características físicas y su nivel de actividad habitual.

El módulo de perfil cubre esa necesidad. Almacena los datos biométricos del usuario, calcula a partir de ellos la Tasa Metabólica Basal (TMB) y el Gasto Energético Total Diario (TDEE), y pone ese valor a disposición del resto del sistema. La pantalla del diario lo usa para mostrar el consumo como «kilocalorías consumidas / TDEE», dando al usuario un marco concreto para interpretar su ingesta.

---

## 5.5.2 Datos biométricos del perfil

Los datos del perfil se almacenan directamente en la tabla `usuarios`, sin una tabla separada. Como se explica en §4.2.4, esta decisión evita una join innecesaria y refleja que los datos biométricos son obligatorios desde el registro.

El modelo de dominio `Perfil` agrupa los siguientes campos:

| Campo             | Tipo Java          | Tipo SQL              | Restricción          |
|-------------------|--------------------|-----------------------|----------------------|
| `sexo`            | `Sexo` (enum H/M)  | `ENUM('H','M')`       | NOT NULL             |
| `fechaNacimiento` | `LocalDate`        | `DATE`                | NOT NULL             |
| `alturaCm`        | `int`              | `SMALLINT`            | NOT NULL             |
| `pesoKgActual`    | `double`           | `DECIMAL(5,2)`        | NOT NULL             |
| `pesoObjetivo`    | `Double`           | `DECIMAL(5,2)`        | NULL (opcional)      |
| `nivelActividad`  | `NivelActividad`   | `ENUM(...)`           | NOT NULL             |

El campo `pesoObjetivo` es el único nullable del conjunto: un usuario puede no haber definido un peso objetivo. Los cinco niveles de actividad están modelados como un enum Java con su factor multiplicador incorporado:

```java
// NivelActividad.java
public enum NivelActividad {
    SEDENTARIO(1.2),
    LIGERO(1.375),
    MODERADO(1.55),
    ALTO(1.725),
    MUY_ALTO(1.9);

    private final double factor;

    NivelActividad(double factor) { this.factor = factor; }
    public double getFactor() { return factor; }
}
```

El factor está encapsulado en el propio valor del enum, de forma que la fórmula del TDEE no necesita una estructura auxiliar de correspondencia.

---

## 5.5.3 API del módulo de perfil

El módulo expone dos endpoints en `PerfilController`:

| Método | Ruta              | Descripción                                      |
|--------|-------------------|--------------------------------------------------|
| GET    | `/api/perfil/{id}`| Devuelve el perfil completo con TMB y TDEE       |
| PUT    | `/api/perfil/{id}`| Actualiza los datos biométricos y recalcula TMB/TDEE |

Ambos devuelven la misma estructura `PerfilResponse`, que incluye los datos biométricos del usuario más los dos valores calculados:

```java
// PerfilResponse.java (campos relevantes)
private double tmb;
private double tdee;
```

El cliente que llama a `GET /api/perfil/{id}` recibe directamente los valores calculados; no necesita conocer la fórmula ni hacer ninguna operación aritmética.

La actualización se valida con `@Valid` en el controlador antes de que el cuerpo llegue al servicio:

```java
// PerfilController.java
@PutMapping("/{id}")
public PerfilResponse updatePerfil(
        @PathVariable Long id,
        @Valid @RequestBody PerfilUpdateRequest request) {
    return perfilService.updatePerfil(id, request);
}
```

---

## 5.5.4 Cálculo de TMB y TDEE

El cálculo se realiza en `PerfilServiceImpl` siguiendo la fórmula de Mifflin-St Jeor. La implementación es directa:

```java
// PerfilServiceImpl.java
private double calcularTmb(Perfil perfil) {
    int edad = Period.between(perfil.getFechaNacimiento(), LocalDate.now()).getYears();
    double base = 10 * perfil.getPesoKgActual()
            + 6.25 * perfil.getAlturaCm()
            - 5 * edad;
    return perfil.getSexo() == Sexo.H ? base + 5 : base - 161;
}

private double calcularTdee(Perfil perfil, double tmb) {
    return tmb * perfil.getNivelActividad().getFactor();
}
```

El término `base` es común a ambos sexos:

```
base = 10 × peso_kg + 6,25 × altura_cm − 5 × edad
```

La constante diferenciadora por sexo se aplica sobre esa base:

- Hombre (H): `TMB = base + 5`
- Mujer (M): `TMB = base − 161`

El TDEE se obtiene multiplicando la TMB por el factor del nivel de actividad del usuario. Los valores intermedios y el resultado final se redondean a dos decimales antes de incluirlos en la respuesta:

```java
// PerfilServiceImpl.java
private PerfilResponse toResponse(Perfil perfil) {
    double tmb = Math.round(calcularTmb(perfil) * 100.0) / 100.0;
    double tdee = Math.round(calcularTdee(perfil, tmb) * 100.0) / 100.0;
    return new PerfilResponse(..., tmb, tdee);
}
```

### Ejemplo verificado por los tests

Para un hombre de 30 años, 70 kg, 170 cm y nivel SEDENTARIO (factor 1,2):

```
base = 10 × 70 + 6,25 × 170 − 5 × 30 = 700 + 1062,5 − 150 = 1612,5
TMB  = 1612,5 + 5 = 1617,5
TDEE = 1617,5 × 1,2 = 1941,0
```

Para una mujer de 30 años, 60 kg, 160 cm y nivel MODERADO (factor 1,55):

```
base = 10 × 60 + 6,25 × 160 − 5 × 30 = 600 + 1000 − 150 = 1450,0
TMB  = 1450,0 − 161 = 1289,0
TDEE = 1289,0 × 1,55 = 1997,95
```

Estos dos casos están cubiertos exactamente por los tests unitarios de `PerfilServiceImplTest`.

---

## 5.5.5 Por qué el cálculo vive en el backend

Calcular TMB y TDEE en el servicio de backend en lugar de en el cliente JavaFX tiene dos consecuencias prácticas.

La primera es la testabilidad. La fórmula reside en un único lugar y puede verificarse con tests unitarios independientes del cliente. Si la fórmula tiene un error, el test lo detecta sin necesidad de arrancar la interfaz gráfica. Si en el futuro el cliente cambia de tecnología, la lógica no se duplica ni se pierde.

La segunda es la consistencia. El cliente recibe los valores ya calculados y listos para mostrar. No existe el riesgo de que distintas partes del cliente implementen la misma fórmula con resultados ligeramente distintos.

---

## 5.5.6 Validaciones del perfil

### Validación en el backend

Las restricciones se declaran en `PerfilUpdateRequest` con anotaciones de Bean Validation:

```java
@NotNull(message = "El sexo es obligatorio")
private Sexo sexo;

@NotNull(message = "La fecha de nacimiento es obligatoria")
@Past(message = "La fecha de nacimiento debe ser anterior a hoy")
private LocalDate fechaNacimiento;

@Min(value = 100, message = "La altura mínima es 100 cm")
@Max(value = 250, message = "La altura máxima es 250 cm")
private int alturaCm;

@DecimalMin(value = "20.0", message = "El peso actual debe ser al menos 20 kg")
private double pesoKgActual;

@NotNull(message = "El nivel de actividad es obligatorio")
private NivelActividad nivelActividad;
```

Spring rechaza cualquier petición que incumpla estas restricciones antes de que llegue al servicio, devolviendo HTTP 400 con el mensaje de la anotación a través del `GlobalExceptionHandler`.

`pesoObjetivo` no tiene restricción porque es un campo opcional: puede llegar nulo o no llegar en el cuerpo de la petición.

### Validación en el cliente

El controlador JavaFX `PerfilController` realiza una validación local antes de enviar la petición. El método `validar()` comprueba que los campos obligatorios no estén vacíos, que la altura esté entre 100 y 250 cm, que el peso sea mayor que 20 kg y que el peso objetivo, si se ha introducido, sea un número válido. Si alguna comprobación falla, se muestra el mensaje de error en la pantalla y no se realiza ninguna llamada a la API.

Esta validación es redundante con la del backend pero mejora la experiencia del usuario: el error se detecta antes de hacer la petición de red.

---

## 5.5.7 Integración con el cliente JavaFX

### Pantalla de perfil

La clase `PerfilController` (cliente) gestiona la pantalla de perfil. Al inicializarse, lanza una tarea en segundo plano para cargar los datos del usuario identificado en `SessionManager`:

```java
// PerfilController.java (cliente)
private void cargarPerfil() {
    Task<PerfilDto> task = new Task<>() {
        @Override
        protected PerfilDto call() throws Exception {
            return perfilApiClient.getPerfil(SessionManager.getUsuarioId());
        }
    };
    task.setOnSucceeded(event -> poblarFormulario(task.getValue()));
    ejecutar(task);
}
```

El método `poblarFormulario` vuelca los datos recibidos en los controles del formulario y asigna los valores de TMB y TDEE directamente a dos etiquetas:

```java
tmbLabel.setText(String.valueOf(perfil.getTmb()));
tdeeLabel.setText(String.valueOf(perfil.getTdee()));
```

Cuando el usuario pulsa guardar, el controlador construye el cuerpo de la petición PUT, la envía y, si tiene éxito, actualiza el formulario con el perfil recalculado. El TDEE actualizado se persiste en `SessionManager` para que esté disponible en otras pantallas:

```java
task.setOnSucceeded(event -> {
    PerfilDto actualizado = task.getValue();
    poblarFormulario(actualizado);
    SessionManager.setTdee(actualizado.getTdee());
});
```

### Integración con el diario

La pantalla del diario (`DiarioController`) usa el TDEE almacenado en `SessionManager` para contextualizar el consumo calórico del día:

```java
// DiarioController.java
double tdee = SessionManager.getTdee();
if (tdee > 0) {
    kcalLabel.setText(resumen.getKcalTotales() + " / " + tdee + " kcal");
} else {
    kcalLabel.setText(String.valueOf(resumen.getKcalTotales()));
}
```

Si el TDEE no está disponible en `SessionManager` —por ejemplo, si no ha podido cargarse correctamente durante la sesión actual— el diario muestra solo el total consumido, sin denominador. Si está disponible, lo muestra en el formato «consumido / objetivo», permitiendo al usuario valorar su ingesta del día sin necesidad de hacer ningún cálculo manual.

---

## 5.5.8 Tests del servicio de perfil

`PerfilServiceImplTest` contiene cinco tests organizados en dos clases anidadas. El repositorio se sustituye por un mock de Mockito, de forma que los tests no requieren base de datos ni contexto de Spring.

**`GetPerfil` — 3 tests**

- `hombre_calculaTmbYTdeeCorrectamente`: verifica que TMB es 1617,5 y TDEE es 1941,0 para el caso de hombre descrito en §5.5.4.
- `mujer_calculaTmbYTdeeCorrectamente`: verifica que TMB es 1289,0 y TDEE es 1997,95 para el caso de mujer.
- `idInexistente_lanzaResourceNotFoundException`: comprueba que se lanza la excepción correcta cuando el repositorio no encuentra el id.

**`UpdatePerfil` — 2 tests**

- `aplicaCambiosYPersistePorRepositorio`: verifica que `perfilRepository.update()` se invoca con el id correcto y que la respuesta refleja los valores actualizados (sexo, altura, peso, peso objetivo y nivel de actividad).
- `idInexistente_lanzaResourceNotFoundException`: mismo caso de id inexistente aplicado a la actualización.

Un detalle técnico relevante en los tests de TMB es la construcción de la fecha de nacimiento. La fórmula calcula la edad con `LocalDate.now()`, lo que haría que una fecha fija produjera resultados distintos según el día de ejecución. Para evitarlo, la fecha se construye como:

```java
private static final LocalDate NACIMIENTO_30 = LocalDate.now().minusYears(30);
```

De este modo `Period.between` devuelve siempre 30 años exactos, con independencia de cuándo se ejecuten los tests, y los valores esperados son estables.
