package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Matchup.EnemyIsZerg
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Predicates.Economy.{GasAtLeast, MineralsAtLeast, MineralsAtMost}
import Planning.Plans.Predicates.Milestones.{EnemyUnitsAtLeast, UnitsAtMost, UpgradeComplete}
import ProxyBwapi.Races.{Terran, Zerg}

class TwoHatchMuta extends GameplanModeTemplate {
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    RequestAtLeast(9, Zerg.Drone),
    RequestAtLeast(2, Zerg.Overlord),
    RequestAtLeast(12, Zerg.Drone))
  
  override def blueprints: Seq[Blueprint] = Vector(
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)))
  
  override def defaultAttackPlan: Plan = new Parallel(
    new Attack { attackers.get.unitMatcher.set(Zerg.Mutalisk) },
    new If(
      new Or(
        new Not(new EnemyIsZerg),
        new UpgradeComplete(Zerg.ZerglingSpeed)),
      new Attack))
  
  override def defaultScoutPlan: Plan = NoPlan()
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtMost(0, Zerg.Lair),
      new Do(() => {
        With.blackboard.gasTargetRatio = 3.0 / 12.0
        With.blackboard.gasLimitFloor = 100
        With.blackboard.gasLimitCeiling = 100
      }),
      new If(
        new UnitsAtMost(0, Zerg.Spire),
        new Do(() => {
          With.blackboard.gasTargetRatio = 2.0 / 9.0
          With.blackboard.gasLimitFloor = 200
          With.blackboard.gasLimitCeiling = 200
        }),
        new Do(() => {
          With.blackboard.gasTargetRatio = (if (With.self.gas > With.self.minerals) 1.0 else 3.0) / 9.0
          With.blackboard.gasLimitFloor = 100
          With.blackboard.gasLimitCeiling = 300
        }))),
    new Build(RequestAtLeast(8, Zerg.Drone)),
    new TrainContinuously(Zerg.SunkenColony),
    new If(
      new EnemyIsZerg,
      new Parallel(
        new BuildOrder(
          RequestAtLeast(1, Zerg.SpawningPool),
          RequestAtLeast(13, Zerg.Drone),
          RequestAtLeast(1, Zerg.Extractor)),
        new RequireMiningBases(2),
        new BuildOrder(
          RequestAtLeast(6, Zerg.Zergling),
          RequestUpgrade(Zerg.ZerglingSpeed))),
      new Parallel(
        new RequireMiningBases(2),
        new Build(
          RequestAtLeast(1, Zerg.SpawningPool),
          RequestAtLeast(1, Zerg.Extractor)))),
    new If(
      new Or(new GasAtLeast(70), new MineralsAtMost(200)),
      new If(
        new And(
          new EnemyUnitsAtLeast(3, Zerg.Mutalisk),
          new Check(() =>
            With.units.countOurs(Zerg.Mutalisk) * 2 >=
            With.units.countOurs(Zerg.Scourge))),
        new TrainContinuously(Zerg.Scourge),
        new TrainContinuously(Zerg.Mutalisk))),
    new If(
      new And(
        new Or(
          new EnemyUnitsAtLeast(1, Terran.Vulture),
          new EnemyUnitsAtLeast(1, Terran.Factory)),
        new UnitsAtMost(0, Zerg.SunkenColony)),
      new Build(RequestAtLeast(1, Zerg.CreepColony))),
    new If(
      new Or(
        new Check(() =>
          With.units.countOurs(_.unitClass.isWorker) / 9 >
          With.geography.bases.size),
        new And(
          new UnitsAtMost(0, Zerg.Mutalisk),
          new UnitsAtMost(6, Zerg.Zergling)),
        new And(
          new EnemyIsZerg,
          new UnitsAtMost(14, Zerg.Zergling)),
        new Check(() => With.geography.ourBases.exists(_.units.exists(u =>
          u.isEnemy
          && u.unitClass.attacksGround
            && ! u.flying
          && ! u.unitClass.isWorker)))),
      new TrainContinuously(Zerg.Zergling),
      new TrainContinuously(Zerg.Drone)),
    new Build(
      RequestAtLeast(1, Zerg.Lair),
      RequestUpgrade(Zerg.ZerglingSpeed),
      RequestAtLeast(1, Zerg.Spire)),
    new If(
      new Check(() =>
        With.units.countOurs(_.unitClass.isWorker) / 6 >
        With.units.countOurs(_.gasLeft > 0)),
      new BuildGasPumps()),
    new If(
      new MineralsAtLeast(300),
      new TrainContinuously(Zerg.Hatchery, 5, 1))
  )
}
