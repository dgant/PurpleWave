package Planning.Plans.GamePlans.Zerg

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Planning.Predicates.Compound.{And, Check}
import Planning.UnitMatchers._
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.Macro.Automatic.{Enemy, Pump, PumpRatio}
import Planning.Predicates.Milestones.{EnemiesAtLeast, EnemiesAtMost, FrameAtMost}
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

object ZergIdeas {
  
  class SafeForOverlords extends And(
    new FrameAtMost(GameTime(4, 0)()),
    new EnemiesAtMost(0, UnitMatchOr(
      UnitMatchAntiAir,
      Terran.Barracks,
      Terran.Marine,
      Protoss.Stargate,
      Protoss.CyberneticsCore,
      Protoss.Dragoon,
      Zerg.HydraliskDen,
      Zerg.Spire,
      Zerg.Mutalisk,
      Zerg.Scourge)))
  
  class ScoutSafelyWithOverlord extends If(
    new SafeForOverlords,
    new Scout(3) { scouts.get.unitMatcher.set(Zerg.Overlord) })
  
  class ScoutSafelyWithDrone extends If(new EnemiesAtMost(0, UnitMatchAntiGround), new Scout)
  
  class PumpJustEnoughZerglings(minimum: Int = 2, maximum: Int = 12) extends PumpRatio(
    Zerg.Zergling, minimum, maximum,
    Seq(
      Enemy(Terran.Marine, 1.75),
      Enemy(Terran.Medic, 3.0),
      Enemy(Terran.Firebat, 3.0),
      Enemy(Terran.Ghost, 2.0),
      // Ignore Vultures
      Enemy(Terran.Goliath, 4.0),
      Enemy(UnitMatchSiegeTank, 4.0),
      Enemy(Protoss.Zealot, 4.5),
      Enemy(Protoss.Dragoon, 3.0),
      Enemy(Protoss.DarkTemplar, 6.0),
      Enemy(Protoss.Reaver, 8.0),
      // Ignore Archons
      Enemy(Zerg.Zergling, 1.5),
      Enemy(Zerg.Hydralisk, 3.0),
      Enemy(Zerg.Ultralisk, 10.0),
      Enemy(UnitMatchAnd(UnitMatchProxied, Terran.Bunker), 12.0),
      Enemy(UnitMatchAnd(UnitMatchProxied, Protoss.PhotonCannon), 8.0),
      Enemy(UnitMatchAnd(UnitMatchProxied, Zerg.CreepColony), 4.0),
      Enemy(UnitMatchAnd(UnitMatchProxied, Zerg.SunkenColony), 8.0)))
  
  class PumpJustEnoughHydralisks(minimum: Int = 0, maximum: Int = 100) extends PumpRatio(
    Zerg.Hydralisk, minimum, maximum,
    Seq(
      Enemy(Terran.Marine, 0.75),
      Enemy(Terran.Medic, 1.0),
      Enemy(Terran.Firebat, 0.75),
      Enemy(Terran.Ghost, 0.5),
      // Ignore Vultures
      Enemy(Terran.Goliath, 2.0),
      Enemy(Terran.Wraith, 2.0),
      Enemy(Terran.Battlecruiser, 4.0),
      Enemy(UnitMatchSiegeTank, 4.0),
      Enemy(Protoss.Zealot, 1.5),
      Enemy(Protoss.Dragoon, 1.5),
      Enemy(Protoss.DarkTemplar, 2.0),
      Enemy(Protoss.Reaver, 3.0),
      Enemy(Protoss.Corsair, 2.0),
      Enemy(Protoss.Scout, 3.0),
      Enemy(Protoss.Carrier, 5.0),
      // Ignore Archons
      Enemy(Zerg.Zergling, 0.5),
      Enemy(Zerg.Hydralisk, 1.0),
      Enemy(Zerg.Ultralisk, 4.0),
      Enemy(UnitMatchAnd(UnitMatchProxied, Terran.Bunker), 5.0),
      Enemy(UnitMatchAnd(UnitMatchProxied, Protoss.PhotonCannon), 4.0),
      Enemy(UnitMatchAnd(UnitMatchProxied, Zerg.CreepColony), 2.0),
      Enemy(UnitMatchAnd(UnitMatchProxied, Zerg.SunkenColony), 4.0)))
  
  class TrainJustEnoughScourge extends Trigger(
    new EnemiesAtLeast(1, UnitMatchOr(
      Terran.Wraith,
      Terran.Valkyrie,
      Terran.Starport,
      Protoss.Corsair,
      Protoss.Stargate,
      Zerg.Mutalisk,
      Zerg.Scourge,
      Zerg.Spire)),
    new PumpRatio(Zerg.Scourge, 2, 12, Seq(
      Enemy(Terran.Wraith, 2.0),
      Enemy(Terran.Valkyrie, 2.0),
      Enemy(Terran.Battlecruiser, 6.0),
      Enemy(Protoss.Corsair, 2.0),
      Enemy(Protoss.Scout, 3.0),
      Enemy(Protoss.Carrier, 6.0),
      Enemy(Zerg.Mutalisk, 2.0),
      Enemy(Zerg.Scourge, 1.0))))
  
  class PumpMutalisks(maximumConcurrently: Int = 100) extends If(
    new Check(() => With.self.gas > Math.min(100, With.self.minerals)),
    new Pump(Zerg.Mutalisk, maximumConcurrently = maximumConcurrently))
}
