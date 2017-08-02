package Planning.Plans.Terran.GamePlans

import Information.Geography.Types.Zone
import Planning.ProxyPlanner

class BBSInMiddle extends AbstractProxyBBS {
  override protected def proxyZone: Option[Zone] = ProxyPlanner.proxyMiddle
}
