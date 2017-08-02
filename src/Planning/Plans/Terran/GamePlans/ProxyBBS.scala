package Planning.Plans.Terran.GamePlans

import Information.Geography.Types.Zone
import Lifecycle.With
import Planning.ProxyPlanner

class ProxyBBS extends AbstractProxyBBS {
  
  protected def proxyZone: Option[Zone] = {
    if (With.geography.startLocations.size > 2)
      ProxyPlanner.proxyMiddle
    else
      ProxyPlanner.proxyEnemyNatural
  }
}