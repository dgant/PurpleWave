package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.Buildables.Get
import Planning.Plan
import ProxyBwapi.Techs.Tech

class TechContinuously(tech: Tech) extends Plan {

  override def onUpdate() {
    if (With.self.hasTech(tech)) return
    if ( ! With.units.existsOurs(tech.whatResearches)) return
    With.scheduler.request(this, Get(tech))
  }

  override def toString: String = f"Tech $tech"
}
