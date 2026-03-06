# C.A.A.S
App Android para control de almacén y administración eficiente de stock en tiempo real.

## Requerimientos Funcionales

Esta sección describe los **requerimientos funcionales del sistema**, los cuales definen las funcionalidades que la aplicación debe ofrecer para la gestión de negocios, inventario, productos, proveedores y sucursales:

---

### Autenticación

| ID | Requerimiento |
|----|---------------|
| RF-01 | El sistema debe permitir al usuario registrarse con correo electrónico y contraseña. |
| RF-02 | El sistema debe permitir al usuario iniciar sesión con sus credenciales registradas. |
| RF-03 | El sistema debe permitir al usuario cerrar sesión desde cualquier pantalla principal. |
| RF-04 | El sistema debe permitir recuperar la contraseña mediante correo electrónico. |

---

### Gestión de Negocio

| ID | Requerimiento |
|----|---------------|
| RF-05 | El sistema debe permitir al usuario crear un negocio ingresando nombre, sector y datos de identificación. |
| RF-06 | El sistema debe permitir al usuario editar la información de su negocio en cualquier momento. |
| RF-07 | El sistema debe asociar todo el contenido del sistema (sucursales, productos y proveedores) al negocio del usuario autenticado. |

---

### Gestión de Sucursales

| ID | Requerimiento |
|----|---------------|
| RF-08 | El sistema debe permitir al usuario crear una o más sucursales dentro de su negocio. |
| RF-09 | El sistema debe permitir editar y eliminar sucursales existentes. |
| RF-10 | El sistema debe mostrar una lista de todas las sucursales registradas del negocio. |

---

### Gestión de Inventario

| ID | Requerimiento |
|----|---------------|
| RF-11 | El sistema debe permitir registrar entradas de stock de un producto en una sucursal específica. |
| RF-12 | El sistema debe permitir registrar salidas de stock indicando el motivo (venta, daño o traslado). |
| RF-13 | El sistema debe mostrar el stock actual de cada producto por sucursal en tiempo real. |
| RF-14 | El sistema debe generar una alerta cuando el stock de un producto esté por debajo del mínimo configurado. |

---

### Gestión de Productos

| ID | Requerimiento |
|----|---------------|
| RF-15 | El sistema debe permitir crear productos con nombre, SKU, categoría, precio de costo, precio de venta e imagen. |
| RF-16 | El sistema debe permitir editar y eliminar productos del catálogo. |
| RF-17 | El sistema debe permitir buscar y filtrar productos por nombre o categoría. |

---

### Gestión de Proveedores

| ID | Requerimiento |
|----|---------------|
| RF-18 | El sistema debe permitir registrar proveedores con nombre, teléfono, correo y productos que suministra. |
| RF-19 | El sistema debe permitir editar y eliminar proveedores registrados. |
| RF-20 | El sistema debe asociar proveedores a productos específicos del catálogo. |

---

### Dashboard

| ID | Requerimiento |
|----|---------------|
| RF-21 | El sistema debe mostrar un resumen del estado del inventario según la sucursal seleccionada. |
| RF-22 | El sistema debe listar los productos con stock crítico (por debajo del mínimo) en la pantalla principal. |

---

### Notificaciones

| ID | Requerimiento |
|----|---------------|
| RF-23 | El sistema debe notificar al usuario cuando un producto alcance un nivel crítico de inventario. |
| RF-24 | El sistema debe mostrar alertas dentro del sistema relacionadas con eventos importantes del inventario. |
