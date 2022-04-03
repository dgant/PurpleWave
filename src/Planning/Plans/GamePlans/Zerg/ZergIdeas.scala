package Planning.Plans.GamePlans.Zerg

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.Macro.Automatic.{Enemy, Pump, PumpRatio, UpgradeContinuously}
import Planning.Plans.Scouting.ScoutNow
import Planning.Predicates.Compound.{And, Check, Or}
import Planning.Predicates.Milestones.{EnemiesAtMost, EnemyHasShown, FrameAtMost, UpgradeComplete}
import Utilities.UnitMatchers._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.Time.GameTime

object ZergIdeas {
  
  class SafeForOverlords extends And(
    new FrameAtMost(GameTime(4, 0)()),
    new EnemiesAtMost(0, MatchOr(
      MatchAntiAir,
      Terran.Barracks,
      Terran.Marine,
      Protoss.Stargate,
      Protoss.CyberneticsCore,
      Protoss.Dragoon,
      Protoss.Stargate,
      Protoss.Corsair,
      Protoss.Scout,
      Zerg.HydraliskDen,
      Zerg.Spire,
      Zerg.Mutalisk,
      Zerg.Scourge)))
  
  class ScoutSafelyWithDrone extends If(new EnemiesAtMost(0, MatchAntiGround), new ScoutNow)
  
  class PumpJustEnoughZerglings(minimum: Int = 2, maximum: Int = 12) extends PumpRatio(
    Zerg.Zergling, minimum, maximum,
    Seq(
      Enemy(Terran.Marine, 1.75),
      Enemy(Terran.Medic, 3.0),
      Enemy(Terran.Firebat, 3.0),
      Enemy(Terran.Ghost, 2.0),
      // Ignore Vultures
      Enemy(Terran.Goliath, 4.0),
      Enemy(MatchTank, 4.0),
      Enemy(Protoss.Zealot, 4.5),
      Enemy(Protoss.Dragoon, 3.0),
      Enemy(Protoss.DarkTemplar, 6.0),
      Enemy(Protoss.Reaver, 8.0),
      // Ignore Archons
      Enemy(Zerg.Zergling, 1.5),
      Enemy(Zerg.Hydralisk, 3.0),
      Enemy(Zerg.Ultralisk, 10.0),
      Enemy(MatchAnd(MatchProxied, Terran.Bunker), 12.0),
      Enemy(MatchAnd(MatchProxied, Protoss.PhotonCannon), 8.0),
      Enemy(MatchAnd(MatchProxied, Zerg.CreepColony), 4.0),
      Enemy(MatchAnd(MatchProxied, Zerg.SunkenColony), 8.0)))
  
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
      Enemy(MatchTank, 4.0),
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
      Enemy(MatchAnd(MatchProxied, Terran.Bunker), 5.0),
      Enemy(MatchAnd(MatchProxied, Protoss.PhotonCannon), 4.0),
      Enemy(MatchAnd(MatchProxied, Zerg.CreepColony), 2.0),
      Enemy(MatchAnd(MatchProxied, Zerg.SunkenColony), 4.0)))
  
  class PumpJustEnoughScourge extends Trigger(
    new Or(
      new EnemyHasShown(Terran.Wraith),
      new EnemyHasShown(Terran.Valkyrie),
      new EnemyHasShown(Terran.Starport),
      new EnemyHasShown(Protoss.Corsair),
      new EnemyHasShown(Protoss.Stargate),
      new EnemyHasShown(Zerg.Mutalisk),
      new EnemyHasShown(Zerg.Scourge),
      new EnemyHasShown(Zerg.Spire)),
    new PumpRatio(Zerg.Scourge, 2, 12, Seq(
      Enemy(Terran.Wraith, 2.0),
      Enemy(Terran.Valkyrie, 3.0),
      Enemy(Terran.Battlecruiser, 6.0),
      Enemy(Protoss.Corsair, 2.0),
      Enemy(Protoss.Scout, 3.0),
      Enemy(Protoss.Carrier, 6.0),
      Enemy(Zerg.Mutalisk, 2.0),
      Enemy(Zerg.Scourge, 1.0))))

  class UpgradeHydraSpeedThenRange extends If(
    new UpgradeComplete(Zerg.HydraliskSpeed),
    new UpgradeContinuously(Zerg.HydraliskRange),
    new UpgradeContinuously(Zerg.HydraliskSpeed))

  class UpgradeHydraRangeThenSpeed extends If(
    new UpgradeComplete(Zerg.HydraliskRange),
    new UpgradeContinuously(Zerg.HydraliskSpeed),
    new UpgradeContinuously(Zerg.HydraliskRange))

  class UpgradeUltraArmorThenSpeed extends If(
    new UpgradeComplete(Zerg.UltraliskArmor),
    new UpgradeContinuously(Zerg.UltraliskSpeed),
    new UpgradeContinuously(Zerg.UltraliskArmor))

  class UpgradeUltraSpeedThenArmor extends If(
    new UpgradeComplete(Zerg.UltraliskSpeed),
    new UpgradeContinuously(Zerg.UltraliskArmor),
    new UpgradeContinuously(Zerg.UltraliskSpeed))

  class MorphLurkers(count: Int = 100) extends Plan {
    override protected def onUpdate(): Unit = {
      With.scheduler.request(this, Get(
        With.units.countOurs(Zerg.Lurker)
        + With.units.countOurs(Zerg.Hydralisk),
        Zerg.Lurker))
    }
  }

  class PumpMutalisks(maximumConcurrently: Int = 100) extends If(
    new Check(() => With.self.gas > Math.min(100, With.self.minerals)),
    new Pump(Zerg.Mutalisk, maximumConcurrently = maximumConcurrently))
}
