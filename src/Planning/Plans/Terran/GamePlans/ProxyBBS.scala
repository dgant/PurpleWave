package Planning.Plans.Terran.GamePlans

import Information.Geography.Types.Zone
import Planning.ProxyPlanner

class ProxyBBS extends AbstractProxyBBS {
  
  override protected def proxyZone: Option[Zone] = ProxyPlanner.proxyAutomatic
}