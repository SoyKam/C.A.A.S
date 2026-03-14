# C.A.A.S
App Android para control de almacén y administración eficiente de stock en tiempo real.

## Asignación de Requerimientos Funcionales (RF)

---

## Frontend (UI / pantallas) — *Asignado al colaborador Frontend*

| ID | Requerimiento |
|---|---|
| RF-01 | El sistema debe permitir al usuario registrarse con correo electrónico y contraseña. |
| RF-02 | El sistema debe permitir al usuario iniciar sesión con sus credenciales registradas. |
| RF-03 | El sistema debe permitir al usuario cerrar sesión desde cualquier pantalla principal. |
| RF-04 | El sistema debe permitir recuperar la contraseña mediante correo electrónico. |
| RF-05 | El sistema debe permitir al usuario crear un negocio ingresando nombre, sector y datos de identificación. |
| RF-06 | El sistema debe permitir al usuario editar la información de su negocio en cualquier momento. |
| RF-08 | El sistema debe permitir al usuario crear una o más sucursales dentro de su negocio. |
| RF-09 | El sistema debe permitir editar y eliminar sucursales existentes. |
| RF-10 | El sistema debe mostrar una lista de todas las sucursales registradas del negocio. |
| RF-15 | El sistema debe permitir crear productos con nombre, SKU, categoría, precio de costo, precio de venta e imagen. |
| RF-16 | El sistema debe permitir editar y eliminar productos del catálogo. |
| RF-17 | El sistema debe permitir buscar y filtrar productos por nombre o categoría. |
| RF-18 | El sistema debe permitir registrar proveedores con nombre, teléfono, correo y productos que suministra. |
| RF-19 | El sistema debe permitir editar y eliminar proveedores registrados. |
| RF-21 | El sistema debe mostrar un resumen del estado del inventario según la sucursal seleccionada. |
| RF-22 | El sistema debe listar los productos con stock crítico (por debajo del mínimo) en la pantalla principal. |
| RF-24 | El sistema debe mostrar alertas dentro del sistema relacionadas con eventos importantes del inventario. |
| RF-30 | El sistema debe mostrar gráficos de movimientos de inventario (entradas y salidas) por período de tiempo seleccionable (semana, mes, año). |
| RF-31 | El sistema debe mostrar los productos con mayor y menor rotación dentro de un rango de fechas. |
| RF-32 | El sistema debe permitir comparar el nivel de stock entre sucursales en una misma vista. |
| RF-33 | El sistema debe mostrar un resumen de alertas activas (stock bajo) agrupadas por sucursal. |
| RF-48 | El sistema debe permitir al propietario o administrador crear categorías de productos personalizadas para su negocio. |
| RF-49 | El sistema debe permitir crear subcategorías dentro de una categoría existente. |
| RF-50 | El sistema debe permitir editar y eliminar categorías, siempre que no tengan productos activos asociados. |
| RF-51 | El sistema debe permitir asignar una categoría y subcategoría a cada producto al momento de crearlo o editarlo. |
| RF-52 | El sistema debe permitir filtrar el catálogo de productos por categoría y subcategoría desde la pantalla de lista de productos. |

---

## Backend / Lógica / Datos — *Asignado a SoyKam*

| ID | Requerimiento |
|---|---|
| RF-07 | El sistema debe asociar todo el contenido del sistema (sucursales, productos y proveedores) al negocio del usuario autenticado. |
| RF-11 | El sistema debe permitir registrar entradas de stock de un producto en una sucursal específica. |
| RF-12 | El sistema debe permitir registrar salidas de stock indicando el motivo (venta, daño o traslado). |
| RF-13 | El sistema debe mostrar el stock actual de cada producto por sucursal en tiempo real. |
| RF-14 | El sistema debe generar una alerta cuando el stock de un producto esté por debajo del mínimo configurado. |
| RF-20 | El sistema debe asociar proveedores a productos específicos del catálogo. |
| RF-23 | El sistema debe notificar al usuario cuando un producto alcance un nivel crítico de inventario. |
| RF-25 | El sistema debe permitir al propietario invitar empleados al negocio mediante correo electrónico. |
| RF-26 | El sistema debe permitir asignar un rol (Propietario, Administrador, Empleado) a cada usuario del negocio. |
| RF-27 | El sistema debe restringir las funciones visibles y accesibles según el rol del usuario autenticado. |
| RF-28 | El sistema debe permitir al propietario revocar el acceso de un empleado en cualquier momento. |
| RF-29 | El sistema debe permitir asignar un empleado a una sucursal específica. |
| RF-34 | El sistema debe permitir exportar el reporte de inventario de una sucursal en formato PDF. |
| RF-35 | El sistema debe permitir exportar el historial de movimientos de un período en formato Excel (.xlsx). |
| RF-36 | El sistema debe permitir compartir el informe generado mediante las opciones nativas de Android (WhatsApp, correo, Drive, etc.). |
| RF-37 | El sistema debe permitir escanear el código de barras o QR de un producto usando la cámara del dispositivo para autocompletar su SKU al momento de crearlo. |
| RF-38 | El sistema debe permitir buscar un producto en el inventario escaneando su código de barras o QR desde la pantalla de inventario de una sucursal. |
| RF-39 | El sistema debe permitir registrar una entrada o salida de inventario escaneando el código del producto directamente, sin necesidad de buscarlo manualmente. |
| RF-40 | El sistema debe generar automáticamente una orden de compra sugerida cuando el stock de un producto baje del mínimo configurado. |
| RF-41 | El sistema debe permitir al usuario crear una orden de compra manual seleccionando proveedor, productos y cantidades a solicitar. |
| RF-42 | El sistema debe permitir cambiar el estado de una orden de compra (Pendiente, Enviada, Recibida, Cancelada). |
| RF-43 | El sistema debe actualizar automáticamente el stock de la sucursal correspondiente cuando una orden de compra sea marcada como Recibida. |
| RF-44 | El sistema debe listar el historial de órdenes de compra filtrable por estado, proveedor y fecha. |
| RF-45 | El sistema debe registrar automáticamente un nuevo registro en el historial cada vez que se modifique el precio de costo o de venta de un producto. |
| RF-46 | El sistema debe mostrar el historial de cambios de precio indicando el valor anterior, el nuevo valor, la fecha y el usuario que realizó el cambio. |
| RF-47 | El sistema debe permitir visualizar la evolución del precio de costo y venta de un producto en un gráfico de línea dentro de su detalle. |

---
