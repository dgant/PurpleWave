package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.Buildables.Buildable
import Planning.Plan
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Compound.Trigger
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.PumpWorkers
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT1015DT

class PvT1015DT extends GameplanTemplate {
  
  override val activationCriteria = new Employing(PvT1015DT)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override val attackPlan = new AttackAndHarass
  override def scoutPlan = new ScoutOn(Protoss.Gateway, quantity = 2)

  override def workerPlan: Plan = new PumpWorkers(oversaturate = true)

  override val buildOrder: Vector[Buildable] = ProtossBuilds.PvT1015GateGoonDT

  override def emergencyPlans: Seq[Plan] = Seq(
    new PvTIdeas.ReactToFiveRaxAs2GateCore,
    new PvTIdeas.ReactToWorkerRush)

  override val buildPlans = Vector(
    new Trigger(
      new UnitsAtLeast(2, Protoss.DarkTemplar),
      new RequireMiningBases(2)),
    new PvTIdeas.TrainArmy,
    new RequireMiningBases(2))
}

