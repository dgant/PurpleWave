package Strategery.Strategies.Terran

import Gameplans.Terran.TvE.{TvE3Fac, TvEBBS, TvZSparks}
import Gameplans.Terran.TvP.TvP2Fac
import Gameplans.Terran.TvT.{TvT1Port, TvT3FacVult, TvTFE}
import Gameplans.Terran.TvZ.{TvZ111, TvZ8RaxSK, TvZGoliath}
import Lifecycle.With
import Planning.Plans.Plan
import Strategery.Strategies._
import bwapi.Race

abstract class TvEStrategy extends Strategy     { setOurRace(Race.Terran)     }
abstract class TvTStrategy extends TvEStrategy  { setEnemyRace(Race.Terran)   }
abstract class TvPStrategy extends TvEStrategy  { setEnemyRace(Race.Protoss)  }
abstract class TvZStrategy extends TvEStrategy  { setEnemyRace(Race.Zerg)     }
abstract class TvRStrategy extends TvEStrategy  { setEnemyRace(Race.Unknown)  }

object TvEBBS       extends TvEStrategy { override def gameplan: Option[Plan] = Some(new TvEBBS)      ; blacklistVs(With.fingerprints.oneFac, With.fingerprints.twoFac, With.fingerprints.threeFac) }
object TvE3Fac      extends TvEStrategy { override def gameplan: Option[Plan] = Some(new TvE3Fac)     }
object TvT1Port     extends TvTStrategy { override def gameplan: Option[Plan] = Some(new TvT1Port)    }
object TvT3FacVult  extends TvTStrategy { override def gameplan: Option[Plan] = Some(new TvT3FacVult) }
object TvTFE        extends TvTStrategy { override def gameplan: Option[Plan] = Some(new TvTFE)       ; blacklistVs(With.fingerprints.bbs) }
object TvP2Fac      extends TvPStrategy { override def gameplan: Option[Plan] = Some(new TvP2Fac)     }
object TvZ8RaxSK    extends TvZStrategy { override def gameplan: Option[Plan] = Some(new TvZ8RaxSK)   }
object TvZSparks    extends TvZStrategy { override def gameplan: Option[Plan] = Some(new TvZSparks)   }
object TvZ111       extends TvZStrategy { override def gameplan: Option[Plan] = Some(new TvZ111)      }
object TvZGoliath   extends TvZStrategy { override def gameplan: Option[Plan] = Some(new TvZGoliath)  }

object TerranChoices {

  val tvr: Vector[Strategy] = Vector(
    TvEBBS, TvE3Fac
  )
  
  val tvtOpeners: Vector[Strategy] = Vector(
    TvT1Port, TvT3FacVult, TvTFE
  )
  
  val tvpOpeners: Vector[Strategy] = Vector(
    TvP2Fac
  )
  
  val tvzOpeners: Vector[Strategy] = Vector(
    TvZ8RaxSK, TvZSparks, TvZ111, TvZGoliath
  )

  val all: Vector[Strategy] = (tvr ++ tvtOpeners ++ tvpOpeners ++ tvzOpeners).distinct
}