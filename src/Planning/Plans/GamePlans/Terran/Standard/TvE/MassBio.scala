package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Predicates.Compound.{And, Check}
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyIsZerg}
import Planning.Predicates.Milestones._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Terran.TvE.TvEMassBio

class MassBio extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvEMassBio)
  
  override val buildOrder =
    Vector(
      Get(9, Terran.SCV),
      Get(1, Terran.SupplyDepot),
      Get(10, Terran.SCV),
      Get(1, Terran.Barracks),
      Get(12, Terran.SCV),
      Get(2, Terran.Barracks),
      Get(13, Terran.SCV),
      Get(1, Terran.Marine),
      Get(14, Terran.SCV),
      Get(2, Terran.SupplyDepot),
      Get(2, Terran.Marine))
  
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
      new Pump(Terran.Comsat)),
    new If(
      new EnemiesAtLeast(2, Zerg.Lurker),
      new Pump(Terran.MissileTurret, 2)))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new EnemyIsZerg,
      new Pump(Terran.ScienceVessel, 12)),
    new If(
      new UnitsAtLeast(2, Terran.ScienceVessel),
      new Build(Get(Terran.Irradiate))),
    new Trigger(
      new SupplyOutOf200(24),
      new Build(
        Get(1, Terran.Refinery),
        Get(1, Terran.Academy))),
    new Trigger(
      new SupplyOutOf200(27),
      new Build(
        Get(3, Terran.Barracks),
        Get(Terran.Stim))),
    new Trigger(
      new Or(
        new EnemiesAtLeast(1, Terran.Vulture),
        new EnemiesAtLeast(1, Protoss.Dragoon)),
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
      new Build(Get(Terran.SiegeMode))),
    new IfOnMiningBases(3,
      new If(
        new UnitsAtLeast(60, UnitMatchWarriors),
        new Parallel(
          new Build(
            Get(2, Terran.Refinery),
            Get(1, Terran.Factory),
            Get(1, Terran.Starport),
            Get(1, Terran.ScienceFacility),
            Get(1, Terran.ControlTower),
            Get(2, Terran.EngineeringBay),
            Get(1, Terran.Dropship))))),
    new If(
      new UnitsAtLeast(40, UnitMatchWarriors),
      new RequireMiningBases(3)),
    new If(
      new UnitsAtLeast(55, UnitMatchWarriors),
      new RequireMiningBases(4)),
    new If(
      new UnitsAtLeast(65, UnitMatchWarriors),
      new RequireMiningBases(5)),
  
    new Pump(Terran.SiegeTankUnsieged),
    new If(
      new UnitsAtMost(0, Terran.Academy, complete = true),
      new Pump(Terran.Marine),
      new Parallel(
        new If(
          new Check(() =>
            With.units.countOurs(Terran.Marine, Terran.Firebat) >
            8 * With.units.countOurs(Terran.Medic)),
          new Pump(Terran.Medic, maximumConcurrentlyRatio = 0.67)),
        new If(
          new Or(
            new EnemiesAtLeast(3, Protoss.Zealot),
            new EnemiesAtLeast(1, Zerg.Zergling)),
          new Pump(Terran.Firebat, maximumConcurrentlyRatio = 0.34, maximumTotal = 2)),
        new Pump(Terran.Marine)
        )),
  
    new IfOnMiningBases(2,
      new Parallel(
        new UpgradeContinuously(Terran.MarineRange),
        new If(
          new EnemiesAtLeast(1, UnitMatchSiegeTank),
          new Build(
            Get(3, Terran.Factory),
            Get(3, Terran.MachineShop),
            Get(1, Terran.Starport)),
          new Build(
            Get(5, Terran.Barracks))),
        new If(
          new EnemyIsZerg,
          new Build(
            Get(1, Terran.EngineeringBay),
            Get(1, Terran.Factory),
            Get(Terran.BioDamage),
            Get(2, Terran.Refinery),
            Get(1, Terran.Starport),
            Get(1, Terran.ScienceFacility),
            Get(2, Terran.Starport),
            Get(2, Terran.ControlTower)),
          new Build(Get(1, Terran.EngineeringBay))),
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
        new Pump(Terran.Barracks, 30, 3)))
  )
}