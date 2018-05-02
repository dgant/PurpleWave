package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.MineralsAtLeast
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Matchup.EnemyIsZerg
import Planning.Plans.Predicates.Milestones._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Terran.TvE.TvEMassBio

class MassBio extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvEMassBio)
  
  override val buildOrder =
    Vector(
      RequestAtLeast(9, Terran.SCV),
      RequestAtLeast(1, Terran.SupplyDepot),
      RequestAtLeast(10, Terran.SCV),
      RequestAtLeast(1, Terran.Barracks),
      RequestAtLeast(12, Terran.SCV),
      RequestAtLeast(2, Terran.Barracks),
      RequestAtLeast(13, Terran.SCV),
      RequestAtLeast(1, Terran.Marine),
      RequestAtLeast(14, Terran.SCV),
      RequestAtLeast(2, Terran.SupplyDepot),
      RequestAtLeast(2, Terran.Marine))
  
  private val stimThreshold = 24 * 15
  
  override def priorityAttackPlan: Plan = new Trigger(
    new And(
      new UnitsAtLeast(2, Terran.Medic, complete = true),
      new TechComplete(Terran.Stim, stimThreshold)),
    new If(
      new FrameAtMost(GameTime(7, 0)()),
      new Parallel(
        new Aggression(1.6),
        new Attack),
      new Aggression(1.0)))
  
  override def defaultAttackPlan: Plan = new Trigger(
    new TechComplete(Terran.Stim, 15),
    super.defaultAttackPlan)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new If(
      new Or(
        new EnemyHasShownCloakedThreat,
        new TechComplete(Terran.Stim, Terran.Stim.researchFrames)),
      new TrainContinuously(Terran.Comsat)),
    new If(
      new EnemyUnitsAtLeast(2, Zerg.Lurker),
      new TrainContinuously(Terran.MissileTurret, 2)))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new EnemyIsZerg,
      new TrainContinuously(Terran.ScienceVessel, 12)),
    new If(
      new UnitsAtLeast(2, Terran.ScienceVessel),
      new Build(RequestTech(Terran.Irradiate))),
    new Trigger(
      new SupplyOutOf200(24),
      new Build(
        RequestAtLeast(1, Terran.Refinery),
        RequestAtLeast(1, Terran.Academy))),
    new Trigger(
      new SupplyOutOf200(27),
      new Build(
        RequestAtLeast(3, Terran.Barracks),
        RequestTech(Terran.Stim))),
    new Trigger(
      new Or(
        new EnemyUnitsAtLeast(1, Terran.Vulture),
        new EnemyUnitsAtLeast(1, Protoss.Dragoon)),
      new UpgradeContinuously(Terran.MarineRange)),
    new Trigger(
      new MiningBasesAtLeast(3),
      new Do(() => {
        With.blackboard.gasLimitFloor = 0
        With.blackboard.gasLimitCeiling = 300
      }),
      new Do(() => {
        With.blackboard.gasLimitFloor = 100
        With.blackboard.gasLimitCeiling = 150
      })),
    new If(
      new TechComplete(Terran.Stim),
      new RequireMiningBases(2)),
    new If(
      new UnitsAtLeast(1, UnitMatchSiegeTank),
      new Build(RequestTech(Terran.SiegeMode))),
    new IfOnMiningBases(3,
      new If(
        new UnitsAtLeast(60, UnitMatchWarriors),
        new Parallel(
          new Build(
            RequestAtLeast(2, Terran.Refinery),
            RequestAtLeast(1, Terran.Factory),
            RequestAtLeast(1, Terran.Starport),
            RequestAtLeast(1, Terran.ScienceFacility),
            RequestAtLeast(1, Terran.ControlTower),
            RequestAtLeast(2, Terran.EngineeringBay),
            RequestAtLeast(1, Terran.Dropship))))),
    new If(
      new UnitsAtLeast(40, UnitMatchWarriors),
      new RequireMiningBases(3)),
    new If(
      new UnitsAtLeast(55, UnitMatchWarriors),
      new RequireMiningBases(4)),
    new If(
      new UnitsAtLeast(65, UnitMatchWarriors),
      new RequireMiningBases(5)),
  
    new TrainContinuously(Terran.SiegeTankUnsieged),
    new If(
      new UnitsAtMost(0, Terran.Academy, complete = true),
      new TrainContinuously(Terran.Marine),
      new Parallel(
        new If(
          new Check(() =>
            With.units.countOurs(u => u.is(Terran.Marine) || u.is(Terran.Firebat)) >
            8 * With.units.countOurs(Terran.Medic)),
          new TrainContinuously(Terran.Medic, maximumConcurrentlyRatio = 0.67)),
        new If(
          new Or(
            new EnemyUnitsAtLeast(3, Protoss.Zealot),
            new EnemyUnitsAtLeast(1, Zerg.Zergling)),
          new TrainContinuously(Terran.Firebat, maximumConcurrentlyRatio = 0.34, maximumTotal = 2)),
        new TrainContinuously(Terran.Marine)
        )),
  
    new IfOnMiningBases(2,
      new Parallel(
        new UpgradeContinuously(Terran.MarineRange),
        new If(
          new EnemyUnitsAtLeast(1, UnitMatchSiegeTank),
          new Build(
            RequestAtLeast(3, Terran.Factory),
            RequestAtLeast(3, Terran.MachineShop),
            RequestAtLeast(1, Terran.Starport)),
          new Build(
            RequestAtLeast(5, Terran.Barracks))),
        new If(
          new EnemyIsZerg,
          new Build(
            RequestAtLeast(1, Terran.EngineeringBay),
            RequestAtLeast(1, Terran.Factory),
            RequestUpgrade(Terran.BioDamage),
            RequestAtLeast(2, Terran.Refinery),
            RequestAtLeast(1, Terran.Starport),
            RequestAtLeast(1, Terran.ScienceFacility),
            RequestAtLeast(2, Terran.Starport),
            RequestAtLeast(2, Terran.ControlTower)),
          new Build(RequestAtLeast(1, Terran.EngineeringBay))),
        new UpgradeContinuously(Terran.BioDamage),
        new UpgradeContinuously(Terran.BioArmor))),
    new Trigger(
      new Or(
        new SupplyOutOf200(30),
        new MineralsAtLeast(400)),
      new If(
        new Or(
          new Check(() => With.units.countOurs(Terran.Barracks) < 5 * With.geography.ourBases.size),
          new MineralsAtLeast(600)),
        new TrainContinuously(Terran.Barracks, 30, 3)))
  )
}