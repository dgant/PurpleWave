package Planning.Plans.Protoss.GamePlans

import Information.Geography.Types.Zone
import Lifecycle.With

class Proxy2GateAtEnemyNatural extends AbstractProxy2Gate {
  override protected def proxyZone: Option[Zone] = {
    With.geography.bases.find(_.isNaturalOf.exists( ! _.owner.isUs)).map(_.zone)
  }
}
