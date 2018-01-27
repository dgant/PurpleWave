package Planning.Plans.GamePlans.Terran.Standard.TvE

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Army.NukeBase
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.{Employing, SafeAtHome}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, IfOnMiningBases, MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvE.TvTPNukeDrop

class TvTPNukeDrop extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvTPNukeDrop)
  override def defaultAttackPlan = new If(
    new And(
      new UnitsAtLeast(40, UnitMatchWarriors),
      new MiningBasesAtLeast(2)),
    super.defaultAttackPlan)
  
  override def priorityAttackPlan: Plan = new NukeBase
  override def defaultDropPlan: Plan = NoPlan()
  
  override val buildOrder = Vector(
    RequestAtLeast(9, Terran.SCV),
    RequestAtLeast(1, Terran.SupplyDepot),
    RequestAtLeast(10, Terran.SCV),
    RequestAtLeast(1, Terran.Barracks),
    RequestAtLeast(11, Terran.SCV),
    RequestAtLeast(1, Terran.Refinery))
  
  override def defaultWorkerPlan: Plan = NoPlan()
  
  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Terran.NuclearSilo),
    new TrainContinuously(Terran.NuclearMissile),
    new TrainContinuously(Terran.SCV),
    new TrainContinuously(Terran.Ghost, 2, 1),
    new TrainContinuously(Terran.Dropship, 2, 1),
    new If(
      new Or(
        new EnemyUnitsAtLeast(1, Terran.Wraith),
        new EnemyUnitsAtLeast(1, Protoss.Scout)),
      new TrainContinuously(Terran.Wraith),
      new Build(
        RequestTech(Terran.WraithCloak),
        RequestAtLeast(2, Terran.Starport))
    ),
    new FlipIf(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(20, UnitMatchWarriors)),
    new Parallel(
      new IfOnMiningBases(2, new TrainContinuously(Terran.ScienceVessel, 2, 1)),
      new TrainContinuously(Terran.SiegeTankUnsieged),
      new TrainContinuously(Terran.Marine)),
    new FlipIf(
      new And(
        new UnitsAtLeast(8, UnitMatchWarriors),
        new UnitsAtLeast(3, UnitMatchSiegeTank),
        new SafeAtHome),
      new Build(
        RequestAtLeast(1, Terran.Bunker),
        RequestAtLeast(1, Terran.Factory),
        RequestAtLeast(1, Terran.MachineShop),
        RequestAtLeast(1, Terran.Starport),
        RequestTech(Terran.SiegeMode),
        RequestAtLeast(1, Terran.EngineeringBay),
        RequestAtLeast(1, Terran.MissileTurret),
        RequestAtLeast(1, Terran.ScienceFacility),
        RequestAtLeast(1, Terran.CovertOps),
        RequestAtLeast(1, Terran.Academy),
        RequestTech(Terran.GhostCloak),
        RequestAtLeast(1, Terran.ControlTower),
        RequestAtLeast(2, Terran.Barracks),
        RequestUpgrade(Terran.GhostVisionRange)))),
    new Build(
      RequestAtLeast(2, Terran.CommandCenter),
      RequestAtLeast(2, Terran.Bunker),
      RequestAtLeast(2, Terran.MissileTurret)),
    new Build(
      RequestUpgrade(Terran.MarineRange),
      RequestAtLeast(2, Terran.EngineeringBay)),
    new UpgradeContinuously(Terran.BioDamage),
    new UpgradeContinuously(Terran.BioArmor),
    new Build(
      RequestTech(Terran.Stim),
      RequestAtLeast(4, Terran.Factory),
      RequestAtLeast(4, Terran.MachineShop),
      RequestAtLeast(8, Terran.Barracks)),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(15, Terran.Barracks))
  )
}
