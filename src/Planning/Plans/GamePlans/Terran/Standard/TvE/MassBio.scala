package Planning.Plans.GamePlans.Terran.Standard.TvE

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Information.Reactive.EnemyMutalisks
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEMassBio

class MassBio extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvEMassBio)
  
  override val aggression = 0.8
  
  override val buildOrder = Vector(
    RequestAtLeast(9, Terran.SCV),
    RequestAtLeast(1, Terran.SupplyDepot),
    RequestAtLeast(10, Terran.SCV),
    RequestAtLeast(1, Terran.Barracks),
    RequestAtLeast(12, Terran.SCV),
    RequestAtLeast(1, Terran.Bunker),
    RequestAtLeast(1, Terran.Marine),
    RequestAtLeast(14, Terran.SCV),
    RequestAtLeast(2, Terran.Barracks))
  
  override def defaultAttackPlan: Plan = new If(
    new UnitsAtLeast(12, UnitMatchWarriors),
    super.defaultAttackPlan)
  
  override def emergencyPlans: Seq[Plan] = Seq(new TrainContinuously(Terran.Comsat))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(8, UnitMatchWarriors),
      new Build(
        RequestAtLeast(1, Terran.Refinery),
        RequestAtLeast(1, Terran.Academy),
        RequestTech(Terran.Stim))),
    new If(
      new UnitsAtLeast(20, UnitMatchWarriors),
      new Parallel(
        new Build(
          RequestUpgrade(Terran.MarineRange),
          RequestAtLeast(1, Terran.EngineeringBay)),
        new UpgradeContinuously(Terran.BioDamage))),
    new If(
      new UnitsAtLeast(40, UnitMatchWarriors),
      new Parallel(
        new Build(
          RequestAtLeast(2, Terran.Refinery),
          RequestAtLeast(1, Terran.Factory),
          RequestUpgrade(Terran.BioArmor),
          RequestAtLeast(1, Terran.Starport),
          RequestAtLeast(1, Terran.ScienceFacility),
          RequestAtLeast(1, Terran.ControlTower),
          RequestAtLeast(2, Terran.EngineeringBay)),
        new UpgradeContinuously(Terran.BioArmor))),
    new If(
      new UnitsAtLeast(60, UnitMatchWarriors),
      new Build(RequestAtLeast(1, Terran.Dropship))),
    new If(
      new UnitsAtLeast(20, UnitMatchWarriors),
      new RequireMiningBases(2)),
    new If(
      new UnitsAtLeast(40, UnitMatchWarriors),
      new RequireMiningBases(3)),
    new If(
      new UnitsAtLeast(55, UnitMatchWarriors),
      new RequireMiningBases(4)),
    new If(
      new UnitsAtLeast(65, UnitMatchWarriors),
      new RequireMiningBases(5)),
    
    new If(
      new UnitsAtMost(0, Terran.Academy, complete = true),
      new TrainContinuously(Terran.Marine),
      new Parallel(
        new If(
          new Check(() =>
            With.units.ours.count(u => u.is(Terran.Marine) || u.is(Terran.Firebat)) >
            8 * With.units.ours.count(_.is(Terran.Medic))),
          new TrainContinuously(Terran.Medic, maximumConcurrentlyRatio = 0.5)),
        new If(
          new Not(new EnemyMutalisks),
          new TrainContinuously(Terran.Firebat, maximumConcurrentlyRatio = 0.75)),
        new TrainContinuously(Terran.Marine)
        )),
    new If(
      new Or(
        new Check(() => With.units.ours.count(_.is(Terran.Barracks)) < 5 * With.geography.ourBases.size),
        new Check(() => With.self.minerals > 600)),
      new TrainContinuously(Terran.Barracks, 30, 3)),
    new RequireMiningBases(3)
  )
}