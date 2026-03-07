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

## Requerimientos No Funcionales

Esta sección describe los requerimientos no funcionales de la app:

---

| ID | Requerimiento |
|----|---------------|
| RNF-01 | (Rendimiento) El sistema debe cargar el Dashboard principal en un tiempo máximo de **3 segundos** bajo condiciones normales de red. |
| RNF-02 | (Rendimiento) El sistema debe reflejar los cambios de **stock en tiempo real** con una latencia máxima de **2 segundos** tras registrar un movimiento. |
| RNF-03 | (Rendimiento) Las **notificaciones de stock bajo** deben enviarse al usuario en un tiempo máximo de **5 segundos** después de detectarse la condición. |
| RNF-04 | (Usabilidad) La interfaz debe seguir los principios de diseño **Material Design 3 de Android** para garantizar una experiencia consistente y familiar al usuario. |
| RNF-05 | (Usabilidad) El flujo de **onboarding (registro → crear negocio)** no debe requerir más de **3 pantallas** para que el usuario pueda comenzar a usar la app. |
| RNF-06 | (Usabilidad) Todos los **formularios** deben mostrar **mensajes de error descriptivos** cuando el usuario ingrese datos inválidos o incompletos. |
| RNF-07 | (Seguridad) Las **contraseñas de los usuarios** deben ser gestionadas exclusivamente por **Firebase Authentication**, sin almacenarse en texto plano en ninguna capa del sistema. |
| RNF-08 | (Seguridad) El acceso a los **datos del negocio** debe estar restringido únicamente al **usuario propietario autenticado** mediante reglas de seguridad en **Firebase Firestore**. |
| RNF-09 | (Seguridad) La **sesión del usuario** debe expirar automáticamente tras un periodo prolongado de **inactividad**. |
| RNF-10 | (Disponibilidad) El sistema debe estar disponible el **99% del tiempo**, aprovechando la infraestructura de **alta disponibilidad de Firebase**. |
| RNF-11 | (Disponibilidad) La aplicación debe mostrar un **mensaje informativo** al usuario cuando **no haya conexión a internet**, indicando que los datos no pueden sincronizarse. |
| RNF-12 | (Escalabilidad) La arquitectura basada en **Firebase Firestore** debe soportar el crecimiento del **número de sucursales, productos y proveedores** sin degradar el rendimiento. |
| RNF-13 | (Escalabilidad) El **modelo de datos** debe permitir que un mismo usuario pueda **gestionar múltiples negocios** en el futuro sin requerir cambios estructurales mayores. |
| RNF-14 | (Mantenibilidad) El **código fuente** debe seguir la arquitectura **MVVM** para facilitar la separación de responsabilidades y el mantenimiento futuro. |
| RNF-15 | (Mantenibilidad) El proyecto debe estar **versionado en GitHub** con **commits descriptivos** que reflejen los cambios realizados en cada módulo. |
| RNF-16 | (Compatibilidad) La aplicación debe ser compatible con **dispositivos Android desde la versión 8.0 (API 26)** en adelante. |
| RNF-17 | (Compatibilidad) La interfaz debe **adaptarse correctamente a diferentes tamaños de pantalla** (teléfonos de **5" a 7"**) sin distorsionar los elementos visuales. |
| RNF-18 | (Portabilidad) La aplicación debe poder **instalarse mediante un archivo APK** o **distribuirse a través de Google Play Store** sin requerir configuración adicional por parte del usuario. |
