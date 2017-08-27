package Planning.Plans.Terran.GamePlans

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{DefendEntrance, Scan}
import Planning.Plans.Compound._
import Planning.Plans.Information.Employ
import Planning.Plans.Macro.Automatic.Gather
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, FollowBuildOrder, RequireBareMinimum}
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.Situational.DefendProxy
import Planning.Plans.Recruitment.RecruitFreelancers
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvZ._

class TerranVsZergEarly extends Parallel {
  
  private class Build1RaxFEConservative extends FirstEightMinutes(
    new Build(
      RequestAtLeast(1,   Terran.CommandCenter),
      RequestAtLeast(9,   Terran.SCV),
      RequestAtLeast(1,   Terran.Barracks),
      RequestAtLeast(1,   Terran.SupplyDepot),
      RequestAtLeast(11,  Terran.SCV),
      RequestAtLeast(1,   Terran.Marine),
      RequestAtLeast(1,   Terran.Bunker)
    ))
  
  private class Build1RaxGas extends FirstEightMinutes(
    new Build(
      RequestAtLeast(1,   Terran.CommandCenter),
      RequestAtLeast(9,   Terran.SCV),
      RequestAtLeast(1,   Terran.SupplyDepot),
      RequestAtLeast(11,  Terran.SCV),
      RequestAtLeast(1,   Terran.Barracks),
      RequestAtLeast(12,  Terran.SCV),
      RequestAtLeast(1,   Terran.Refinery),
      RequestAtLeast(13,  Terran.SCV),
      RequestAtLeast(1,   Terran.Marine),
      RequestAtLeast(14,  Terran.SCV),
      RequestAtLeast(2,   Terran.SupplyDepot),
      RequestAtLeast(2,   Terran.Marine),
      RequestAtLeast(1,   Terran.Factory)
    ))
  
  private class Build1RaxFEEconomic extends FirstEightMinutes(
    new Build(
      RequestAtLeast(1,   Terran.CommandCenter),
      RequestAtLeast(9,   Terran.SCV),
      RequestAtLeast(1,   Terran.SupplyDepot),
      RequestAtLeast(11,  Terran.SCV),
      RequestAtLeast(1,   Terran.Barracks)
    ))
  
  private class Build2Rax extends FirstEightMinutes(
    new Build(
      RequestAtLeast(1,   Terran.CommandCenter),
      RequestAtLeast(9,   Terran.SCV),
      RequestAtLeast(1,   Terran.SupplyDepot),
      RequestAtLeast(11,  Terran.SCV),
      RequestAtLeast(1,   Terran.Barracks),
      RequestAtLeast(13,  Terran.SCV),
      RequestAtLeast(2,   Terran.Barracks),
      RequestAtLeast(14,  Terran.SCV),
      RequestAtLeast(2,   Terran.SupplyDepot)
    ))
  
  private class BuildCCFirst extends FirstEightMinutes(
    new Build(
      RequestAtLeast(1,   Terran.CommandCenter),
      RequestAtLeast(9,   Terran.SCV),
      RequestAtLeast(1,   Terran.SupplyDepot),
      RequestAtLeast(14,  Terran.SCV),
      RequestAtLeast(2,   Terran.CommandCenter),
      RequestAtLeast(15,  Terran.SCV),
      RequestAtLeast(1,   Terran.Barracks),
      RequestAtLeast(2,   Terran.SupplyDepot),
      RequestAtLeast(17,  Terran.SCV),
      RequestAtLeast(2,   Terran.Barracks)
    ))
  
  children.set(Vector(
    new RequireBareMinimum,
    new ProposePlacement {
      override lazy val blueprints: Iterable[Blueprint] = Vector(
        new Blueprint(this, building = Some(Terran.Bunker),         preferZone = Some(With.geography.ourNatural.zone)),
        new Blueprint(this, building = Some(Terran.MissileTurret),  preferZone = Some(With.geography.ourNatural.zone), marginPixels = Some(32.0 * 6.0)),
        new Blueprint(this, building = Some(Terran.Bunker),         preferZone = Some(With.geography.ourNatural.zone)),
        new Blueprint(this, building = Some(Terran.Bunker),         preferZone = Some(With.geography.ourNatural.zone)),
        new Blueprint(this, building = Some(Terran.Bunker),         preferZone = Some(With.geography.ourNatural.zone))
      )
    },
    new Employ(TvZEarly1RaxFEConservative,  new Build1RaxFEConservative),
    new Employ(TvZEarly1RaxFEEconomic,      new Build1RaxFEEconomic),
    new Employ(TvZEarly1RaxGas,             new Build1RaxGas),
    new Employ(TvZEarly2Rax,                new Build2Rax),
    new Employ(TvZEarlyCCFirst,             new BuildCCFirst),
    new If(new UnitsAtLeast(4, UnitMatchWarriors), new RequireMiningBases(2)),
    new Employ(TvZMidgameBio,     new TerranVsZergBio),
    new Employ(TvZMidgameMech,    new TerranVsZergMech),
    new Employ(TvZMidgameWraiths, new TerranVsZergMech),
    new FollowBuildOrder,
    new Scan,
    new DefendProxy,
    new RemoveMineralBlocksAt(40),
    new Gather,
    new RecruitFreelancers,
    new DefendEntrance
  ))
}