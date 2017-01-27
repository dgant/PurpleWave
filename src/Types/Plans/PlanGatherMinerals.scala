package Types.Plans

import Startup.BotListener
import Types.Matcher.UnitMatcher
import Types.Quantities.AtLeast
import Types.Resources.{Resource, ResourceUnit}
import Types.Tactics.{Tactic, TacticGatherMinerals}

import scala.collection.JavaConverters._

class PlanGatherMinerals extends Plan {

  override def execute():Iterable[Tactic] = {
    //Let's keep this real simple for the moment
    return BotListener.bot.get.game
      .getAllUnits().asScala
      .filter(unit => unit.canGather())
      .map(unit => new TacticGatherMinerals(unit))
  }

  val _requiredResources = new ResourceUnit(
    new AtLeast(1),
    new UnitMatcher())
  override def getRequiredResources(): Resource = {
    return _requiredResources
  }


}
