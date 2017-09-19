package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack, DefendZones}
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly1015GateGoon

class PvT1015Gate extends Mode {
  
  description.set("PvT 10/15 Gate Goon")
  
  override val activationCriteria: Plan = new Employing(PvTEarly1015GateGoon)
  override val completionCriteria: Plan = new MiningBasesAtLeast(2)
  
  children.set(Vector(
    new Aggression(1.2),
    new BuildOrder(ProtossBuilds.Opening10Gate15GateDragoons: _*),
    new If(new UnitsAtLeast(5, Protoss.Dragoon), new RequireMiningBases(2)),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new TrainContinuously(Protoss.Dragoon),
    new RequireMiningBases(2),
    new ScoutAt(14),
    new DefendZones,
    new Attack
  ))
}

