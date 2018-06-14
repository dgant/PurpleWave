package Planning.Plans.GamePlans.Zerg

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Planning.Predicates.Compound.And
import Planning.UnitMatchers.{UnitMatchAntiAir, UnitMatchAntiGround, UnitMatchOr}
import Planning.Plans.Compound.If
import Planning.Predicates.Milestones.{EnemyUnitsAtMost, FrameAtMost}
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

object ZergIdeas {
  
  class SafeForOverlords extends And(
    new FrameAtMost(GameTime(4, 0)()),
    new EnemyUnitsAtMost(0, UnitMatchOr(
      UnitMatchAntiAir,
      Terran.Barracks,
      Protoss.Stargate,
      Protoss.CyberneticsCore,
      Zerg.HydraliskDen,
      Zerg.Spire)))
  
  class ScoutSafelyWithOverlord extends If(
    new SafeForOverlords,
    new Scout(3) { scouts.get.unitMatcher.set(Zerg.Overlord) })
  
  class ScoutSafelyWithDrone extends If(new EnemyUnitsAtMost(0, UnitMatchAntiGround), new Scout)
}
