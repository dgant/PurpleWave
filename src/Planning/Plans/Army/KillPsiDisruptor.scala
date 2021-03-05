package Planning.Plans.Army

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.{MatchAnd, MatchAntiGround, MatchWarriors}
import ProxyBwapi.Races.Neutral

class KillPsiDisruptor extends Plan() {
  val killers = new LockUnits
  killers.matcher = MatchAnd(MatchWarriors, MatchAntiGround)
  killers.counter = CountEverything
  override def onUpdate() {
    val targets = With.geography.ourZones.flatMap(_.units.filter(_.is(Neutral.PsiDisruptor)))
    if (targets.isEmpty) return
    killers.acquire(this)
    killers.units.foreach(killer => killer.agent.intend(this, new Intention {
      toAttack = targets.headOption
    }))
  }
}
