package net.sclan.minigames.billing

sealed class PurchaseState {
    object Unknown : PurchaseState()
    object NotPurchased : PurchaseState()
    object Pending : PurchaseState()
    object Purchased : PurchaseState()
}

/** Pure state holder — no Android/Compose deps, fully unit-testable. */
class BillingStateHolder(initial: PurchaseState = PurchaseState.Unknown) {
    private var _state: PurchaseState = initial
    val state: PurchaseState get() = _state

    fun update(s: PurchaseState) { _state = s }
    fun areAdsEnabled(): Boolean = _state !is PurchaseState.Purchased
    fun canPurchase(): Boolean = _state == PurchaseState.NotPurchased || _state == PurchaseState.Unknown
}
