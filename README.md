# C.A.A.S
App Android para control de almacén y administración eficiente de stock en tiempo real.

---

## Estado de Requerimientos Funcionales

> Última actualización: 10 de abril de 2026 — Rama: `main`

### Progreso General

| Métrica | Valor |
|---|---|
| Total de RF definidos | 52 |
| Implementados | 25 |
| Parciales | 0 |
| Pendientes | 27 |
| **% de avance** | **~48%** |

---

### RF Implementados

| ID | Descripción | Módulo |
|---|---|---|
| RF-01 | Registrarse con correo electrónico y contraseña. | Autenticación |
| RF-02 | Iniciar sesión con credenciales registradas. | Autenticación |
| RF-03 | Cerrar sesión desde cualquier pantalla principal. | Autenticación |
| RF-04 | Recuperar contraseña mediante correo electrónico. | Autenticación |
| RF-05 | Crear un negocio ingresando nombre, sector y datos de identificación. | Negocio |
| RF-06 | Editar la información de su negocio en cualquier momento. | Negocio |
| RF-07 | Asociar todo el contenido del sistema al negocio del usuario autenticado. | Negocio |
| RF-08 | Crear una o más sucursales dentro de su negocio. | Sucursales |
| RF-09 | Editar y eliminar sucursales existentes. | Sucursales |
| RF-10 | Mostrar una lista de todas las sucursales registradas del negocio. | Sucursales |
| RF-11 | Registrar entradas de stock de un producto en una sucursal específica. | Stock |
| RF-12 | Registrar salidas de stock indicando el motivo (venta, daño o traslado). | Stock |
| RF-13 | Mostrar el stock actual de cada producto por sucursal en tiempo real. | Stock |
| RF-14 | Generar una alerta cuando el stock esté por debajo del mínimo configurado. | Stock |
| RF-15 | Crear productos con nombre, SKU, categoría, precio de costo, precio de venta e imagen. | Productos |
| RF-16 | Editar y eliminar productos del catálogo. | Productos |
| RF-17 | Buscar y filtrar productos por nombre o categoría. | Productos |
| RF-18 | Registrar proveedores con nombre, teléfono, correo y productos que suministra. | Proveedores |
| RF-19 | Editar y eliminar proveedores registrados. | Proveedores |
| RF-20 | Asociar proveedores a productos específicos del catálogo. | Proveedores |
| RF-21 | Mostrar un resumen del estado del inventario según la sucursal seleccionada. | Inventario |
| RF-22 | Listar los productos con stock crítico (por debajo del mínimo) en la pantalla principal. | Alertas |
| RF-23 | Notificar al usuario cuando un producto alcance un nivel crítico de inventario. | Alertas |
| RF-26 | Asignar un rol (Propietario, Administrador, Empleado) a cada usuario del negocio. | Roles |
| RF-27 | Restringir las funciones visibles y accesibles según el rol del usuario autenticado. | Roles |

---

### RF Pendientes

#### Prioridad Alta — Funcionalidades clave

| ID | Descripción |
|---|---|
| RF-24 | Mostrar alertas dentro del sistema relacionadas con eventos importantes del inventario. |
| RF-25 | Permitir al propietario invitar empleados al negocio mediante correo electrónico. |
| RF-28 | Permitir al propietario revocar el acceso de un empleado en cualquier momento. |
| RF-29 | Permitir asignar un empleado a una sucursal específica. |
| RF-40 | Generar automáticamente una orden de compra cuando el stock baje del mínimo configurado. |
| RF-41 | Crear una orden de compra manual seleccionando proveedor, productos y cantidades. |
| RF-42 | Cambiar el estado de una orden de compra (Pendiente, Enviada, Recibida, Cancelada). |
| RF-43 | Actualizar automáticamente el stock cuando una orden de compra sea marcada como Recibida. |
| RF-44 | Listar el historial de órdenes de compra filtrable por estado, proveedor y fecha. |

#### Prioridad Media — Reportes y exportación

| ID | Descripción |
|---|---|
| RF-34 | Exportar el reporte de inventario de una sucursal en formato PDF. |
| RF-35 | Exportar el historial de movimientos de un período en formato Excel (.xlsx). |
| RF-36 | Compartir el informe generado mediante las opciones nativas de Android. |
| RF-37 | Escanear el código de barras o QR de un producto para autocompletar su SKU al crearlo. |
| RF-38 | Buscar un producto en el inventario escaneando su código de barras o QR. |
| RF-39 | Registrar una entrada o salida de inventario escaneando el código del producto. |
| RF-45 | Registrar automáticamente un historial cada vez que se modifique el precio de un producto. |
| RF-46 | Mostrar el historial de cambios de precio con valor anterior, nuevo valor, fecha y usuario. |
| RF-47 | Visualizar la evolución del precio de costo y venta en un gráfico de línea. |

#### Prioridad Baja — Dashboard y categorías

| ID | Descripción |
|---|---|
| RF-30 | Mostrar gráficos de movimientos de inventario por período (semana, mes, año). |
| RF-31 | Mostrar los productos con mayor y menor rotación dentro de un rango de fechas. |
| RF-32 | Comparar el nivel de stock entre sucursales en una misma vista. |
| RF-33 | Mostrar un resumen de alertas activas (stock bajo) agrupadas por sucursal. |
| RF-48 | Crear categorías de productos personalizadas para el negocio. |
| RF-49 | Crear subcategorías dentro de una categoría existente. |
| RF-50 | Editar y eliminar categorías, siempre que no tengan productos activos asociados. |
| RF-51 | Asignar una categoría y subcategoría a cada producto al crearlo o editarlo. |
| RF-52 | Filtrar el catálogo de productos por categoría y subcategoría. |

---
