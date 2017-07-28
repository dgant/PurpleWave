package Planning.Plans.Protoss.GamePlans

import Information.Geography.Types.Zone
import Planning.ProxyPlanner

class Proxy2GateInMiddle extends AbstractProxy2Gate {
  override protected def proxyZone: Option[Zone] = ProxyPlanner.proxyMiddle
}
