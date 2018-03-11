package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.TrainWorkersContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Reactive.{EnemyBasesAtLeast, EnemyBio}
import Planning.Plans.Predicates.{Employing, Never}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT._

class PvTBasic extends GameplanModeTemplate {
  override val activationCriteria     = new Employing(PvT13Nexus, PvT21Nexus, PvTDTExpand, PvT2BaseCarrier, PvT2BaseArbiter, PvT3BaseCarrier, PvT3BaseArbiter)
  override val completionCriteria     = new Never
  override val buildOrder             = ProtossBuilds.OpeningDTExpand
  override val defaultWorkerPlan      = new TrainWorkersContinuously(oversaturate = true)
  override val priorityAttackPlan     = new PvTIdeas.PriorityAttacks
  override val defaultAttackPlan      = new ConsiderAttacking
  
  override def emergencyPlans: Seq[Plan] = Vector(new PvTIdeas.EmergencyBuilds)
  
  override def defaultBuildOrder: Plan = new Parallel(
    new If(new Employing(PvT13Nexus),   new Build(ProtossBuilds.Opening13Nexus_NoZealot_TwoGateways: _*)),
    new If(new Employing(PvT21Nexus),   new Build(ProtossBuilds.Opening21Nexus_Robo: _*)),
    new If(new Employing(PvTDTExpand),  new Build(ProtossBuilds.OpeningDTExpand: _*)))
  
  override val buildPlans = Vector(
    new RequireMiningBases(2),
    new PvTIdeas.TrainArmy,
    new BuildCannonsAtExpansions(2),
    new If(
      new UnitsAtLeast(1, Protoss.HighTemplar),
      new Build(RequestTech(Protoss.PsionicStorm))),
    new If(
      new UnitsAtLeast(2, Protoss.Carrier),
      new Parallel(
        new If(
          new EnemyBio,
          new UpgradeContinuously(Protoss.AirArmor),
          new UpgradeContinuously(Protoss.AirDamage)),
        new UpgradeContinuously(Protoss.CarrierCapacity))),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(1, Protoss.RoboticsFacility)),
    new If(
      new And(
        new EnemyBio,
        new UnitsAtMost(0, Protoss.TemplarArchives)),
      new Build(RequestAtLeast(1, Protoss.RoboticsSupportBay))),
    new If(
      new Or(
        new EnemyHasShownCloakedThreat,
        new EnemyUnitsAtLeast(2, Terran.Vulture),
        new EnemyUnitsAtLeast(2, Terran.Factory)),
      new Build(RequestAtLeast(1, Protoss.Observatory))),
    new If(
      new And(
        new EnemyUnitsAtLeast(3, Terran.SpiderMine),
        new Or(
          new MiningBasesAtLeast(3),
          new Employing(PvT3BaseCarrier, PvT3BaseArbiter))),
      new UpgradeContinuously(Protoss.ObserverSpeed)),
    new If(
      new Employing(PvT2BaseCarrier, PvT2BaseArbiter),
      new BuildGasPumps),
    new If(
      new Or(
        new EnemyUnitsAtLeast(2, Terran.CommandCenter),
        new EnemyBasesAtLeast(2)),
      new RequireMiningBases(3)),
    new If(
      new Or(
        new Employing(PvT2BaseCarrier),
        new And(
          new Employing(PvT3BaseCarrier),
          new MiningBasesAtLeast(3))),
      new Parallel(
        new BuildGasPumps,
        new Build(
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(4, Protoss.Gateway),
          RequestAtLeast(1, Protoss.FleetBeacon),
          RequestAtLeast(2, Protoss.Stargate)))),
    new If(
      new Or(
        new Employing(PvT2BaseArbiter),
        new And(
          new Employing(PvT3BaseArbiter),
          new MiningBasesAtLeast(3))),
      new Parallel(
        new BuildGasPumps,
        new Build(
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestAtLeast(1, Protoss.TemplarArchives),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(4, Protoss.Gateway),
          RequestAtLeast(1, Protoss.ArbiterTribunal),
          RequestTech(Protoss.Stasis)))),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(1, Protoss.Forge)),
    new BuildGasPumps,
    new Build(
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(6, Protoss.Gateway)),
    new If(
      new Or(
        new Employing(PvT2BaseCarrier),
        new Employing(PvT3BaseCarrier)),
      new Build(RequestAtLeast(3, Protoss.Stargate))),
    new RequireMiningBases(4),
    new If(
      new Or(
        new Employing(PvT2BaseCarrier),
        new Employing(PvT3BaseCarrier)),
      new Parallel(
        new UpgradeContinuously(Protoss.AirArmor),
        new UpgradeContinuously(Protoss.AirDamage))),
    new Build(RequestAtLeast(2, Protoss.Forge)),
    new UpgradeContinuously(Protoss.GroundArmor),
    new Build(RequestAtLeast(1, Protoss.TemplarArchives)),
    new Build(RequestAtLeast(20, Protoss.Gateway)),
    new RequireMiningBases(5)
  )
}

