package Strategery.Strategies.Zerg

import Gameplans.Zerg.ZvE.ZvE4Pool
import Gameplans.Zerg.ZvP.{ZvP10HatchSpeed, ZvP11Hatch, ZvPCrackling, ZvPHydraLurker}
import Gameplans.Zerg.ZvT.{ZvT13PoolMuta, ZvT3HatchCrazy, ZvTEffortBust, ZvTOneBaseLurker}
import Gameplans.Zerg.ZvZ.{ZvZ10Hatch, ZvZ9Pool}
import Lifecycle.With
import Planning.Plans.Plan
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvEStrategy extends Strategy     { setOurRace(Race.Zerg)       }
abstract class ZvTStrategy extends ZvEStrategy  { setEnemyRace(Race.Terran)   }
abstract class ZvPStrategy extends ZvEStrategy  { setEnemyRace(Race.Protoss)  }
abstract class ZvZStrategy extends ZvEStrategy  { setEnemyRace(Race.Zerg)     }

object ZvE4Pool           extends ZvEStrategy { override def gameplan: Option[Plan] = Some(new ZvE4Pool)        ; blacklistVs(With.fingerprints.bbs, With.fingerprints.twoGate99, With.fingerprints.ninePool, With.fingerprints.overpool) }
object ZvTEffortBust      extends ZvTStrategy { override def gameplan: Option[Plan] = Some(new ZvTEffortBust)     }
object ZvTOneHatchLurker  extends ZvTStrategy { override def gameplan: Option[Plan] = Some(new ZvTOneBaseLurker)  }
object ZvT13PoolMuta      extends ZvTStrategy { override def gameplan: Option[Plan] = Some(new ZvT13PoolMuta)   ; blacklistVs(With.fingerprints.bbs, With.fingerprints.twoRax1113, With.fingerprints.twoRaxAcad)  }
object ZvT3HatchCrazy     extends ZvTStrategy { override def gameplan: Option[Plan] = Some(new ZvT3HatchCrazy)    }
object ZvPCrackling       extends ZvPStrategy { override def gameplan: Option[Plan] = Some(new ZvPCrackling)      }
object ZvPHydraLurker     extends ZvPStrategy { override def gameplan: Option[Plan] = Some(new ZvPHydraLurker)    }
object ZvP10HatchSpeed    extends ZvPStrategy { override def gameplan: Option[Plan] = Some(new ZvP10HatchSpeed)   }
object ZvP11Hatch         extends ZvPStrategy { override def gameplan: Option[Plan] = Some(new ZvP11Hatch)        }
object ZvZ10Hatch         extends ZvZStrategy { override def gameplan: Option[Plan] = Some(new ZvZ10Hatch)      ; blacklistVs(With.fingerprints.fourPool)  }
object ZvZ9Pool           extends ZvZStrategy { override def gameplan: Option[Plan] = Some(new ZvZ9Pool)          }

object ZergChoices {

  val zvr: Vector[Strategy] = Vector(
    ZvE4Pool
  )

  val zvt: Vector[Strategy] = Vector(
    ZvTEffortBust,
    ZvTOneHatchLurker,
    ZvT13PoolMuta,
    ZvT3HatchCrazy
  )

  val zvp: Vector[Strategy] = Vector(
    ZvPCrackling,
    ZvPHydraLurker,
    ZvP10HatchSpeed,
    ZvP11Hatch
  )

  val zvz: Vector[Strategy] = Vector(
    ZvZ10Hatch,
    ZvZ9Pool
  )
  
  val all: Vector[Strategy] = (zvr ++ zvt ++ zvp ++ zvz).distinct
}