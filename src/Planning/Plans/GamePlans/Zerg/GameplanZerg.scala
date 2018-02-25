package Planning.Plans.GamePlans.Zerg

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Matchup.SwitchEnemyRace
import Planning.Plans.Predicates.Milestones.{EnemyUnitsAtLeast, EnemyUnitsAtMost, MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Predicates.Reactive.EnemyBio
import Planning.Plans.Predicates.{Never, SafeAtHome}
import ProxyBwapi.Races.{Protoss, Zerg}

abstract class GameplanZerg extends GameplanModeTemplate {
  
  protected def earlyGame: Plan = NoPlan()
  protected def doneWithEarlyGame: Plan = new Never()
  
  protected def safeToDevelop: Plan = new SafeAtHome()
  protected def readyToKill: Plan = new SwitchEnemyRace(
    whenTerran = new And(
      new UnitsAtLeast(50, UnitMatchWorkers),
      new MiningBasesAtLeast(4)),
    whenProtoss = new And(
      new UnitsAtLeast(75, UnitMatchWorkers),
      new MiningBasesAtLeast(4)),
    whenZerg = new And(
      new UnitsAtLeast(20, UnitMatchWorkers),
      new MiningBasesAtLeast(2)))
  
  protected def goEconomy: Plan = new SwitchEnemyRace(
    whenTerran = new Parallel(
      new TrainContinuously(Zerg.Drone, 12),
      new RequireMiningBases(2),
      new TrainContinuously(Zerg.Drone, 13),
      new RequireMiningBases(3),
      new TrainContinuously(Zerg.Drone, 50),
      new Build(
        RequestAtLeast(4, Zerg.Hatchery),
        RequestAtLeast(1, Zerg.Extractor),
        RequestAtLeast(6, Zerg.Hatchery)),
      new BuildGasPumps,
      new RequireMiningBases(4),
      new TrainContinuously(Zerg.Drone, 50)),
    whenProtoss = new Parallel(
      new TrainContinuously(Zerg.Drone, 12),
      new RequireMiningBases(2),
      new TrainContinuously(Zerg.Drone, 13),
      new RequireMiningBases(3),
      new TrainContinuously(Zerg.Drone, 50),
      new Build(
        RequestAtLeast(4, Zerg.Hatchery),
        RequestAtLeast(1, Zerg.Extractor),
        RequestAtLeast(6, Zerg.Hatchery)),
      new BuildGasPumps,
      new RequireMiningBases(4),
      new TrainContinuously(Zerg.Drone, 75)),
    whenZerg = new Parallel(
      new TrainContinuously(Zerg.Drone, 12),
      new RequireMiningBases(2),
      new BuildGasPumps,
      new TrainContinuously(Zerg.Drone, 20)))
  
  protected def goUpgrades: Plan = new SwitchEnemyRace(
    whenTerran = new If(
      new EnemyBio,
      new Parallel(
        new Build(
          RequestAtLeast(1, Zerg.SpawningPool),
          RequestUpgrade(Zerg.ZerglingSpeed),
          RequestAtLeast(1, Zerg.Lair),
          RequestAtLeast(1, Zerg.HydraliskDen),
          RequestTech(Zerg.LurkerMorph)),
        new UpgradeContinuously(Zerg.ZerglingAttackSpeed),
        new UpgradeContinuously(Zerg.UltraliskArmor),
        new UpgradeContinuously(Zerg.UltraliskSpeed),
        new UpgradeContinuously(Zerg.GroundArmor),
        new UpgradeContinuously(Zerg.GroundMeleeDamage),
        new UpgradeContinuously(Zerg.GroundRangeDamage),
        new Build(
          RequestAtLeast(2, Zerg.EvolutionChamber),
          RequestAtLeast(1, Zerg.QueensNest),
          RequestAtLeast(1, Zerg.Hive),
          RequestAtLeast(1, Zerg.UltraliskCavern))
      ),
      new Parallel(
        new Build(
          RequestAtLeast(1, Zerg.SpawningPool),
          RequestUpgrade(Zerg.ZerglingSpeed),
          RequestAtLeast(1, Zerg.HydraliskDen),
          RequestUpgrade(Zerg.HydraliskRange),
          RequestUpgrade(Zerg.HydraliskSpeed),
          RequestAtLeast(1, Zerg.Lair),
          RequestAtLeast(1, Zerg.Spire)),
        new UpgradeContinuously(Zerg.ZerglingAttackSpeed),
        new UpgradeContinuously(Zerg.AirDamage),
        new UpgradeContinuously(Zerg.AirArmor),
        new UpgradeContinuously(Zerg.GroundRangeDamage),
        new UpgradeContinuously(Zerg.GroundArmor),
        new UpgradeContinuously(Zerg.GroundMeleeDamage),
        new Build(
          RequestAtLeast(2, Zerg.EvolutionChamber),
          RequestAtLeast(1, Zerg.QueensNest),
          RequestAtLeast(1, Zerg.Hive),
          RequestAtLeast(1, Zerg.GreaterSpire))
      )
    ),
    whenProtoss = new Parallel(
      new Build(
        RequestAtLeast(1, Zerg.SpawningPool),
        RequestUpgrade(Zerg.ZerglingSpeed)),
      new UpgradeContinuously(Zerg.ZerglingAttackSpeed),
      new If(
        new Or(
          new EnemyUnitsAtMost(0, Protoss.Corsair),
          new And(
            new UnitsAtLeast(6, Zerg.Mutalisk),
            new EnemyUnitsAtMost(2, Protoss.Corsair))),
        new Parallel(
          new Build(
            RequestAtLeast(1, Zerg.Lair),
            RequestAtLeast(1, Zerg.EvolutionChamber),
            RequestAtLeast(1, Zerg.Spire)),
          new UpgradeContinuously(Zerg.GroundArmor),
          new UpgradeContinuously(Zerg.AirDamage),
          new UpgradeContinuously(Zerg.GroundMeleeDamage),
          new UpgradeContinuously(Zerg.AirArmor),
          new Build(
            RequestAtLeast(1, Zerg.QueensNest),
            RequestAtLeast(1, Zerg.Hive),
            RequestAtLeast(1, Zerg.GreaterSpire))),
        new Parallel(
          new Build(
            RequestAtLeast(1, Zerg.HydraliskDen),
            RequestUpgrade(Zerg.HydraliskSpeed),
            RequestUpgrade(Zerg.HydraliskRange),
            RequestAtLeast(1, Zerg.Lair),
            RequestUpgrade(Zerg.OverlordSpeed),
            RequestTech(Zerg.LurkerMorph),
            RequestAtLeast(2, Zerg.EvolutionChamber)),
          new UpgradeContinuously(Zerg.GroundRangeDamage),
          new UpgradeContinuously(Zerg.GroundArmor),
          new Build(
            RequestAtLeast(1, Zerg.QueensNest),
            RequestAtLeast(1, Zerg.Hive)),
          new UpgradeContinuously(Zerg.GroundMeleeDamage)
        )
      )
    ),
    whenZerg = new Parallel(
      new Build(
        RequestAtLeast(1, Zerg.SpawningPool),
        RequestUpgrade(Zerg.ZerglingSpeed),
        RequestAtLeast(1, Zerg.Lair),
        RequestAtLeast(1, Zerg.Spire))
    )
  )
  protected def goDefense: Plan = goOffense
  protected def goOffense: Plan =  new SwitchEnemyRace(
    whenTerran = new Parallel(
      new If(
        new EnemyBio,
        new Parallel(
          new UpgradeContinuously(Zerg.GroundArmor),
          new UpgradeContinuously(Zerg.GroundMeleeDamage),
          new UpgradeContinuously(Zerg.UltraliskArmor),
          new UpgradeContinuously(Zerg.UltraliskSpeed),
          new TrainContinuously(Zerg.Ultralisk),
          new TrainContinuously(Zerg.Lurker),
          new TrainContinuously(Zerg.Hydralisk),
          new TrainContinuously(Zerg.Mutalisk),
          new UpgradeContinuously(Zerg.GroundRangeDamage),
          new UpgradeContinuously(Zerg.ZerglingSpeed),
          new TrainContinuously(Zerg.Zergling)
        ),
        new Parallel(
          new TrainContinuously(Zerg.Ultralisk),
          new TrainContinuously(Zerg.Mutalisk),
          new TrainContinuously(Zerg.Hydralisk),
          new TrainContinuously(Zerg.Zergling),
          new UpgradeContinuously(Zerg.ZerglingSpeed)
        )
      )),
    whenProtoss = new Parallel(
      new If(
        new EnemyUnitsAtMost(3, Protoss.Corsair),
        new Parallel(
          new TrainContinuously(Zerg.Guardian, 8),
          new If(
            new EnemyUnitsAtLeast(1, Protoss.Corsair),
            new TrainContinuously(Zerg.Scourge, 4)),
          new TrainContinuously(Zerg.Mutalisk)),
        new Parallel(
          new TrainContinuously(Zerg.Lurker, 8),
          new TrainContinuously(Zerg.Hydralisk))),
      new TrainContinuously(Zerg.Zergling)
    ),
    whenZerg = new Parallel(
      new TrainContinuously(Zerg.Hydralisk),
      new If(
        new Check(() => {
          val ourMutalisks    = With.units.ours.count(_.is(Zerg.Mutalisk))
          val ourScourge      = With.units.ours.count(_.is(Zerg.Scourge))
          val enemyMutalisks  = With.units.enemy.count(_.is(Zerg.Mutalisk))
          (
            enemyMutalisks > 0
              && ourScourge < enemyMutalisks * 2
              && ourScourge < ourMutalisks * 2
          )
        }),
        new TrainContinuously(Zerg.Scourge),
        new TrainContinuously(Zerg.Mutalisk)
      ),
      new TrainContinuously(Zerg.Zergling)
    )
  )
  
  final override lazy val buildPlans = Vector(
    new If(
      new Not(doneWithEarlyGame),
      earlyGame,
      new If(
        safeToDevelop,
        new If(
          readyToKill,
          new Parallel(
            goUpgrades,
            goOffense,
            goEconomy),
          new Parallel(
            goEconomy,
            goUpgrades,
            goOffense)),
        new Parallel(
          goDefense,
          goUpgrades,
          goEconomy))
    )
  )
}
