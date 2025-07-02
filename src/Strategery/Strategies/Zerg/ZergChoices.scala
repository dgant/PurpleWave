package Strategery.Strategies.Zerg

import Gameplans.Zerg.ZvE.ZvE4Pool
import Planning.Plans.Plan
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvEStrategy extends Strategy { setOurRace(Race.Zerg) }

object ZvE4Pool extends ZvEStrategy { override def gameplan: Option[Plan] = Some(new ZvE4Pool) }

object ZergChoices {

  val zvr: Vector[Strategy] = Vector(
    ZvE4Pool
  )

  val zvt: Vector[Strategy] = Vector(
  )

  val zvp: Vector[Strategy] = Vector(
  )

  val zvz: Vector[Strategy] = Vector(
  )
  
  val all: Vector[Strategy] = (zvr ++ zvt ++ zvp ++ zvz).distinct
}