package net.sclan.minigames

import net.sclan.minigames.billing.BillingStateHolder
import net.sclan.minigames.billing.PurchaseState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BillingStateTest {

    private fun holder(initial: PurchaseState = PurchaseState.Unknown) = BillingStateHolder(initial)

    @Test fun `initial state is Unknown`() =
        assertEquals(PurchaseState.Unknown, holder().state)

    @Test fun `ads enabled in Unknown state`() =
        assertTrue(holder(PurchaseState.Unknown).areAdsEnabled())

    @Test fun `ads enabled when not purchased`() =
        assertTrue(holder(PurchaseState.NotPurchased).areAdsEnabled())

    @Test fun `ads enabled in Pending state`() =
        assertTrue(holder(PurchaseState.Pending).areAdsEnabled())

    @Test fun `ads disabled after purchase`() =
        assertFalse(holder(PurchaseState.Purchased).areAdsEnabled())

    @Test fun `can purchase when Unknown`() =
        assertTrue(holder(PurchaseState.Unknown).canPurchase())

    @Test fun `can purchase when NotPurchased`() =
        assertTrue(holder(PurchaseState.NotPurchased).canPurchase())

    @Test fun `cannot purchase when Pending`() =
        assertFalse(holder(PurchaseState.Pending).canPurchase())

    @Test fun `cannot purchase when already Purchased`() =
        assertFalse(holder(PurchaseState.Purchased).canPurchase())

    @Test fun `update transitions state correctly`() {
        val h = holder()
        h.update(PurchaseState.NotPurchased)
        assertEquals(PurchaseState.NotPurchased, h.state)
        h.update(PurchaseState.Purchased)
        assertEquals(PurchaseState.Purchased, h.state)
        assertFalse(h.areAdsEnabled())
    }

    @Test fun `update to Pending keeps ads enabled`() {
        val h = holder(PurchaseState.NotPurchased)
        h.update(PurchaseState.Pending)
        assertTrue(h.areAdsEnabled())
    }
}
