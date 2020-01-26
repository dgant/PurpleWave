package Planning.Plans.Army

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchAntiGround, UnitMatchWarriors}
import ProxyBwapi.Races.Neutral

class KillPsiDisruptor extends Plan() {
  val killers = new LockUnits
  killers.unitMatcher.set(UnitMatchAnd(UnitMatchWarriors, UnitMatchAntiGround))
  killers.unitCounter.set(UnitCountEverything)
  override def onUpdate() {
    val targets = With.geography.ourZones.flatMap(_.units.filter(_.is(Neutral.PsiDisruptor)))
    if (targets.isEmpty) return
    killers.acquire(this)
    killers.units.foreach(killer => killer.agent.intend(this, new Intention {
      toAttack = targets.headOption
    }))
  }
}
