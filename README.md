# C.A.A.S
App Android para control de almacén y administración eficiente de stock en tiempo real.

---

## Estado de Requerimientos Funcionales 

> Última actualización: 27 de marzo de 2026 — Rama: `main`

### Progreso General

| Métrica | Valor |
|---|---|
| Total de RF definidos | 52 |
| Implementados | 12 |
| Parciales | 1 |
| Pendientes | 39 |
| **% de avance** | **~23%** |

---

### RF Implementados

| ID | Descripción | Módulo |
|---|---|---|
| RF-01 | El sistema debe permitir al usuario registrarse con correo electrónico y contraseña. | Autenticación |
| RF-02 | El sistema debe permitir al usuario iniciar sesión con sus credenciales registradas. | Autenticación |
| RF-03 | El sistema debe permitir al usuario cerrar sesión desde cualquier pantalla principal. | Autenticación |
| RF-05 | El sistema debe permitir al usuario crear un negocio ingresando nombre, sector y datos de identificación. | Negocio |
| RF-06 | El sistema debe permitir al usuario editar la información de su negocio en cualquier momento. | Negocio |
| RF-07 | El sistema debe asociar todo el contenido del sistema al negocio del usuario autenticado. | Negocio |
| RF-08 | El sistema debe permitir al usuario crear una o más sucursales dentro de su negocio. | Sucursales |
| RF-09 | El sistema debe permitir editar y eliminar sucursales existentes. | Sucursales |
| RF-10 | El sistema debe mostrar una lista de todas las sucursales registradas del negocio. | Sucursales |
| RF-26 | El sistema debe permitir asignar un rol (Propietario, Administrador, Empleado) a cada usuario del negocio. | Roles |
| RF-27 | El sistema debe restringir las funciones visibles y accesibles según el rol del usuario autenticado. *(infraestructura base)* | Roles |

---

### RF Parciales

| ID | Descripción | Qué falta |
|---|---|---|
| RF-04 | El sistema debe permitir recuperar la contraseña mediante correo electrónico. | Firebase Auth disponible, sin UI completamente implementada |

---

### RF Pendientes

#### Prioridad Alta — Núcleo del sistema

| ID | Descripción |
|---|---|
| RF-15 | El sistema debe permitir crear productos con nombre, SKU, categoría, precio de costo, precio de venta e imagen. |
| RF-16 | El sistema debe permitir editar y eliminar productos del catálogo. |
| RF-17 | El sistema debe permitir buscar y filtrar productos por nombre o categoría. |
| RF-11 | El sistema debe permitir registrar entradas de stock de un producto en una sucursal específica. |
| RF-12 | El sistema debe permitir registrar salidas de stock indicando el motivo (venta, daño o traslado). |
| RF-13 | El sistema debe mostrar el stock actual de cada producto por sucursal en tiempo real. |
| RF-14 | El sistema debe generar una alerta cuando el stock de un producto esté por debajo del mínimo configurado. |

#### Prioridad Media — Dependientes del núcleo

| ID | Descripción |
|---|---|
| RF-18 | El sistema debe permitir registrar proveedores con nombre, teléfono, correo y productos que suministra. |
| RF-19 | El sistema debe permitir editar y eliminar proveedores registrados. |
| RF-20 | El sistema debe asociar proveedores a productos específicos del catálogo. |
| RF-21 | El sistema debe mostrar un resumen del estado del inventario según la sucursal seleccionada. |
| RF-22 | El sistema debe listar los productos con stock crítico (por debajo del mínimo) en la pantalla principal. |
| RF-23 | El sistema debe notificar al usuario cuando un producto alcance un nivel crítico de inventario. |
| RF-24 | El sistema debe mostrar alertas dentro del sistema relacionadas con eventos importantes del inventario. |
| RF-25 | El sistema debe permitir al propietario 
