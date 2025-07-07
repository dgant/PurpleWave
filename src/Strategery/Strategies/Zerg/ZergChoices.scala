package Strategery.Strategies.Zerg

import Gameplans.Zerg.ZvE.{ZvE4Pool, ZvZ10Hatch}
import Gameplans.Zerg.ZvP.{ZvPCrackling, ZvPHydraLurker}
import Gameplans.Zerg.ZvT.{ZvT13PoolMuta, ZvTEffortBust, ZvTOneBaseLurker}
import Planning.Plans.Plan
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvEStrategy extends Strategy     { setOurRace(Race.Zerg)       }
abstract class ZvTStrategy extends ZvEStrategy  { setEnemyRace(Race.Terran)   }
abstract class ZvPStrategy extends ZvEStrategy  { setEnemyRace(Race.Protoss)  }
abstract class ZvZStrategy extends ZvEStrategy  { setEnemyRace(Race.Zerg)     }

object ZvE4Pool           extends ZvEStrategy { override def gameplan: Option[Plan] = Some(new ZvE4Pool) }
object ZvTEffortBust      extends ZvTStrategy { override def gameplan: Option[Plan] = Some(new ZvTEffortBust) }
object ZvTOneHatchLurker  extends ZvTStrategy { override def gameplan: Option[Plan] = Some(new ZvTOneBaseLurker) }
object ZvT13PoolMuta      extends ZvTStrategy { override def gameplan: Option[Plan] = Some(new ZvT13PoolMuta) }
object ZvPCrackling       extends ZvPStrategy { override def gameplan: Option[Plan] = Some(new ZvPCrackling) }
object ZvPHydraLurker     extends ZvPStrategy { override def gameplan: Option[Plan] = Some(new ZvPHydraLurker) }
object ZvZ10Hatch         extends ZvZStrategy { override def gameplan: Option[Plan] = Some(new ZvZ10Hatch) }

object ZergChoices {

  val zvr: Vector[Strategy] = Vector(
    ZvE4Pool
  )

  val zvt: Vector[Strategy] = Vector(
    ZvTEffortBust,
    ZvTOneHatchLurker,
    ZvT13PoolMuta
  )

  val zvp: Vector[Strategy] = Vector(
    ZvPCrackling,
    ZvPHydraLurker
  )

  val zvz: Vector[Strategy] = Vector(
    ZvZ10Hatch
  )
  
  val all: Vector[Strategy] = (zvr ++ zvt ++ zvp ++ zvz).distinct
}