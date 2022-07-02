package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.{FrameCount, Frames}
import Utilities.UnitFilters.UnitFilter

class FingerprintArrivesBy(
  unitMatcher : UnitFilter,
  gameTime    : FrameCount,
  quantity    : Int = 1)
    extends Fingerprint {
  
  override val sticky = true

  protected def matchedUnits  : Iterable[UnitInfo] = With.units.everEnemy.filter(unitMatcher)
  protected def arrivingUnits : Iterable[UnitInfo] = matchedUnits.filter(_.arrivalFrame() < gameTime())
  
  override def investigate: Boolean = {
    // Game time cutoff spares performance
    With.frame < gameTime() && arrivingUnits.size >= quantity
  }

  override def reason: String = f"$quantity $unitMatcher arriving by $gameTime (at least ${arrivingUnits.size} by ${Frames(Maff.max(arrivingUnits.map(_.arrivalFrame())).getOrElse(0))}"
}
