package Information.Intelligenze

import Information.Geography.Types.Base

object BaseFilterExpansions {

  def apply(base: Base): Boolean = {
    if ( ! base.owner.isNeutral) return false
    if (base.natural.exists( ! _.owner.isNeutral)) return false
    if (base.isNaturalOf.exists( ! _.owner.isNeutral)) return false
    true
  }
}
