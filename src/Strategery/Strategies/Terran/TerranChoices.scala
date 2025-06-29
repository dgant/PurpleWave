package Strategery.Strategies.Terran

import Gameplans.Terran.TvE.{TvE3Fac, TvEBBS, TvZSparks}
import Gameplans.Terran.TvP.TvP2Fac
import Gameplans.Terran.TvT.{TvT1Port, TvT3FacVult}
import Gameplans.Terran.TvZ.TvZ8RaxSK
import Planning.Plans.Plan
import Strategery.Strategies._
import bwapi.Race

abstract class TvEStrategy extends Strategy     { setOurRace(Race.Terran)     }
abstract class TvTStrategy extends TvEStrategy  { setEnemyRace(Race.Terran)   }
abstract class TvPStrategy extends TvEStrategy  { setEnemyRace(Race.Protoss)  }
abstract class TvZStrategy extends TvEStrategy  { setEnemyRace(Race.Zerg)     }
abstract class TvRStrategy extends TvEStrategy  { setEnemyRace(Race.Unknown)  }

object TvEBBS       extends TvEStrategy { override def gameplan: Option[Plan] = Some(new TvEBBS)      }
object TvE3Fac      extends TvEStrategy { override def gameplan: Option[Plan] = Some(new TvE3Fac)     }
object TvT1Port     extends TvTStrategy { override def gameplan: Option[Plan] = Some(new TvT1Port)    }
object TvT3FacVult  extends TvTStrategy { override def gameplan: Option[Plan] = Some(new TvT3FacVult) }
object TvP2Fac      extends TvPStrategy { override def gameplan: Option[Plan] = Some(new TvP2Fac)     }
object TvZ8RaxSK    extends TvZStrategy { override def gameplan: Option[Plan] = Some(new TvZ8RaxSK)   }
object TvZSparks    extends TvEStrategy { override def gameplan: Option[Plan] = Some(new TvZSparks)   }

object TerranChoices {

  val tvr: Vector[Strategy] = Vector(
    TvEBBS, TvE3Fac
  )
  
  val tvtOpeners: Vector[Strategy] = Vector(
    TvT1Port, TvT3FacVult
  )
  
  val tvpOpeners: Vector[Strategy] = Vector(
    TvP2Fac
  )
  
  val tvzOpeners: Vector[Strategy] = Vector(
    TvZ8RaxSK, TvZSparks
  )

  val all: Vector[Strategy] = (tvr ++ tvtOpeners ++ tvpOpeners ++ tvzOpeners).distinct
}