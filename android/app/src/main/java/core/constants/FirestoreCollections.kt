package com.caas.app.core.constants

/**
 * Constantes para nombres de colecciones y subcollections en Firestore.
 * Organizado por módulo funcional del negocio.
 */
object FirestoreCollections {
    // Colecciones raíz - Gestión de negocios
    const val BUSINESSES = "businesses"
    const val MEMBERS = "members"
    const val INVITATIONS = "invitations"

    // Colecciones raíz - Gestión de inventario
    const val BRANCHES = "branches"
    const val PRODUCTS = "products"
    const val STOCK = "stock"
    const val MOVEMENTS = "movements"

    // Colecciones raíz - Gestión de compras
    const val PURCHASE_ORDERS = "purchaseOrders"
    const val PROVIDERS = "providers"

    // Colecciones raíz - Gestión de categorías y precios
    const val CATEGORIES = "categories"
    const val PRICE_HISTORY = "priceHistory"

    /**
     * Subcollections dentro de businesses/{businessId}/
     */
    object SubCollections {
        const val BRANCHES_SUB = "branches"
        const val MEMBERS_SUB = "members"
        const val PRODUCTS_SUB = "products"
    }
}

/**
 * Constantes para campos comunes en documentos Firestore.
 * Garantiza consistencia de nombres de campos en toda la aplicación.
 */
object FirestoreFields {
    // Identificadores
    const val BUSINESS_ID = "businessId"
    const val OWNER_ID = "ownerId"
    const val USER_ID = "userId"
    const val BRANCH_ID = "branchId"
    const val PRODUCT_ID = "productId"
    const val PROVIDER_ID = "providerId"

    // Timestamps
    const val CREATED_AT = "createdAt"
    const val UPDATED_AT = "updatedAt"
    const val JOINED_AT = "joinedAt"
    const val INVITED_AT = "invitedAt"

    // Estado
    const val IS_ACTIVE = "isActive"
    const val STATUS = "status"
    const val ROLE = "role"

    // Datos comunes
    const val NAME = "name"
    const val EMAIL = "email"
    const val PHONE = "phone"
    const val ADDRESS = "address"

    // Campos de inventario
    const val SKU = "sku"
    const val QUANTITY = "quantity"
    const val MIN_STOCK = "minStock"
    const val COST_PRICE = "costPrice"
    const val SALE_PRICE = "salePrice"
    const val CATEGORY_ID = "categoryId"
    const val IMAGE_URL = "imageUrl"

    // Campos de movimiento
    const val REASON = "reason"
    const val PREVIOUS_PRICE = "previousPrice"
    const val NEW_PRICE = "newPrice"
}